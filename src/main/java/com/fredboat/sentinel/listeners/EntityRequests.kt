package com.fredboat.sentinel.listeners

import com.fredboat.sentinel.QueueNames
import com.fredboat.sentinel.entities.*
import com.fredboat.sentinel.extension.toEntity
import net.dv8tion.jda.bot.sharding.ShardManager
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.entities.TextChannel
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitHandler
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Service

@Service
@RabbitListener(queues = [QueueNames.SENTINEL_REQUESTS_QUEUE])
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

    @RabbitHandler
    fun sendMessage(request: SendMessageRequest): SendMessageResponse? {
        val channel: TextChannel? = shardManager.getTextChannelById(request.channel)

        if (channel == null) {
            log.error("Received SendMessageRequest for channel ${request.channel} which was not found")
            return null
        }

        val msg = channel.sendMessage(request.content).complete()
        return SendMessageResponse(msg.idLong)
    }

    @RabbitHandler
    fun sendPrivateMessage(request: SendPrivateMessageRequest): SendMessageResponse? {
        val user = shardManager.getUserById(request.recipient)
        val privateChannel = user.openPrivateChannel().complete(true)

        if (user == null) {
            log.error("User ${request.recipient} was not found when sending private message")
            return null
        }

        val msg = privateChannel.sendMessage(request.content).complete()
        return SendMessageResponse(msg.idLong)
    }

    @RabbitHandler
    fun editMessage(request: EditMessageRequest) {
        val channel: TextChannel? = shardManager.getTextChannelById(request.channel)

        if (channel == null) {
            log.error("Received EditMessageRequest for channel ${request.channel} which was not found")
            return
        }

        channel.editMessageById(request.messageId, request.content).queue()
    }

    @RabbitHandler
    fun sendTyping(request: SendTypingRequest) {
        val channel: TextChannel? = shardManager.getTextChannelById(request.channel)

        if (channel == null) {
            log.error("Received SendTypingRequest for channel ${request.channel} which was not found")
            return
        }

        channel.sendTyping().queue()
    }

    @Suppress("UNUSED_PARAMETER")
    @RabbitHandler
    fun getApplicationInfo(request: ApplicationInfoRequest): ApplicationInfo {
        val info = shardManager.applicationInfo.complete()
        lateinit var entity: ApplicationInfo
        info.apply {
            entity = ApplicationInfo(
                    idLong,
                    doesBotRequireCodeGrant(),
                    description,
                    iconId,
                    iconUrl,
                    name,
                    owner.idLong,
                    isBotPublic
            )
        }
        return entity
    }

}