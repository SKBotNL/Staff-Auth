package net.trueog.staffauth.dto.admin

import io.micronaut.serde.annotation.Serdeable

@Serdeable
data class CreateInviteDto(val invitedUserId: Long)