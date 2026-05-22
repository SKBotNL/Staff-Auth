package net.trueog.staffauth.service

import jakarta.inject.Singleton
import kotlinx.coroutines.flow.map
import net.trueog.staffauth.dto.admin.CreateInviteDto
import net.trueog.staffauth.dto.admin.InviteDto
import net.trueog.staffauth.entity.Invite
import net.trueog.staffauth.exception.invite.DuplicateInviteException
import net.trueog.staffauth.exception.invite.UserAlreadySetUpException
import net.trueog.staffauth.exception.user.UserNotFoundException
import net.trueog.staffauth.repository.InviteRepository
import net.trueog.staffauth.repository.UserRepository

@Singleton
class InviteService(
    private val inviteRepository: InviteRepository,
    private val userRepository: UserRepository,
    private val tokenGeneratorService: TokenGeneratorService
) {
    fun list() = inviteRepository.findAll().map { InviteDto.fromEntity(it) }

    suspend fun get(id: Long) = inviteRepository.findById(id)?.let {
        InviteDto.fromEntity(it)
    }

    suspend fun getByToken(token: String) = inviteRepository.findByToken(token)?.let {
        InviteDto.fromEntity(it)
    }

    suspend fun create(createInviteDto: CreateInviteDto): InviteDto {
        val user = userRepository.findById(createInviteDto.invitedUserId) ?: throw UserNotFoundException()
        if (user.isSetUp) {
            throw UserAlreadySetUpException()
        }
        if (inviteRepository.findByInvitedUserId(user.id!!) != null) throw DuplicateInviteException()

        val token = tokenGeneratorService.generateToken(32)
        val invite = Invite(
            token.toHexString(),
            createInviteDto.invitedUserId
        )
        return InviteDto.fromEntity(inviteRepository.save(invite))
    }

    suspend fun delete(id: Long) = inviteRepository.deleteById(id)
}