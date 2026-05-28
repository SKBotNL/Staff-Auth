package net.trueog.staffauth.repository

import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.r2dbc.annotation.R2dbcRepository
import io.micronaut.data.repository.kotlin.CoroutineCrudRepository
import net.trueog.staffauth.entity.User
import java.util.*

@R2dbcRepository(dialect = Dialect.POSTGRES)
abstract class UserRepository : CoroutineCrudRepository<User, Long> {
    abstract suspend fun findByUuid(uuid: UUID): User?
    abstract suspend fun findByMinecraftUuid(minecraftUuid: UUID): User?
}