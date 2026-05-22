package net.trueog.staffauth

import io.micronaut.context.annotation.Value
import io.micronaut.context.event.ApplicationEventListener
import io.micronaut.context.event.StartupEvent
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.runBlocking
import net.trueog.staffauth.dto.admin.CreateInviteDto
import net.trueog.staffauth.entity.User
import net.trueog.staffauth.model.Role
import net.trueog.staffauth.repository.InviteRepository
import net.trueog.staffauth.repository.UserRepository
import net.trueog.staffauth.service.InviteService
import org.slf4j.LoggerFactory
import java.net.URI
import java.util.*
import kotlin.system.exitProcess

@Singleton
class InitialConfig(
    private val userRepository: UserRepository,
    private val inviteService: InviteService,
    private val inviteRepository: InviteRepository
) : ApplicationEventListener<StartupEvent> {
    private val log = LoggerFactory.getLogger(InitialConfig::class.java)

    @Value($$"${initialSetup.adminUuid:}")
    private var adminUuid: UUID? = null

    @Value($$"${frontend.host}")
    private lateinit var frontendHost: String

    override fun onApplicationEvent(event: StartupEvent): Unit = runBlocking {
        // If no users exist or if no users are set up
        if (userRepository.count() == 0L || userRepository.findAll()
                .fold(true) { acc, user -> acc && (!user.isSetUp) }
        ) {
            if (adminUuid == null) {
                log.error("Not set up but initialSetup.adminUuid not set or invalid, exiting...")
                exitProcess(1)
            }
            inviteRepository.deleteAll()
            userRepository.deleteAll()
            val user = userRepository.save(User(null, Role.ADMIN, adminUuid!!, null, null, false))
            val invite = inviteService.create(CreateInviteDto(user.id!!))
            log.info("Initial setup, created invite for admin user: ${URI(frontendHost).resolve("/setup?token=${invite.token}")}")
        } else if (adminUuid != null) {
            log.warn("Already set up but initialSetup.adminUuid is still set, can be removed")
        }
    }
}