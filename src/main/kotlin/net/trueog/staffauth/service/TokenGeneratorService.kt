package net.trueog.staffauth.service

import jakarta.inject.Singleton
import java.security.SecureRandom

@Singleton
class TokenGeneratorService {
    private val secureRandom = SecureRandom.getInstanceStrong()

    fun generateToken(length: Int): ByteArray {
        val token = ByteArray(length)
        secureRandom.nextBytes(token)
        return token
    }
}