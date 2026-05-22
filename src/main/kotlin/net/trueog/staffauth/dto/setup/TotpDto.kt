package net.trueog.staffauth.dto.setup

import io.micronaut.serde.annotation.Serdeable

@Serdeable
data class TotpDto(val secret: String, val qrCode: String)
