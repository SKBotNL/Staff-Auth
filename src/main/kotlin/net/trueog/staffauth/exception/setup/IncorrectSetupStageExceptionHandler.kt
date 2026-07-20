package net.trueog.staffauth.exception.setup

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.server.exceptions.ExceptionHandler
import jakarta.inject.Singleton
import net.trueog.staffauth.dto.ErrorDto

@Singleton
class IncorrectSetupStageExceptionHandler : ExceptionHandler<IncorrectSetupStageException, HttpResponse<*>> {
    override fun handle(request: HttpRequest<*>, exception: IncorrectSetupStageException) =
        HttpResponse.badRequest(ErrorDto.Default("INCORRECT_SETUP_STAGE"))!!
}