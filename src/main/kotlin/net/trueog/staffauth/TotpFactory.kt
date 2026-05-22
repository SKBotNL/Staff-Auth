package net.trueog.staffauth

import dev.samstevens.totp.code.CodeVerifier
import dev.samstevens.totp.code.DefaultCodeGenerator
import dev.samstevens.totp.code.DefaultCodeVerifier
import dev.samstevens.totp.secret.DefaultSecretGenerator
import dev.samstevens.totp.secret.SecretGenerator
import dev.samstevens.totp.time.SystemTimeProvider
import io.micronaut.context.annotation.Factory
import jakarta.inject.Singleton

@Factory
class TotpFactory {
    @Singleton
    fun secretGenerator(): SecretGenerator = DefaultSecretGenerator()

    @Singleton
    fun codeVerifier(): CodeVerifier = DefaultCodeVerifier(DefaultCodeGenerator(), SystemTimeProvider())
}