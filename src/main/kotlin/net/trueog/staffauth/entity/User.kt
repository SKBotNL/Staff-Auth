package net.trueog.staffauth.entity

import io.micronaut.data.annotation.*
import io.micronaut.data.model.DataType
import net.trueog.staffauth.model.Role
import java.util.*

@MappedEntity("users")
data class User(
    var email: String?,
    @field:TypeDef(type = DataType.STRING)
    var role: Role,
    var minecraftUuid: UUID,
    var passwordHash: String?,
    var totpSecret: String?,
    var deactivated: Boolean,
    @field:Id
    @field:GeneratedValue(GeneratedValue.Type.AUTO)
    var id: Long? = null,
    @field:GeneratedValue(GeneratedValue.Type.AUTO)
    var uuid: UUID? = null
) {
    @Transient
    val isSetUp = this.email != null && this.passwordHash != null && this.totpSecret != null
}
