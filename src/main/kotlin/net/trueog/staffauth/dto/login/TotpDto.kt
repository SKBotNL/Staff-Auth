package net.trueog.staffauth.dto.login

import io.micronaut.serde.annotation.Serdeable

@Serdeable
data class TotpDto(val code: String, val loginChallenge: String)