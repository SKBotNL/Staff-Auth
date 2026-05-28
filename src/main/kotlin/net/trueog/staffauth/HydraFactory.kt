package net.trueog.staffauth

import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Value
import jakarta.inject.Singleton
import sh.ory.hydra.ApiClient
import sh.ory.hydra.api.OAuth2Api

@Factory
class HydraFactory {
    @Value($$"${hydra.host}")
    private lateinit var hydraHost: String

    @Singleton
    fun oAuth2Api() = OAuth2Api(ApiClient().apply {
        basePath = hydraHost
    })
}