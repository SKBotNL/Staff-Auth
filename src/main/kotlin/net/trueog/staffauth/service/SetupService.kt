package net.trueog.staffauth.service

import com.github.benmanes.caffeine.cache.Caffeine
import dev.samstevens.totp.secret.SecretGenerator
import jakarta.inject.Singleton
import net.trueog.staffauth.client.MinecraftClient
import net.trueog.staffauth.dto.TotpSetupDto
import net.trueog.staffauth.dto.setup.DetailsDto
import net.trueog.staffauth.exception.IncorrectTotpCodeException
import net.trueog.staffauth.exception.invite.InvalidInviteException
import net.trueog.staffauth.exception.setup.DeactivatedException
import net.trueog.staffauth.exception.setup.IncorrectSetupStageException
import net.trueog.staffauth.exception.setup.InvalidTotpCodeLengthException
import net.trueog.staffauth.model.SetupStage
import net.trueog.staffauth.repository.InviteRepository
import net.trueog.staffauth.repository.UserRepository
import proto.IpCheckerGrpcKt
import proto.ipCheckRequest
import java.time.Duration

@Singleton
class SetupService(
    private val userRepository: UserRepository,
    private val inviteRepository: InviteRepository,
    private val passwordEncoderService: Argon2idPasswordEncoderService,
    private val ipCheckerStub: IpCheckerGrpcKt.IpCheckerCoroutineStub,
    private val totpService: TotpService,
    private val secretGenerator: SecretGenerator,
    private val minecraftClient: MinecraftClient
) {
    /** Maps setup token to [SetupStage]. */
    val setupStageMap = Caffeine.newBuilder().expireAfterAccess(Duration.ofMinutes(20)).build<String, SetupStage>()

    suspend fun getCurrentStage(token: String) = when (setupStageMap.getIfPresent(token)) {
        is SetupStage.AwaitingMinecraftCheck -> "MINECRAFT_CHECK"
        is SetupStage.AwaitingTotp -> "TOTP"
        is SetupStage.AwaitingTotpVerify -> "TOTP"
        is SetupStage.AwaitingFinalize -> "FINALIZE"
        null -> {
            inviteRepository.findByToken(token) ?: throw InvalidInviteException()
            "DETAILS"
        }
    }

    suspend fun details(token: String, detailsDto: DetailsDto) {
        setupStageMap.getIfPresent(token)?.let {
            throw IncorrectSetupStageException(it)
        }

        val invite = inviteRepository.findByToken(token) ?: throw InvalidInviteException()

        val user = userRepository.findById(invite.invitedUserId) ?: throw InvalidInviteException()
        // Don't let user set up their account if the account is deactivated
        if (user.deactivated) {
            throw DeactivatedException()
        }

        val details = SetupStage.AwaitingMinecraftCheck(
            invite.invitedUserId, SetupStage.Details(
                detailsDto.email,
                passwordEncoderService.encode(
                    detailsDto.password
                )!!
            )
        )
        setupStageMap.put(token, details)
    }

    suspend fun minecraftCheck(token: String, ip: String): Boolean {
        val setupStage = setupStageMap.getIfPresent(token) ?: throw InvalidInviteException()
        if (setupStage !is SetupStage.AwaitingMinecraftCheck) throw IncorrectSetupStageException(setupStage)

        // If the user can't be found, that must mean the user was deleted, and as such, the invite is invalid
        val user = userRepository.findById(setupStage.userId) ?: throw InvalidInviteException()

        val reply = ipCheckerStub.checkIp(ipCheckRequest {
            this.uuid = user.minecraftUuid.toString()
        })
        val valid = reply.ip == ip
        if (valid) {
            setupStageMap.put(token, SetupStage.AwaitingTotp(setupStage.userId, setupStage.details))
        }
        return valid
    }

    suspend fun generateTotp(token: String): TotpSetupDto {
        val setupStage = setupStageMap.getIfPresent(token) ?: throw InvalidInviteException()
        val (secret, userId, passwordHash) = if (setupStage !is SetupStage.AwaitingTotp) {
            if (setupStage is SetupStage.AwaitingTotpVerify) {
                Triple(setupStage.totpSecret, setupStage.userId, setupStage.details)
            } else {
                throw IncorrectSetupStageException(setupStage)
            }
        } else {
            Triple(secretGenerator.generate(), setupStage.userId, setupStage.details)
        }

        // If the user can't be found, that must mean the user was deleted, and as such, the invite is invalid
        val minecraftUuid = userRepository.findById(userId)?.minecraftUuid ?: throw InvalidInviteException()
        val minecraftProfileDto = minecraftClient.getByUuid(minecraftUuid)

        val (_, qrCode) = totpService.generateTotp(minecraftProfileDto?.name ?: throw IllegalStateException())

        setupStageMap.put(token, SetupStage.AwaitingTotpVerify(userId, passwordHash, secret))

        return TotpSetupDto(secret, qrCode)
    }

    fun verifyTotp(token: String, code: String) {
        val setupStage = setupStageMap.getIfPresent(token) ?: throw InvalidInviteException()
        if (setupStage !is SetupStage.AwaitingTotpVerify) throw IncorrectSetupStageException(setupStage)

        if (code.length != 6) {
            throw InvalidTotpCodeLengthException()
        }

        if (!totpService.isValid(setupStage.totpSecret, code)) {
            throw IncorrectTotpCodeException()
        }

        setupStageMap.put(
            token,
            SetupStage.AwaitingFinalize(setupStage.userId, setupStage.details, setupStage.totpSecret)
        )
    }

    suspend fun finalize(token: String) {
        val setupStage = setupStageMap.getIfPresent(token) ?: throw InvalidInviteException()
        if (setupStage !is SetupStage.AwaitingFinalize) throw IncorrectSetupStageException(setupStage)

        // If the user can't be found, that must mean the user was deleted, and as such, the invite is invalid
        val user = userRepository.findById(setupStage.userId) ?: throw InvalidInviteException()
        val setUpUser = user.copy(
            email = setupStage.details.email,
            passwordHash = setupStage.details.passwordHash,
            totpSecret = setupStage.totpSecret
        )
        userRepository.update(setUpUser)
        inviteRepository.deleteByToken(token)
        setupStageMap.invalidate(token)
    }
}