package net.trueog.staffauth.repository

import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.r2dbc.annotation.R2dbcRepository
import io.micronaut.data.repository.kotlin.CoroutineCrudRepository
import kotlinx.coroutines.flow.Flow
import net.trueog.staffauth.entity.Invite

@R2dbcRepository(dialect = Dialect.POSTGRES)
abstract class InviteRepository : CoroutineCrudRepository<Invite, Long> {
    abstract fun findAllOrderById(): Flow<Invite>
    abstract suspend fun findByToken(token: String): Invite?
    abstract suspend fun findByInvitedUserId(invitedUserId: Long): Invite?
    abstract suspend fun deleteByToken(token: String): Int
}