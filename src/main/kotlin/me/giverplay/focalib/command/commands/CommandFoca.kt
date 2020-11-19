package me.giverplay.focalib.command.commands

import me.giverplay.focalib.FocaLib
import me.giverplay.focalib.command.CommandSource
import me.giverplay.focalib.command.FocaCommand
import org.bukkit.command.Command
import org.bukkit.command.CommandSender

class CommandFoca(private val plugin: FocaLib): FocaCommand("foca", true)
{
    override fun execute(sender: CommandSource, args: Array<out String>)
    {
        if(args.isEmpty() || !sender.hasPermission("foca.manager"))
            return sender.sendMessage("Mensagem bonitinha")

        if(args[0].equals("reload", ignoreCase = true))
        {
            plugin.reload();
            sender.sendMessage(msg("info.reload"))
            return
        }
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): MutableList<String>
    {
        return ArrayList()
    }
}