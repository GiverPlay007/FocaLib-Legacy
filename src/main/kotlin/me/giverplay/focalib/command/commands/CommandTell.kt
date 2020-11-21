package me.giverplay.focalib.command.commands

import me.giverplay.focalib.FocaPlugin.Companion.lib
import me.giverplay.focalib.chat.ChannelManager
import me.giverplay.focalib.command.CommandSource
import me.giverplay.focalib.command.FocaCommand
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender

class CommandTell(private val manager: ChannelManager): FocaCommand("tell", false)
{
    override fun execute(sender: CommandSource, args: Array<out String>)
    {
        if (args.isEmpty())
            return sendUsage(sender, "/tell <nickname> <msg>")

        val player = sender.asFocaPlayer

        val bukkitPlayer = Bukkit.getPlayer(args[0])
            ?: return sender.sendMessage(msg("error.nullplayer"))

        val other = lib!!.playerManager.getPlayer(bukkitPlayer.name)

        if (args.size == 1) {
            player!!.tellLocked = other
            msg("info.tell-locked", other?.name)?.let { player.sendMessage(it) }
            return
        }

        val sb = StringBuilder()

        for (i in 1 until args.size)
            sb.append(args[i]).append(" ")

        if (player != null && other != null)
            manager.performTell(player, other, sb.toString().trim())
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