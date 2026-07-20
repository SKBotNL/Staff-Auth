package net.trueog.staffauth.exception.setup

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.server.exceptions.ExceptionHandler
import jakarta.inject.Singleton
import net.trueog.staffauth.dto.ErrorDto

@Singleton
class InvalidPasswordExceptionHandler : ExceptionHandler<InvalidPasswordException, HttpResponse<*>> {
    override fun handle(request: HttpRequest<*>, exception: InvalidPasswordException) =
        HttpResponse.badRequest(ErrorDto.Default("INVALID_PASSWORD"))!!
}