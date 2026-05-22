package net.trueog.staffauth.dto.admin

import io.micronaut.serde.annotation.Serdeable
import net.trueog.staffauth.model.Role
import java.util.*

@Serdeable
data class UpdateUserDto(
    val id: Long,
    val email: String?,
    val role: Role?,
    val minecraftUuid: UUID?,
    val deactivated: Boolean?
)