package net.trueog.staffauth.service.oauth

import io.micronaut.context.annotation.Value
import jakarta.inject.Singleton
import net.trueog.staffauth.client.MinecraftClient
import net.trueog.staffauth.repository.UserRepository
import sh.ory.hydra.api.OAuth2Api
import sh.ory.hydra.model.AcceptOAuth2ConsentRequest
import sh.ory.hydra.model.AcceptOAuth2ConsentRequestSession
import sh.ory.hydra.model.OAuth2ConsentRequest
import sh.ory.hydra.model.RejectOAuth2Request
import java.net.URI
import java.time.Duration
import java.util.*

@Singleton
class ConsentService(
    private val oAuth2Api: OAuth2Api,
    private val userRepository: UserRepository,
    private val minecraftClient: MinecraftClient
) {
    @Value($$"${hydra.remember-duration}")
    lateinit var rememberDuration: Duration

    fun getConsentRequest(consentChallenge: String): OAuth2ConsentRequest {
        return oAuth2Api.getOAuth2ConsentRequest(consentChallenge)
    }

    suspend fun accept(consentRequest: OAuth2ConsentRequest): URI {
        val subject = consentRequest.subject ?: throw IllegalStateException()
        val user = userRepository.findByUuid(UUID.fromString(subject)) ?: throw IllegalStateException()
        val claims = buildMap {
            if (consentRequest.requestedScope?.contains("email") == true) put("email", user.email)
            if (consentRequest.requestedScope?.contains("roles") == true) put("roles", arrayOf(user.role))
            if (consentRequest.requestedScope?.contains("profile") == true) {
                put("name", minecraftClient.getByUuid(user.minecraftUuid)?.name ?: throw IllegalStateException())
                put("picture", "https://minotar.net/helm/${user.minecraftUuid.toString().replace("-", "")}.png")
            }
        }
        val response = oAuth2Api.acceptOAuth2ConsentRequest(
            consentRequest.challenge, AcceptOAuth2ConsentRequest().grantScope(consentRequest.requestedScope).session(
                AcceptOAuth2ConsentRequestSession().idToken(claims)
            ).remember(true).rememberFor(rememberDuration.seconds)
        )
        return URI.create(response.redirectTo)
    }

    fun reject(consentRequest: OAuth2ConsentRequest): URI {
        val response = oAuth2Api.rejectOAuth2ConsentRequest(consentRequest.challenge, RejectOAuth2Request())
        return URI.create(response.redirectTo)
    }
}