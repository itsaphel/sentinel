package com.fredboat.sentinel.rpc

import com.fredboat.sentinel.SentinelExchanges
import com.fredboat.sentinel.entities.Guild
import com.fredboat.sentinel.entities.GuildRequest
import com.fredboat.sentinel.entities.GuildsRequest
import com.fredboat.sentinel.entities.GuildsResponse
import com.fredboat.sentinel.extension.toEntity
import net.dv8tion.jda.bot.sharding.ShardManager
import net.dv8tion.jda.core.JDA
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitHandler
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Service

@Service
@RabbitListener(queues = [SentinelExchanges.REQUESTS])
class EntityRequests(private val shardManager: ShardManager) {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(EntityRequests::class.java)
    }

    @RabbitHandler
    fun getGuilds(request: GuildsRequest): GuildsResponse? {
        val jda: JDA? = shardManager.getShardById(request.shard)

        if (jda == null) {
            log.error("Received GuildsRequest for shard ${request.shard} which was not found")
            return null
        } else if (jda.status != JDA.Status.CONNECTED) {
            log.warn("Received GuildsRequest for shard ${request.shard} but status is ${jda.status}")
        }

        val list = mutableListOf<Guild>()
        jda.guilds.forEach { list.add(it.toEntity()) }
        return GuildsResponse(list)
    }

    @RabbitHandler
    fun getGuild(request: GuildRequest): Guild? {
        val guild: Guild? = shardManager.getGuildById(request.id)?.toEntity()

        if (guild == null) log.error("Received GuildRequest but guild ${request.id} was not found")

        return guild
    }

}