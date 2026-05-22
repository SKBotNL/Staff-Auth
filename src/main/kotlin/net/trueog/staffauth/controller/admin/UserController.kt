package net.trueog.staffauth.controller.admin

import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.*
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.Authentication
import kotlinx.coroutines.flow.Flow
import net.trueog.staffauth.dto.admin.CreateUserDto
import net.trueog.staffauth.dto.admin.UpdateUserDto
import net.trueog.staffauth.dto.admin.UserDto
import net.trueog.staffauth.exception.user.DeactivateSelfException
import net.trueog.staffauth.service.UserService

@Controller("/user")
@Secured("ADMIN")
class UserController(private val userService: UserService) {
    @Get
    fun getAll(): Flow<UserDto> = userService.list()

    @Get("/{id}")
    suspend fun getById(@PathVariable id: Long): HttpResponse<UserDto> = userService.get(id)?.let {
        HttpResponse.ok(it)
    } ?: HttpResponse.notFound()

    @Post
    @Status(HttpStatus.CREATED)
    suspend fun create(@Body createUserDto: CreateUserDto): UserDto = userService.create(createUserDto)

    @Patch("/{id}")
    suspend fun update(@Body updateUserDto: UpdateUserDto, auth: Authentication): HttpResponse<UserDto> =
        userService.update(updateUserDto, auth)?.let {
            HttpResponse.ok(it)
        } ?: HttpResponse.notFound()

    @Delete("/{id}")
    suspend fun delete(@PathVariable id: Long): HttpResponse<Unit> =
        if (userService.delete(id) > 0) HttpResponse.noContent() else HttpResponse.notFound()

    @Error(exception = DeactivateSelfException::class)
    fun onDeactivateSelf(): HttpResponse<String> = HttpResponse.badRequest("DEACTIVATE_SELF")

}