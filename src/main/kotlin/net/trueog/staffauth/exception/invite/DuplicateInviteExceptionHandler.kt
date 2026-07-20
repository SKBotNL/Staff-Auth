package net.trueog.staffauth.exception.invite

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.server.exceptions.ExceptionHandler
import jakarta.inject.Singleton
import net.trueog.staffauth.dto.ErrorDto

@Singleton
class DuplicateInviteExceptionHandler : ExceptionHandler<DuplicateInviteException, HttpResponse<*>> {
    override fun handle(request: HttpRequest<*>, exception: DuplicateInviteException) =
        HttpResponse.status<Unit>(HttpStatus.CONFLICT).body(ErrorDto.Default("DUPLICATE_INVITE"))!!
}