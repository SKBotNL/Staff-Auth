package net.trueog.staffauth.dto.admin

import io.micronaut.serde.annotation.Serdeable
import net.trueog.staffauth.entity.User
import net.trueog.staffauth.model.Role
import java.util.*

@Serdeable
data class CreateUserDto(val email: String, val role: Role, val minecraftUuid: UUID) {
    fun toEntity() = User(email, role, minecraftUuid, null, null, false)
}