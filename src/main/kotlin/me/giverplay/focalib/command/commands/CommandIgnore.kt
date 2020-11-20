package me.giverplay.focalib.command.commands

import me.giverplay.focalib.command.CommandSource
import me.giverplay.focalib.command.FocaCommand
import me.giverplay.focalib.player.FocaPlayer
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.command.Command
import org.bukkit.command.CommandSender

class CommandIgnore(): FocaCommand("ignore", false)
{
    override fun execute(sender: CommandSource, args: Array<out String>)
    {
        if(args.isEmpty())
            return sendUsage(sender, "/ignore <jogador>")

        val ignored: OfflinePlayer = Bukkit.getOfflinePlayer(args[0])

        if(!ignored.hasPlayedBefore())
            return sender.sendMessage(msg("error.nullplayer"))

        val player: FocaPlayer? = sender.asFocaPlayer

        if(player != null)
        {
            if(!player.ignoring.contains(ignored.name))
            {
                ignored.name?.let { player.ignoring.add(it) }
                sender.sendMessage(msg("info.ignore", ignored.name))
            }
            else
            {
                player.ignoring.remove(ignored.name)
                sender.sendMessage(msg("info.unignore", ignored.name))
            }
        }
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