package net.trueog.staffauth.model

sealed class SetupStage {
    data class AwaitingMinecraftCheck(val userId: Long, val details: Details) : SetupStage()
    data class AwaitingTotp(val userId: Long, val details: Details) : SetupStage()
    data class AwaitingTotpVerify(val userId: Long, val details: Details, val totpSecret: String) : SetupStage()
    data class AwaitingFinalize(val userId: Long, val details: Details, val totpSecret: String) : SetupStage()

    data class Details(val email: String, val passwordHash: String)
}