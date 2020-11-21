package me.giverplay.focalib.command.commands

import me.giverplay.focalib.chat.ChatManager
import me.giverplay.focalib.command.CommandSource
import me.giverplay.focalib.command.FocaCommand
import me.giverplay.focalib.player.FocaPlayer
import org.bukkit.command.Command
import org.bukkit.command.CommandSender

class CommandReply(private val manager: ChatManager): FocaCommand("reply", false)
{
    override fun execute(sender: CommandSource, args: Array<out String>)
    {
        if (args.isEmpty()) {
            sendUsage(sender, "/r <msg>")
            return
        }

        val player: FocaPlayer? = sender.asFocaPlayer

        if (player?.lastTell == null)
            return sender.sendMessage(msg("error.nullreply"))

        manager.performTell(player, player.lastTell!!, args)
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): MutableList<String> {
        return ArrayList()
    }
}