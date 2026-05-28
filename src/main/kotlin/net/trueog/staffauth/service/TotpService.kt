package net.trueog.staffauth.service

import dev.samstevens.totp.code.CodeVerifier
import dev.samstevens.totp.code.HashingAlgorithm
import dev.samstevens.totp.qr.QrData
import dev.samstevens.totp.qr.ZxingPngQrGenerator
import dev.samstevens.totp.secret.SecretGenerator
import dev.samstevens.totp.util.Utils.getDataUriForImage
import jakarta.inject.Singleton

@Singleton
class TotpService(
    private val secretGenerator: SecretGenerator,
    private val codeVerifier: CodeVerifier
) {
    /**
     * @return secret, qrCode
     */
    fun generateTotp(username: String): Pair<String, String> {
        val secret = secretGenerator.generate()

        val qrCodeData = QrData.Builder()
            .label(username)
            .secret(secret)
            .issuer("Staff-OG")
            .algorithm(HashingAlgorithm.SHA1)
            .digits(6)
            .period(30)
            .build()

        val generator = ZxingPngQrGenerator()
        val imageData = generator.generate(qrCodeData)

        val qrCode = getDataUriForImage(imageData, generator.imageMimeType)
        return Pair(secret, qrCode)
    }

    fun isValid(secret: String, code: String) = codeVerifier.isValidCode(secret, code)
}