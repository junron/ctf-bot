package com.junron.ctfbot.commands

import com.jessecorbett.diskord.api.rest.CreateMessage
import com.jessecorbett.diskord.dsl.Bot
import com.jessecorbett.diskord.dsl.CommandSet
import com.jessecorbett.diskord.dsl.command
import com.junron.ctfbot.model.ctftime.CTFSubscriber
import com.junron.ctfbot.model.ctftime.CTFTime
import com.junron.ctfbot.model.ctftime.CTFTime.fetchCTF
import com.junron.ctfbot.util.ScheduledReminders
import com.junron.ctfbot.util.dmUser
import com.junron.ctfbot.util.reject
import com.junron.pyrobase.jsoncache.Storage
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.UnstableDefault
import java.time.ZoneOffset

val subscribers = Storage("ctf_subscribers", CTFSubscriber.serializer())

@UnstableDefault
object CTFTime : Command {
    override fun init(bot: Bot, prefix: CommandSet) {
        val reminders = ScheduledReminders(subscribers, bot) { subscriber, _ ->
            runBlocking {
                bot.dmUser(
                    subscriber.authorId,
                    CreateMessage("${subscriber.ctf.title} is starting in one hour. Good luck!")
                )
            }
        }
        with(bot) {
            with(prefix) {
                command("upcoming") {
                    val ctfs = CTFTime.fetchNextWeek() ?: return@command reject(
                        this,
                        "An unknown error occurred."
                    )
                    reply("Here are the upcoming CTFs:")
                    ctfs.filter {
                        !it.onsite && it.restrictions == "Open"
                    }.take(1).forEach {
                        val message = reply("", it.embed())
                        message.react("⏰")
                    }
                }
            }
            reactionAdded { reaction ->
                //   Ignore from bot
                if (reaction.userId == "716138599275298846") return@reactionAdded
                val ctfId =
                    bot.clientStore.channels[reaction.channelId].getMessage(
                        reaction.messageId
                    ).embeds.firstOrNull()?.fields?.firstOrNull { it.name == "CTFTime ID" }?.value?.toIntOrNull()
                        ?: return@reactionAdded
                val ctf = fetchCTF(ctfId) ?: return@reactionAdded
                val subscriber = CTFSubscriber(
                    listOf(
                        ctf.startTime.minusHours(1).toEpochSecond(
                            ZoneOffset.ofHours(8)
                        )
                    ),
                    reaction.userId, ctf
                )
                subscribers += subscriber
                reminders.updateSubscriptions(subscribers)
            }
        }
    }
}
