package net.trueog.staffauth.grpc

import io.grpc.ManagedChannel
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder
import io.micronaut.context.annotation.Factory
import jakarta.inject.Named
import jakarta.inject.Singleton
import net.trueog.staffauth.configuration.PluginConfiguration
import proto.IpCheckerGrpcKt
import java.io.File

@Factory
class GrpcClients {
    @Singleton
    @Named("ipCheckChannel")
    fun ipCheckChannel(config: PluginConfiguration) : ManagedChannel {
        val builder = NettyChannelBuilder.forAddress(config.host, config.port)
        if (config.ssl.enabled) {
            builder.sslContext(
                GrpcSslContexts.forClient()
                    .trustManager(File(config.ssl.trustCa))
                    .keyManager(File(config.ssl.cert), File(config.ssl.privateKey))
                    .build()
            )
        } else {
            builder.usePlaintext()
        }
        return builder.build()
    }

    @Singleton
    fun ipCheckStub(@Named("ipCheckChannel") channel: ManagedChannel) =
        IpCheckerGrpcKt.IpCheckerCoroutineStub(channel)
}