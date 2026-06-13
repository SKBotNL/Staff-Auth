package net.trueog.staffauth.dto

import io.micronaut.serde.annotation.Serdeable

sealed class ErrorDto(open val message: String) {
    @Serdeable
    data class Default(override val message: String) : ErrorDto(message)

    @Serdeable
    data class RedirectTo(override val message: String, val redirectUrl: String) : ErrorDto(message)
}
