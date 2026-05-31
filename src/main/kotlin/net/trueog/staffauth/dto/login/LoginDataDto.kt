package net.trueog.staffauth.dto.login

import io.micronaut.serde.annotation.Serdeable

@Serdeable
data class LoginDataDto(val skip: Boolean, val redirectUri: String?, val currentStage: String?)