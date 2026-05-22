package net.trueog.staffauth.client

import io.micronaut.http.annotation.Get
import io.micronaut.http.client.annotation.Client
import net.trueog.staffauth.client.dto.MinecraftProfileDto
import java.util.*

@Client("https://api.mojang.com/")
interface MinecraftClient {
    @Get("users/profiles/minecraft/{username}")
    suspend fun getByUsername(username: String): MinecraftProfileDto?

    @Get("user/profile/{uuid}")
    suspend fun getByUuid(uuid: UUID): MinecraftProfileDto?
}