package com.fredboat.sentinel.entities

/** Sent by FredBoat to fanout. We will respond with [SentinelHello] */
class FredBoatHello(
        /** If true Sentinel will reset its subscriptions */
        val startup: Boolean,
        /** Discord status */
        val game: String
)

/** Sent when Sentinel starts or [FredBoatHello] is received.
 *  Used for mapping what Sentinels we have in FredBoat */
data class SentinelHello(
        val shardStart: Int,
        val shardEnd: Int,
        val shardCount: Int,
        val key: String
)