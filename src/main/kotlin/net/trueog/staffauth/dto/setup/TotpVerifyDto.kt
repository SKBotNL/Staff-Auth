package net.trueog.staffauth.dto.setup

import io.micronaut.serde.annotation.Serdeable

@Serdeable
data class TotpVerifyDto(val token: String, val code: String)