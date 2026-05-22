package net.trueog.staffauth.dto.consent

import io.micronaut.serde.annotation.Serdeable

@Serdeable
data class ConsentDto(val consent: Boolean, val consentChallenge: String)
