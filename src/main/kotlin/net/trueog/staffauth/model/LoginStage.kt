package net.trueog.staffauth.model

sealed class LoginStage(open val userId: Long, open val ip: String) {
    data class AwaitingMinecraftCheck(override val userId: Long, override val ip: String) : LoginStage(userId, ip)
    data class AwaitingTotp(override val userId: Long, override val ip: String) : LoginStage(userId, ip)
    data class AwaitingAccept(override val userId: Long, override val ip: String) : LoginStage(userId, ip)
}