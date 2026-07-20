package net.trueog.staffauth.exception.invite

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.server.exceptions.ExceptionHandler
import jakarta.inject.Singleton
import net.trueog.staffauth.dto.ErrorDto

@Singleton
class UserAlreadySetUpExceptionHandler : ExceptionHandler<UserAlreadySetUpException, HttpResponse<*>> {
    override fun handle(request: HttpRequest<*>, exception: UserAlreadySetUpException) =
        HttpResponse.badRequest(ErrorDto.Default("USER_ALREADY_SET_UP"))!!
}