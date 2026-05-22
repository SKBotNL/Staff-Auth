package net.trueog.staffauth.controller

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.rules.SecurityRule

@Controller("/me")
@Secured(SecurityRule.IS_AUTHENTICATED)
class MeResource {
    @Get
    fun index(auth: Authentication): Map<String, Any> {
        return auth.attributes
    }
}