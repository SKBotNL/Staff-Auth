package net.trueog.staffauth.dto.setup

import io.micronaut.core.annotation.Introspected
import io.micronaut.serde.annotation.Serdeable
import jakarta.validation.constraints.NotBlank

@Introspected
@Serdeable
data class DetailsDto(
    @param:NotBlank val email: String,
    @param:NotBlank val password: String,
    @param:NotBlank val token: String
)