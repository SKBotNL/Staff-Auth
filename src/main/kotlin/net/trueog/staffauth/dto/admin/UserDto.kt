package net.trueog.staffauth.dto.admin

import io.micronaut.serde.annotation.Serdeable
import net.trueog.staffauth.entity.User
import net.trueog.staffauth.model.Role
import java.util.*

@Serdeable
data class UserDto(
    val id: Long,
    val uuid: UUID,
    val username: String?,
    val email: String?,
    val role: Role,
    val minecraftUuid: UUID,
    val deactivated: Boolean,
    val setUp: Boolean
) {
    companion object {
        fun fromEntity(entity: User, username: String?) = UserDto(
            entity.id!!,
            entity.uuid!!,
            username,
            entity.email,
            entity.role,
            entity.minecraftUuid,
            entity.deactivated,
            entity.isSetUp
        )
    }
}
