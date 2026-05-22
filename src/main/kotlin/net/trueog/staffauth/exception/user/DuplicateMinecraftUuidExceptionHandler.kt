package net.trueog.staffauth.exception.user

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.server.exceptions.ExceptionHandler
import jakarta.inject.Singleton

@Singleton
class DuplicateMinecraftUuidExceptionHandler : ExceptionHandler<DuplicateMinecraftUuidException, HttpResponse<*>> {
    override fun handle(request: HttpRequest<*>, exception: DuplicateMinecraftUuidException) =
        HttpResponse.status<Unit>(
            HttpStatus.CONFLICT
        ).body("DUPLICATE_MINECRAFT_UUID")!!
}