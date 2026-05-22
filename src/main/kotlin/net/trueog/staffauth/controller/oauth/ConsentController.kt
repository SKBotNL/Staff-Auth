package net.trueog.staffauth.controller.oauth

import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.*
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import net.trueog.staffauth.dto.consent.ConsentDto
import net.trueog.staffauth.service.oauth.ConsentService
import sh.ory.hydra.ApiException

@Controller("/consent")
@Secured(SecurityRule.IS_ANONYMOUS)
class ConsentController(
    private val consentService: ConsentService
) {
    @Get
    fun index(@QueryValue("consent_challenge") consentChallenge: String): Map<String, Any?> {
        val consentRequest = consentService.getConsentRequest(consentChallenge)

        return mapOf(
            "skip" to (consentRequest.skip == true || consentRequest.requestedScope?.all { it == "openid" } == true),
            "clientName" to consentRequest.client?.run { clientName?.takeIf { it.isNotBlank() } ?: clientId },
            "scopes" to consentRequest.requestedScope?.filter { it != "openid" }
        )
    }

    @Post
    suspend fun indexSubmit(@Body consentDto: ConsentDto): String {
        val consentRequest = consentService.getConsentRequest(consentDto.consentChallenge)
        return if (consentDto.consent) {
            consentService.accept(consentRequest)
        } else {
            consentService.reject(consentRequest)
        }.toString()
    }

    @Error(exception = ApiException::class)
    fun onHydraApiException(exception: ApiException): HttpResponse<String> {
        return when (exception.code) {
            401, 404 -> HttpResponse.unauthorized<String>().body("INVALID_CONSENT_CHALLENGE")
            410 -> HttpResponse.unauthorized<String>().body("CONSENT_REQUEST_USED")
            else -> HttpResponse.status<Unit>(HttpStatus.valueOf(exception.code.takeIf { it != 0 } ?: 500))
                .body("HYDRA")
        }
    }
}