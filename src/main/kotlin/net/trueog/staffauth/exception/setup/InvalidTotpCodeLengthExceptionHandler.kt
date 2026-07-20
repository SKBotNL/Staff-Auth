package net.trueog.staffauth.exception.setup

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.server.exceptions.ExceptionHandler
import jakarta.inject.Singleton
import net.trueog.staffauth.dto.ErrorDto

@Singleton
class InvalidTotpCodeLengthExceptionHandler : ExceptionHandler<InvalidTotpCodeLengthException, HttpResponse<*>> {
    override fun handle(request: HttpRequest<*>, exception: InvalidTotpCodeLengthException) =
        HttpResponse.badRequest(ErrorDto.Default("INVALID_TOTP_CODE_LENGTH"))!!
}