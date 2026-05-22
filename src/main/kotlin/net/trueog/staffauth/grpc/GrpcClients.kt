package net.trueog.staffauth.grpc

import io.grpc.ManagedChannel
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import jakarta.inject.Singleton
import proto.IpCheckerGrpcKt

@Factory
class GrpcClients {
    @Singleton
    fun ipCheckStub(@GrpcChannel($$"${plugin.address}") channel: ManagedChannel) =
        IpCheckerGrpcKt.IpCheckerCoroutineStub(channel)
}