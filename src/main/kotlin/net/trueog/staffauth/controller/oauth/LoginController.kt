package net.trueog.staffauth.controller.oauth

import io.grpc.Status
import io.grpc.StatusException
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.*
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import net.trueog.staffauth.dto.login.CredentialsDto
import net.trueog.staffauth.dto.login.LoginDataDto
import net.trueog.staffauth.dto.login.MinecraftCheckDto
import net.trueog.staffauth.dto.login.TotpDto
import net.trueog.staffauth.exception.IncorrectTotpCodeException
import net.trueog.staffauth.exception.TooManyRequestsException
import net.trueog.staffauth.exception.login.IncorrectLoginStageException
import net.trueog.staffauth.exception.login.IncorrectUsernameOrPasswordException
import net.trueog.staffauth.exception.login.InvalidLoginChallengeException
import net.trueog.staffauth.model.LoginStage
import net.trueog.staffauth.service.oauth.LoginService
import sh.ory.hydra.ApiException

@Controller("/login")
@Secured(SecurityRule.IS_ANONYMOUS)
class LoginController(
    private val loginService: LoginService,
) {
    @Get("/data")
    fun loginData(@QueryValue("login_challenge") loginChallenge: String): LoginDataDto =
        loginService.getLoginData(loginChallenge)

    @Post("/credentials")
    suspend fun credentials(@Body credentialsDto: CredentialsDto) {
        loginService.usernamePassword(credentialsDto.loginChallenge, credentialsDto.username, credentialsDto.password)
    }

    @Post("/minecraftcheck")
    suspend fun minecraftCheck(@Body minecraftCheckDto: MinecraftCheckDto, request: HttpRequest<*>): Boolean {
        return loginService.minecraftCheck(minecraftCheckDto.loginChallenge, request.remoteAddress.address.hostAddress)
    }

    @Post("/totp")
    suspend fun totp(@Body totpDto: TotpDto): String {
        loginService.totp(totpDto.loginChallenge, totpDto.code)
        val redirectUri = loginService.accept(totpDto.loginChallenge, totpDto.rememberMe)
        return redirectUri
    }

    @Error(exception = IncorrectUsernameOrPasswordException::class)
    fun onIncorrectUsernameOrPassword(): HttpResponse<String> =
        HttpResponse.unauthorized<String>().body("INCORRECT_USERNAME_OR_PASSWORD")

    @Error(exception = IncorrectTotpCodeException::class)
    fun onIncorrectTotpCode(): HttpResponse<String> = HttpResponse.unauthorized<String>().body("INCORRECT_TOTP_CODE")

    @Error(exception = IncorrectLoginStageException::class)
    fun onIncorrectLoginStage(exception: IncorrectLoginStageException): HttpResponse<String> =
        HttpResponse.status<Unit>(HttpStatus.CONFLICT).body(
            when (exception.correctLoginStage) {
                is LoginStage.AwaitingMinecraftCheck -> "MINECRAFT_CHECK"
                is LoginStage.AwaitingTotp -> "TOTP"
                is LoginStage.AwaitingAccept -> "ACCEPT"
            }
        )

    @Error(exception = InvalidLoginChallengeException::class)
    fun onInvalidLoginChallenge(): HttpResponse<String> =
        HttpResponse.unauthorized<String>().body("INVALID_LOGIN_CHALLENGE")

    @Error(exception = StatusException::class)
    fun onGrpcStatusException(exception: StatusException): HttpResponse<String> = when (exception.status.code) {
        Status.Code.DEADLINE_EXCEEDED -> HttpResponse.status<Unit>(HttpStatus.GATEWAY_TIMEOUT)
            .body("MINECRAFT_CHECK_TIMEOUT")

        Status.Code.UNAVAILABLE -> HttpResponse.status<Unit>(HttpStatus.SERVICE_UNAVAILABLE)
            .body("MINECRAFT_CHECK_UNAVAILABLE")

        else -> throw exception
    }

    @Error(exception = ApiException::class)
    fun onHydraApiException(exception: ApiException): HttpResponse<String> {
        return when (exception.code) {
            401, 404 -> HttpResponse.unauthorized<String>().body("INVALID_LOGIN_CHALLENGE")
            410 -> HttpResponse.unauthorized<String>().body("LOGIN_REQUEST_USED")
            else -> HttpResponse.status<Unit>(HttpStatus.valueOf(exception.code.takeIf { it != 0 } ?: 500))
                .body("HYDRA")
        }
    }

    @Error(exception = HttpClientResponseException::class)
    fun onHttpClientResponseException(exception: HttpClientResponseException) {
        when (exception.status) {
            HttpStatus.NO_CONTENT, HttpStatus.BAD_REQUEST -> throw IncorrectUsernameOrPasswordException()
            HttpStatus.TOO_MANY_REQUESTS -> throw TooManyRequestsException()
            else -> throw exception
        }
    }
}