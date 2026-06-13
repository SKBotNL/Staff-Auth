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
import net.trueog.staffauth.dto.ErrorDto
import net.trueog.staffauth.dto.login.CredentialsDto
import net.trueog.staffauth.dto.login.LoginDataDto
import net.trueog.staffauth.dto.login.MinecraftCheckDto
import net.trueog.staffauth.dto.login.TotpDto
import net.trueog.staffauth.exception.IncorrectTotpCodeException
import net.trueog.staffauth.exception.TooManyRequestsException
import net.trueog.staffauth.exception.login.*
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
    suspend fun credentials(@Body credentialsDto: CredentialsDto, request: HttpRequest<*>) {
        loginService.usernamePassword(
            credentialsDto.loginChallenge,
            credentialsDto.username,
            credentialsDto.password,
            request.remoteAddress.address.hostAddress
        )
    }

    @Post("/minecraftcheck")
    suspend fun minecraftCheck(@Body minecraftCheckDto: MinecraftCheckDto, request: HttpRequest<*>): Boolean {
        return loginService.minecraftCheck(minecraftCheckDto.loginChallenge, request.remoteAddress.address.hostAddress)
    }

    @Post("/totp")
    suspend fun totp(@Body totpDto: TotpDto, request: HttpRequest<*>): String {
        loginService.totp(totpDto.loginChallenge, totpDto.code, request.remoteAddress.address.hostAddress)
        val redirectUrl =
            loginService.accept(totpDto.loginChallenge, totpDto.rememberMe, request.remoteAddress.address.hostAddress)
        return redirectUrl
    }

    @Error(exception = IncorrectUsernameOrPasswordException::class)
    fun onIncorrectUsernameOrPassword(): HttpResponse<ErrorDto> =
        HttpResponse.unauthorized<Unit>().body(ErrorDto.Default("INCORRECT_USERNAME_OR_PASSWORD"))

    @Error(exception = IncorrectTotpCodeException::class)
    fun onIncorrectTotpCode(): HttpResponse<ErrorDto> =
        HttpResponse.unauthorized<Unit>().body(ErrorDto.Default("INCORRECT_TOTP_CODE"))

    @Error(exception = IncorrectLoginStageException::class)
    fun onIncorrectLoginStage(exception: IncorrectLoginStageException): HttpResponse<ErrorDto> =
        HttpResponse.status<Unit>(HttpStatus.CONFLICT).body(
            ErrorDto.Default(
                when (exception.correctLoginStage) {
                    is LoginStage.AwaitingMinecraftCheck -> "MINECRAFT_CHECK"
                    is LoginStage.AwaitingTotp -> "TOTP"
                    is LoginStage.AwaitingAccept -> "ACCEPT"
                }
            )
        )

    @Error(exception = InvalidLoginChallengeException::class)
    fun onInvalidLoginChallenge(): HttpResponse<ErrorDto> =
        HttpResponse.unauthorized<Unit>().body(ErrorDto.Default("INVALID_LOGIN_CHALLENGE"))

    @Error(exception = DifferentIpException::class)
    fun onDifferentIp(exception: DifferentIpException): HttpResponse<ErrorDto> =
        HttpResponse.status<Unit>(HttpStatus.FORBIDDEN).body(ErrorDto.RedirectTo("DIFFERENT_IP", exception.redirectUrl))

    @Error(exception = UnrecoverableException::class)
    fun onUnrecoverable(exception: UnrecoverableException): HttpResponse<ErrorDto> =
        HttpResponse.serverError<Unit>().body(ErrorDto.RedirectTo("UNRECOVERABLE", exception.redirectUrl))


    @Error(exception = StatusException::class)
    fun onGrpcStatusException(exception: StatusException): HttpResponse<ErrorDto> = when (exception.status.code) {
        Status.Code.DEADLINE_EXCEEDED -> HttpResponse.status<Unit>(HttpStatus.GATEWAY_TIMEOUT)
            .body(ErrorDto.Default("MINECRAFT_CHECK_TIMEOUT"))

        Status.Code.UNAVAILABLE -> HttpResponse.status<Unit>(HttpStatus.SERVICE_UNAVAILABLE)
            .body(ErrorDto.Default("MINECRAFT_CHECK_UNAVAILABLE"))

        else -> throw exception
    }

    @Error(exception = ApiException::class)
    fun onHydraApiException(exception: ApiException): HttpResponse<ErrorDto> {
        return when (exception.code) {
            401, 404 -> HttpResponse.unauthorized<Unit>().body(ErrorDto.Default("INVALID_LOGIN_CHALLENGE"))
            410 -> HttpResponse.unauthorized<Unit>().body(ErrorDto.Default("LOGIN_REQUEST_USED"))
            else -> HttpResponse.status<Unit>(HttpStatus.valueOf(exception.code.takeIf { it != 0 } ?: 500))
                .body(ErrorDto.Default("HYDRA"))
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