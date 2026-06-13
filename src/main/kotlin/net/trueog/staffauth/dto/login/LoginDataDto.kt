package net.trueog.staffauth.dto.login

import io.micronaut.serde.annotation.Serdeable

@Serdeable
data class LoginDataDto(val skip: Boolean, val redirectUrl: String?, val currentStage: String?)