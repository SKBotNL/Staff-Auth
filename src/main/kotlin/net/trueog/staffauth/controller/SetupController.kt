package net.trueog.staffauth.controller

import io.grpc.Status
import io.grpc.StatusException
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.*
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import jakarta.validation.Valid
import net.trueog.staffauth.dto.ErrorDto
import net.trueog.staffauth.dto.TotpSetupDto
import net.trueog.staffauth.dto.setup.DetailsDto
import net.trueog.staffauth.dto.setup.TokenDto
import net.trueog.staffauth.dto.setup.TotpVerifyDto
import net.trueog.staffauth.exception.IncorrectTotpCodeException
import net.trueog.staffauth.exception.setup.DeactivatedException
import net.trueog.staffauth.exception.setup.IncorrectSetupStageException
import net.trueog.staffauth.model.SetupStage
import net.trueog.staffauth.service.SetupService

@Controller("/setup")
@Secured(SecurityRule.IS_ANONYMOUS)
open class SetupController(
    private val setupService: SetupService
) {
    @Get("/currentStage")
    suspend fun currentStage(@QueryValue("token") token: String): String = setupService.getCurrentStage(token)

    @Post("/details")
    open suspend fun details(@Valid @Body detailsDto: DetailsDto) {
        return setupService.details(detailsDto.token, detailsDto)
    }

    @Post("/minecraftcheck")
    suspend fun minecraftCheck(@Body tokenDto: TokenDto, request: HttpRequest<*>): Boolean {
        return setupService.minecraftCheck(tokenDto.token, request.remoteAddress.address.hostAddress)
    }

    @Post("/totpsetup")
    suspend fun totpSetup(@Body tokenDto: TokenDto): TotpSetupDto {
        return setupService.generateTotp(tokenDto.token)
    }

    @Post("/totpverify")
    suspend fun totpVerify(@Body totpVerifyDto: TotpVerifyDto) {
        setupService.verifyTotp(totpVerifyDto.token, totpVerifyDto.code)
        setupService.finalize(totpVerifyDto.token)
    }

    @Error(exception = DeactivatedException::class)
    fun onDeactivated(): HttpResponse<ErrorDto> =
        HttpResponse.unauthorized<Unit>().body(ErrorDto.Default("DEACTIVATED"))

    @Error(exception = IncorrectTotpCodeException::class)
    fun onIncorrectTotpCode(): HttpResponse<ErrorDto> =
        HttpResponse.unauthorized<Unit>().body(ErrorDto.Default("INCORRECT_TOTP_CODE"))

    @Error(exception = StatusException::class)
    fun onGrpcStatusException(exception: StatusException): HttpResponse<ErrorDto> = when (exception.status.code) {
        Status.Code.DEADLINE_EXCEEDED -> HttpResponse.status<Unit>(HttpStatus.GATEWAY_TIMEOUT)
            .body(ErrorDto.Default("MINECRAFT_CHECK_TIMEOUT"))

        Status.Code.UNAVAILABLE -> HttpResponse.status<Unit>(HttpStatus.SERVICE_UNAVAILABLE)
            .body(ErrorDto.Default("MINECRAFT_CHECK_UNAVAILABLE"))

        else -> throw exception
    }

    @Error(exception = IncorrectSetupStageException::class)
    fun onIncorrectSetupStage(exception: IncorrectSetupStageException): HttpResponse<ErrorDto> =
        HttpResponse.status<Unit>(HttpStatus.CONFLICT).body(
            ErrorDto.Default(
                when (exception.correctSetupStage) {
                    is SetupStage.AwaitingMinecraftCheck -> "MINECRAFT_CHECK"
                    is SetupStage.AwaitingTotp -> "TOTP"
                    is SetupStage.AwaitingTotpVerify -> "TOTP"
                    is SetupStage.AwaitingFinalize -> "FINALIZE"
                }
            )
        )
}