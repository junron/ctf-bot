package com.junron.ctfbot.commands

import com.jessecorbett.diskord.dsl.Bot
import com.jessecorbett.diskord.dsl.CommandSet
import com.jessecorbett.diskord.dsl.command
import com.junron.ctfbot.model.ctftime.CTFTime
import com.junron.ctfbot.util.reject
import kotlinx.serialization.UnstableDefault

@UnstableDefault
object CTFTime : Command {
    override fun init(bot: Bot, prefix: CommandSet) {
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
                    }.forEach { reply("", it.embed()) }
                }
            }
        }
    }
}
