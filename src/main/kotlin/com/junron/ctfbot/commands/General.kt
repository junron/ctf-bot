package com.junron.ctfbot.commands

import com.jessecorbett.diskord.api.model.Permission
import com.jessecorbett.diskord.api.rest.BulkMessageDelete
import com.jessecorbett.diskord.dsl.Bot
import com.jessecorbett.diskord.dsl.CommandSet
import com.jessecorbett.diskord.dsl.command
import com.jessecorbett.diskord.util.computePermissions
import com.jessecorbett.diskord.util.words
import com.junron.ctfbot.util.toDate
import com.junron.ctfbot.util.uuid
import kotlinx.coroutines.runBlocking
import java.lang.Integer.min
import java.util.*
import kotlin.concurrent.schedule

object General : Command {
    override fun init(bot: Bot, prefix: CommandSet) {
        with(bot) {
            with(prefix) {
                command("clear") {
                    val permissions =
                        computePermissions(author, channel.get(), clientStore)
                    if (!permissions.contains(Permission.ADMINISTRATOR)) {
                        return@command
                    }
                    var n = words.drop(1)[0].toIntOrNull()?.plus(1)
                        ?: return@command
                    var deleted = 0
                    while (n > 0) {
                        val chunk = min(100, n)
                        n -= chunk
                        val messages = channel.getMessages(chunk)
                            .filter {
                                Date().time - it.sentAt.toDate().time < 1.21e+9
                            }
                        if (messages.size < 2) {
                            messages.forEach { it.delete() }
                        }
                        val ids = messages.map { it.id }
                        deleted += messages.size
                        channel.bulkDeleteMessages(BulkMessageDelete(ids))
                    }
                    deleted -= 1
                    val msg = reply("$deleted messages deleted.")
                    Timer(uuid()).schedule(2000) {
                        runBlocking {
                            msg.delete()
                        }
                    }
                }
            }
        }
    }

}
