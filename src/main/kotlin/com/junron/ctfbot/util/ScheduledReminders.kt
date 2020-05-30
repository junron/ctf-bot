package com.junron.ctfbot.util

import com.jessecorbett.diskord.dsl.Bot
import com.junron.ctfbot.model.Subscriber
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
import kotlin.concurrent.schedule

class ScheduledReminders<T : Subscriber>(
    var subscribers: List<T>,
    private val bot: Bot,
    val callback: (subscriber: T, bot: Bot) -> Unit
) {
    lateinit var timers: List<Timer>

    init {
        updateSubscriptions(subscribers)
    }

    fun updateSubscriptions(subscribers: List<T>) {
        this.subscribers = subscribers
        if (::timers.isInitialized) timers.forEach { it.cancel() }
        val timings = mutableMapOf<LocalDateTime, MutableList<T>>()
        subscribers.forEach { subscriber ->
            subscriber.timings.forEach { timeLong ->
                val time = LocalDateTime.ofEpochSecond(
                    timeLong,
                    0,
                    ZoneOffset.ofHours(8)
                )
                if (timings.containsKey(time)) {
                    timings[time]?.plusAssign(subscriber)
                } else {
                    timings[time] = mutableListOf(subscriber)
                }
            }
        }
        timers = timings.mapNotNull { (time, subscribers) ->
            val deltaMillis =
                (time.toEpochSecond(ZoneOffset.UTC) - LocalDateTime.now()
                    .toEpochSecond(ZoneOffset.UTC)) * 1000
            println(deltaMillis)
            if(deltaMillis<0) return@mapNotNull null
            Timer(false).apply {
                schedule(deltaMillis) {
                    subscribers.forEach {
                        callback(it, bot)
                    }
                }
            }
        }
    }
}
