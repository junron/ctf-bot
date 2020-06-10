package com.junron.ctfbot.commands

import com.jessecorbett.diskord.api.rest.CreateMessage
import com.jessecorbett.diskord.dsl.Bot
import com.jessecorbett.diskord.dsl.CommandSet
import com.jessecorbett.diskord.dsl.command
import com.jessecorbett.diskord.util.words
import com.joestelmach.natty.Parser
import com.junron.ctfbot.model.ctftime.CTFSubscriber
import com.junron.ctfbot.model.ctftime.CTFTime
import com.junron.ctfbot.model.ctftime.CTFTime.fetchCTF
import com.junron.ctfbot.util.ScheduledReminders
import com.junron.ctfbot.util.dmUser
import com.junron.ctfbot.util.isFuture
import com.junron.ctfbot.util.reject
import com.junron.pyrobase.jsoncache.Storage
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.UnstableDefault
import java.time.ZoneOffset
import java.util.*

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
                    val parser = Parser()
                    val input = words.drop(2).joinToString(" ").trim()

                    val dateRange = if (input.isNotEmpty()) parser.parse(input)
                        .firstOrNull()?.dates?.toMutableList()
                        ?: mutableListOf() else mutableListOf()
                    if (dateRange.isEmpty() && input.isNotEmpty()) {
                        reply("$input doesn't seem like a valid date.")
                        return@command
                    }
                    if (dateRange.any { !it.isFuture() }) {
                        reply("Dates must be in the future")
                        return@command
                    }
                    if (dateRange.size == 1) {
                        dateRange.add(0, Date())
                    }
                    val ctfs = CTFTime.fetchDateRange(dateRange)
                        ?: return@command reject(
                            this,
                            "An unknown error occurred."
                        )
                    if (ctfs.isEmpty()) return@command run {
                        reply("There are no upcoming CTFs.")
                    }
                    reply("Here are the upcoming CTFs:")
                    ctfs.filter {
                        !it.onsite && it.restrictions == "Open"
                    }.forEach {
                        val message = reply("", it.embed())
                        message.react("⏰")
                    }
                }
                command("archive") {
                    if (guildId != "715868718156218449") return@command
                    val currentCTFCategory = "715869115776237608"
                    val archiveCategory = "715869152446906378"
                    val channel =
                        clientStore.guilds["715868718156218449"].getChannels()
                            .first { it.id == channelId }
                    if (channel.parentId != currentCTFCategory) return@command run {
                        reply("Only CTF channels can be archived")
                    }
                    clientStore.channels[channelId].update(channel.copy(parentId = archiveCategory))
                    reply("${channel.name} has been archived.")
                }
            }
            reactionAdded { reaction ->
                //   Ignore bot
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
            reactionRemoved { reaction ->
                if (reaction.userId == "716138599275298846") return@reactionRemoved
                val ctfId =
                    bot.clientStore.channels[reaction.channelId].getMessage(
                        reaction.messageId
                    ).embeds.firstOrNull()?.fields?.firstOrNull { it.name == "CTFTime ID" }?.value?.toIntOrNull()
                        ?: return@reactionRemoved
                subscribers -= reaction.userId + ctfId
                reminders.updateSubscriptions(subscribers)
            }
        }
    }
}
