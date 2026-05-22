package net.trueog.staffauth.controller.admin

import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.*
import io.micronaut.security.annotation.Secured
import kotlinx.coroutines.flow.Flow
import net.trueog.staffauth.dto.admin.CreateInviteDto
import net.trueog.staffauth.dto.admin.InviteDto
import net.trueog.staffauth.service.InviteService

@Controller("/invite")
@Secured("ADMIN")
class InviteController(private val inviteService: InviteService) {
    @Get
    fun getAll(): Flow<InviteDto> = inviteService.list()

    @Get("/{id}")
    suspend fun getById(@PathVariable id: Long): HttpResponse<InviteDto> = inviteService.get(id)?.let {
        HttpResponse.ok(it)
    } ?: HttpResponse.notFound()

    @Post
    @Status(HttpStatus.CREATED)
    suspend fun create(@Body createInviteDto: CreateInviteDto): InviteDto = inviteService.create(createInviteDto)

    @Delete("/{id}")
    suspend fun delete(@PathVariable id: Long): HttpResponse<Unit> =
        if (inviteService.delete(id) > 0) HttpResponse.noContent() else HttpResponse.notFound()
}