package net.trueog.staffauth.dto.login

import io.micronaut.serde.annotation.Serdeable

@Serdeable
data class MinecraftCheckDto(val loginChallenge: String)
