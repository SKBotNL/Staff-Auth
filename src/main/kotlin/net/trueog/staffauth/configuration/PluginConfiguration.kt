package net.trueog.staffauth.configuration

import io.micronaut.context.annotation.ConfigurationProperties

@ConfigurationProperties("plugin")
data class PluginConfiguration(
    var host: String,
    var port: Int,
    var ssl: SslConfig = SslConfig()
) {
    @ConfigurationProperties("ssl")
    data class SslConfig(
        var enabled: Boolean = false,
        var trustCa: String = "",
        var cert: String = "",
        var privateKey: String = ""
    )
}