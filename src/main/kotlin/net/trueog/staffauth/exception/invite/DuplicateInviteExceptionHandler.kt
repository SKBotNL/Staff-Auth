package net.trueog.staffauth.exception.invite

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.server.exceptions.ExceptionHandler
import jakarta.inject.Singleton

@Singleton
class DuplicateInviteExceptionHandler : ExceptionHandler<DuplicateInviteException, HttpResponse<*>> {
    override fun handle(request: HttpRequest<*>, exception: DuplicateInviteException) =
        HttpResponse.status<Unit>(HttpStatus.CONFLICT).body("DUPLICATE_INVITE")!!
}