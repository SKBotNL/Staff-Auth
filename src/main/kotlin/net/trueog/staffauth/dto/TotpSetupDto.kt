package net.trueog.staffauth.dto

import io.micronaut.serde.annotation.Serdeable

@Serdeable
data class TotpSetupDto(val secret: String, val qrCode: String)