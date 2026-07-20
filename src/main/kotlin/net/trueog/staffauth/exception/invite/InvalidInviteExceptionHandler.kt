package net.trueog.staffauth.exception.invite

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.server.exceptions.ExceptionHandler
import jakarta.inject.Singleton
import net.trueog.staffauth.dto.ErrorDto

@Singleton
class InvalidInviteExceptionHandler : ExceptionHandler<InvalidInviteException, HttpResponse<*>> {
    override fun handle(request: HttpRequest<*>, exception: InvalidInviteException) =
        HttpResponse.unauthorized<Unit>().body(ErrorDto.Default("INVALID_INVITE"))!!
}