package net.trueog.staffauth.model

sealed class LoginStage {
    data class AwaitingMinecraftCheck(val userId: Long) : LoginStage()
    data class AwaitingTotp(val userId: Long) : LoginStage()
    data class AwaitingAccept(val userId: Long) : LoginStage()
}