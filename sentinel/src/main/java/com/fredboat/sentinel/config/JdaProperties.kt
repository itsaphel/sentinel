package com.fredboat.sentinel.config

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.FileInputStream

@Component
@ConfigurationProperties(prefix = "sentinel")
class JdaProperties(
        discordToken: String = "",
        var shardStart: Int = 0,
        var shardEnd: Int = 0,
        var shardCount: Int = 1
) {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(JdaProperties::class.java)
    }

    private var _discordToken = discordToken
    var discordToken: String
        get() {
            if (_discordToken.isBlank()) {
                val file = File("common.yml")
                if (file.exists()) {
                    val yaml = Yaml()
                    val map = yaml.load<Map<String, Any>>(FileInputStream(file))
                    val newToken = map["discordToken"] as? String
                    if (newToken != null) {
                        log.info("Discovered token in ${file.absolutePath}")
                        _discordToken = newToken
                    } else {
                        log.error("Found ${file.absolutePath} but no token!")
                    }
                } else {
                    log.warn("common.yml is missing and no token was defined by Spring")
                }
            }

            if (_discordToken.isBlank()) {
                throw RuntimeException("No discord bot token provided." +
                        "\nMake sure to put a discord bot token into your common.yml file.")
            }

            return _discordToken
        }
        set(value) { _discordToken = value }
}
