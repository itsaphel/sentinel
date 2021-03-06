package com.fredboat.sentinel.config

import com.fredboat.sentinel.ApplicationState
import com.fredboat.sentinel.jda.JdaRabbitEventListener
import net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder
import net.dv8tion.jda.bot.sharding.ShardManager
import net.dv8tion.jda.core.utils.SessionController
import net.dv8tion.jda.core.utils.SessionControllerAdapter
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.*
import javax.security.auth.login.LoginException
import kotlin.collections.HashSet

@Configuration
class ShardManagerConfig {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(ShardManagerConfig::class.java)
    }
    @Bean
    fun buildShardManager(jdaProperties: JdaProperties,
                          rabbitEventListener: JdaRabbitEventListener,
                          sessionController: SessionController
    ): ShardManager {

        val builder = DefaultShardManagerBuilder()
                .setToken(jdaProperties.discordToken)
                //.setGame(Game.playing(configProvider.getAppConfig().getStatus()))
                .setBulkDeleteSplittingEnabled(false)
                .setEnableShutdownHook(false)
                .setAudioEnabled(true)
                .setAutoReconnect(true)
                .setSessionController(SessionControllerAdapter())
                .setShardsTotal(jdaProperties.shardCount)
                .setShards(jdaProperties.shardStart, jdaProperties.shardEnd)
                .setSessionController(sessionController)
                //.setHttpClientBuilder(Http.DEFAULT_BUILDER.newBuilder()
                //        .eventListener(OkHttpEventMetrics("jda", Metrics.httpEventCounter)))
                .addEventListeners(rabbitEventListener)
                //.addEventListeners(jdaEventsMetricsListener)
                //.addEventListeners(eventLogger)
                //.addEventListeners(shardReviveHandler)
                //.addEventListeners(musicPersistenceHandler)
                //.addEventListeners(audioConnectionFacade)

        val shardManager: ShardManager
        try {
            shardManager = builder.build()
            if (ApplicationState.isTesting) {
                log.info("Shutting down JDA because we are running tests")
                try {
                    shardManager.shutdown()
                } catch (npe: NullPointerException) {
                    // Race condition
                    Thread.sleep(500)
                    shardManager.shutdown()
                }
            }
        } catch (e: LoginException) {
            throw RuntimeException("Failed to log in to Discord! Is your token invalid?", e)
        }

        return shardManager
    }

    @Bean
    fun guildSubscriptions(): MutableSet<Long> = Collections.synchronizedSet(HashSet<Long>())

}