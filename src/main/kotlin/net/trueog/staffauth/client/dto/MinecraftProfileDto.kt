package net.trueog.staffauth.client.dto

import io.micronaut.serde.annotation.Serdeable
import java.util.*

val uuidRegex = """(\w{8})(\w{4})(\w{4})(\w{4})(\w{12})""".toRegex()

@Serdeable
data class MinecraftProfileDto(val name: String, val id: String) {
    var uuid: UUID = UUID.fromString(uuidRegex.replace(id, $$"$1-$2-$3-$4-$5"))
}