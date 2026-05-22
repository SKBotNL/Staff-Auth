package net.trueog.staffauth.exception.invite

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.server.exceptions.ExceptionHandler
import jakarta.inject.Singleton

@Singleton
class InvalidInviteExceptionHandler : ExceptionHandler<InvalidInviteException, HttpResponse<*>> {
    override fun handle(request: HttpRequest<*>, exception: InvalidInviteException) =
        HttpResponse.unauthorized<String>().body("INVALID_INVITE")!!
}