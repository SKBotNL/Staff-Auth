package net.trueog.staffauth.service

import io.micronaut.security.authentication.Authentication
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.map
import net.trueog.staffauth.client.MinecraftClient
import net.trueog.staffauth.dto.admin.CreateUserDto
import net.trueog.staffauth.dto.admin.UpdateUserDto
import net.trueog.staffauth.dto.admin.UserDto
import net.trueog.staffauth.exception.user.DeactivateSelfException
import net.trueog.staffauth.exception.user.DeleteSelfException
import net.trueog.staffauth.exception.user.DuplicateMinecraftUuidException
import net.trueog.staffauth.exception.user.InvalidMinecraftUuidException
import net.trueog.staffauth.repository.UserRepository

@Singleton
class UserService(private val userRepository: UserRepository, private val minecraftClient: MinecraftClient) {
    fun list() = userRepository.findAll().map {
        val username = minecraftClient.getByUuid(it.minecraftUuid)?.name ?: throw IllegalStateException()
        UserDto.fromEntity(it, username)
    }

    suspend fun get(id: Long) = userRepository.findById(id)?.let {
        val username = minecraftClient.getByUuid(it.minecraftUuid)?.name ?: throw IllegalStateException()
        UserDto.fromEntity(it, username)
    }

    suspend fun create(createUserDto: CreateUserDto): UserDto {
        if (userRepository.findByMinecraftUuid(createUserDto.minecraftUuid) != null) throw DuplicateMinecraftUuidException()
        val user = userRepository.save(createUserDto.toEntity())
        val username = minecraftClient.getByUuid(user.minecraftUuid)?.name ?: throw IllegalStateException()
        return UserDto.fromEntity(user, username)
    }

    suspend fun update(updateUserDto: UpdateUserDto, auth: Authentication): UserDto? {
        val user = userRepository.findById(updateUserDto.id) ?: return null
        if (updateUserDto.minecraftUuid != null && userRepository.findByMinecraftUuid(updateUserDto.minecraftUuid)
                ?.let { it.id != user.id } == true
        ) throw DuplicateMinecraftUuidException()
        if (user.uuid.toString() == auth.attributes["sub"]) {
            if (updateUserDto.deactivated == true || updateUserDto.role != user.role) {
                throw DeactivateSelfException()
            }
        }

        val username = minecraftClient.getByUuid(updateUserDto.minecraftUuid ?: user.minecraftUuid)?.name
            ?: throw InvalidMinecraftUuidException()
        val updatedUser = userRepository.update(
            user.copy(
                email = updateUserDto.email ?: user.email,
                role = updateUserDto.role ?: user.role,
                minecraftUuid = updateUserDto.minecraftUuid ?: user.minecraftUuid,
                deactivated = updateUserDto.deactivated ?: user.deactivated
            )
        )
        return UserDto.fromEntity(updatedUser, username)
    }

    suspend fun delete(id: Long, auth: Authentication): Int {
        val user = userRepository.findById(id) ?: throw IllegalStateException()
        if (user.uuid.toString() == auth.attributes["sub"]) {
            throw DeleteSelfException()
        }
        return userRepository.deleteById(id)
    }
}