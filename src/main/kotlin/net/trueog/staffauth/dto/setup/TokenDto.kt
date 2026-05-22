package net.trueog.staffauth.dto.setup

import io.micronaut.serde.annotation.Serdeable

@Serdeable
data class TokenDto(val token: String)
