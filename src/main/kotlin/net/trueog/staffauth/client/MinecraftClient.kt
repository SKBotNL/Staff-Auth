package net.trueog.staffauth.client

import io.micronaut.cache.annotation.CacheConfig
import io.micronaut.cache.annotation.Cacheable
import io.micronaut.http.annotation.Get
import io.micronaut.http.client.annotation.Client
import net.trueog.staffauth.client.dto.MinecraftProfileDto
import java.util.*

@CacheConfig("minecraft-profiles")
@Client("https://api.mojang.com/")
interface MinecraftClient {
    @Cacheable
    @Get("users/profiles/minecraft/{username}")
    suspend fun getByUsername(username: String): MinecraftProfileDto?

    @Cacheable
    @Get("user/profile/{uuid}")
    suspend fun getByUuid(uuid: UUID): MinecraftProfileDto?
}