package net.trueog.staffauth.dto.setup

import io.micronaut.core.annotation.Introspected
import io.micronaut.serde.annotation.Serdeable
import jakarta.validation.constraints.NotBlank

@Introspected
@Serdeable
data class DetailsDto(
    @field:NotBlank val email: String,
    @field:NotBlank val password: String,
    @field:NotBlank val token: String
)