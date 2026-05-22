package net.trueog.staffauth.exception.user

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.server.exceptions.ExceptionHandler
import jakarta.inject.Singleton

@Singleton
class UserNotFoundExceptionHandler : ExceptionHandler<UserNotFoundException, HttpResponse<*>> {
    override fun handle(request: HttpRequest<*>, exception: UserNotFoundException) =
        HttpResponse.notFound("USER_NOT_FOUND")!!
}