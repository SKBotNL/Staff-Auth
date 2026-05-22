package net.trueog.staffauth.service.oauth

import com.github.benmanes.caffeine.cache.Caffeine
import dev.samstevens.totp.code.CodeVerifier
import jakarta.inject.Singleton
import net.trueog.staffauth.client.MinecraftClient
import net.trueog.staffauth.exception.IncorrectTotpCodeException
import net.trueog.staffauth.exception.login.IncorrectLoginStageException
import net.trueog.staffauth.exception.login.IncorrectUsernameOrPasswordException
import net.trueog.staffauth.exception.login.InvalidLoginChallengeException
import net.trueog.staffauth.model.LoginStage
import net.trueog.staffauth.repository.UserRepository
import net.trueog.staffauth.service.Argon2idPasswordEncoderService
import proto.IpCheckerGrpcKt
import proto.ipCheckRequest
import sh.ory.hydra.ApiException
import sh.ory.hydra.api.OAuth2Api
import sh.ory.hydra.model.AcceptOAuth2LoginRequest
import java.time.Duration

@Singleton
class LoginService(
    private val oAuth2Api: OAuth2Api,
    private val userRepository: UserRepository,
    private val minecraftClient: MinecraftClient,
    private val passwordEncoderService: Argon2idPasswordEncoderService,
    private val ipCheckerStub: IpCheckerGrpcKt.IpCheckerCoroutineStub,
    private val codeVerifier: CodeVerifier
) {
    val loginStageMap = Caffeine.newBuilder()
        .expireAfterAccess(Duration.ofMinutes(5))
        .build<String, LoginStage>()

    val totpRegex = "^\\d{6}$".toRegex()

    fun getCurrentStage(loginChallenge: String) = when (loginStageMap.getIfPresent(loginChallenge)) {
        is LoginStage.AwaitingMinecraftCheck -> "MINECRAFT_CHECK"
        is LoginStage.AwaitingTotp -> "TOTP"
        is LoginStage.AwaitingAccept -> "ACCEPT"
        null -> {
            try {
                oAuth2Api.getOAuth2LoginRequest(loginChallenge)
                "CREDENTIALS"
            } catch (_: ApiException) {
                throw InvalidLoginChallengeException()
            }
        }
    }

    suspend fun usernamePassword(loginChallenge: String, username: String, password: String) {
        oAuth2Api.getOAuth2LoginRequest(loginChallenge) // Throws sh.ory.hydra.ApiException if invalid
        loginStageMap.getIfPresent(loginChallenge)?.let {
            throw IncorrectLoginStageException(it)
        }

        val minecraftProfileDto =
            minecraftClient.getByUsername(username) ?: throw IncorrectUsernameOrPasswordException()

        val user =
            userRepository.findByMinecraftUuid(minecraftProfileDto.uuid) ?: throw IncorrectUsernameOrPasswordException()
        // Don't let user log in if the account is not yet set up, or is deactivated
        if (!user.isSetUp || user.deactivated) {
            throw IncorrectUsernameOrPasswordException()
        }
        val userId = user.id ?: throw IncorrectUsernameOrPasswordException()

        if (!passwordEncoderService.matches(password, user.passwordHash)) {
            throw IncorrectUsernameOrPasswordException()
        }

        loginStageMap.put(loginChallenge, LoginStage.AwaitingMinecraftCheck(userId))
    }

    suspend fun minecraftCheck(loginChallenge: String, ip: String): Boolean {
        oAuth2Api.getOAuth2LoginRequest(loginChallenge) // Throws sh.ory.hydra.ApiException if invalid
        val loginStage = loginStageMap.getIfPresent(loginChallenge) ?: throw InvalidLoginChallengeException()
        if (loginStage !is LoginStage.AwaitingMinecraftCheck) throw IncorrectLoginStageException(loginStage)

        val uuid = userRepository.findById(loginStage.userId)?.minecraftUuid ?: throw IllegalStateException()

        val reply = ipCheckerStub.checkIp(ipCheckRequest {
            this.uuid = uuid.toString()
        })
        val valid = reply.ip == ip
        if (valid) {
            loginStageMap.put(loginChallenge, LoginStage.AwaitingTotp(loginStage.userId))
        }
        return valid
    }

    suspend fun totp(loginChallenge: String, code: String) {
        oAuth2Api.getOAuth2LoginRequest(loginChallenge) // Throws sh.ory.hydra.ApiException if invalid
        val loginStage = loginStageMap.getIfPresent(loginChallenge) ?: throw InvalidLoginChallengeException()
        if (loginStage !is LoginStage.AwaitingTotp) throw IncorrectLoginStageException(loginStage)

        if (!totpRegex.matches(code)) {
            throw IncorrectTotpCodeException()
        }

        val totpSecret = userRepository.findById(loginStage.userId)?.totpSecret ?: throw IllegalStateException()

        if (!codeVerifier.isValidCode(totpSecret, code)) {
            throw IncorrectTotpCodeException()
        }
        loginStageMap.put(loginChallenge, LoginStage.AwaitingAccept(loginStage.userId))
    }

    suspend fun accept(loginChallenge: String): String {
        val loginRequest =
            oAuth2Api.getOAuth2LoginRequest(loginChallenge) // Throws sh.ory.hydra.ApiException if invalid
        val loginStage = loginStageMap.getIfPresent(loginChallenge) ?: throw InvalidLoginChallengeException()
        if (loginStage !is LoginStage.AwaitingAccept) throw IncorrectLoginStageException(loginStage)

        userRepository.findById(loginStage.userId)
            ?: throw IllegalStateException() // Check if user still exists before we authorize

        val response = oAuth2Api.acceptOAuth2LoginRequest(
            loginRequest.challenge,
            AcceptOAuth2LoginRequest().subject(loginStage.userId.toString()).remember(true).rememberFor(3600)
        )
        loginStageMap.invalidate(loginChallenge)
        return response.redirectTo
    }
}