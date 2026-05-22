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
import net.trueog.staffauth.dto.setup.DetailsDto
import net.trueog.staffauth.dto.setup.TokenDto
import net.trueog.staffauth.dto.setup.TotpDto
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
    suspend fun totpSetup(@Body tokenDto: TokenDto): TotpDto {
        return setupService.generateTotp(tokenDto.token)
    }

    @Post("/totpverify")
    suspend fun totpVerify(@Body totpVerifyDto: TotpVerifyDto) {
        setupService.verifyTotp(totpVerifyDto.token, totpVerifyDto.code)
        setupService.finalize(totpVerifyDto.token)
    }

    @Error(exception = DeactivatedException::class)
    fun onDeactivated(): HttpResponse<String> = HttpResponse.unauthorized<String>().body("DEACTIVATED")

    @Error(exception = IncorrectTotpCodeException::class)
    fun onIncorrectTotpCode(): HttpResponse<String> = HttpResponse.unauthorized<String>().body("INCORRECT_TOTP_CODE")

    @Error(exception = StatusException::class)
    fun onGrpcStatusException(exception: StatusException): HttpResponse<String> {
        if (exception.status != Status.DEADLINE_EXCEEDED) throw exception
        return HttpResponse.status<Unit>(HttpStatus.GATEWAY_TIMEOUT).body("MINECRAFT_CHECK_TIMEOUT")
    }

    @Error(exception = IncorrectSetupStageException::class)
    fun onIncorrectSetupStage(exception: IncorrectSetupStageException): HttpResponse<String> =
        HttpResponse.status<Unit>(HttpStatus.CONFLICT).body(
            when (exception.correctSetupStage) {
                is SetupStage.AwaitingMinecraftCheck -> "MINECRAFT_CHECK"
                is SetupStage.AwaitingTotp -> "TOTP"
                is SetupStage.AwaitingTotpVerify -> "TOTP"
                is SetupStage.AwaitingFinalize -> "FINALIZE"
            }
        )
}