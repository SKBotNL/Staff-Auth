package net.trueog.staffauth.dto.admin

import io.micronaut.serde.annotation.Serdeable
import net.trueog.staffauth.entity.Invite

@Serdeable
data class InviteDto(val id: Long, val token: String, val invitedUserId: Long) {
    companion object {
        fun fromEntity(entity: Invite) = InviteDto(
            entity.id!!,
            entity.token,
            entity.invitedUserId
        )
    }
}