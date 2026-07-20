package net.trueog.staffauth.service

import jakarta.inject.Singleton
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

@Singleton
class Argon2idPasswordEncoderService : PasswordEncoder {
    private val delegate: Argon2PasswordEncoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8()

    override fun encode(rawPassword: CharSequence?) = delegate.encode(rawPassword)

    override fun matches(rawPassword: CharSequence?, encodedPassword: String?) =
        delegate.matches(rawPassword, encodedPassword)
}