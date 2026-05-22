package net.trueog.staffauth.exception

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.server.exceptions.ExceptionHandler
import jakarta.inject.Singleton

@Singleton
class TooManyRequestsExceptionHandler : ExceptionHandler<TooManyRequestsException, HttpResponse<*>> {
    override fun handle(request: HttpRequest<*>, exception: TooManyRequestsException) =
        HttpResponse.status<Unit>(HttpStatus.TOO_MANY_REQUESTS)!!
}