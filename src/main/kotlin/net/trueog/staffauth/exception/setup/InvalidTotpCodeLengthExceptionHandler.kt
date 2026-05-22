package net.trueog.staffauth.exception.setup

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.server.exceptions.ExceptionHandler
import jakarta.inject.Singleton

@Singleton
class InvalidTotpCodeLengthExceptionHandler : ExceptionHandler<InvalidTotpCodeLengthException, HttpResponse<*>> {
    override fun handle(request: HttpRequest<*>, exception: InvalidTotpCodeLengthException) =
        HttpResponse.badRequest("INVALID_TOTP_CODE_LENGTH")!!
}