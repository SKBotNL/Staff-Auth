package net.trueog.staffauth.dto.login

import io.micronaut.serde.annotation.Serdeable

@Serdeable
data class CredentialsDto(val username: String, val password: String, val loginChallenge: String)