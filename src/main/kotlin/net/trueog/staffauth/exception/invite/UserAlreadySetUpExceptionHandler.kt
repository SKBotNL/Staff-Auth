package net.trueog.staffauth.exception.invite

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.server.exceptions.ExceptionHandler
import jakarta.inject.Singleton

@Singleton
class UserAlreadySetUpExceptionHandler : ExceptionHandler<UserAlreadySetUpException, HttpResponse<*>> {
    override fun handle(request: HttpRequest<*>, exception: UserAlreadySetUpException) =
        HttpResponse.badRequest("USER_ALREADY_SET_UP")!!
}