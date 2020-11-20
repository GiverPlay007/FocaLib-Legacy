package me.giverplay.focalib.command.commands

import me.giverplay.focalib.chat.Channel
import me.giverplay.focalib.chat.MessageManager
import me.giverplay.focalib.command.CommandSource
import me.giverplay.focalib.command.FocaCommand
import me.giverplay.focalib.player.PlayerSettings
import org.apache.commons.lang.WordUtils
import org.bukkit.command.Command
import org.bukkit.command.CommandSender

class CommandChat(private val manager: MessageManager): FocaCommand("chat", false)
{
    override fun execute(sender: CommandSource, args: Array<out String>)
    {
        if(args.isEmpty() || !sender.hasPermission("foca.chat.admin"))
            sender.sendMessage(msg("info.legendchat"))

        if (args[0].equals("channel", ignoreCase = true))
        {
            if (!sender.hasPermission("foca.chat.admin.channel"))
                return sender.sendMessage(msg("error.no-perm"))

            if (args.size < 3)
                return sendUsage(sender, "/chat channel <create/delete> <channel>", "/chat channel create staff")

            when {
                args[1].equals("create", ignoreCase = true) -> {
                    val channel: Channel? = manager.getChannelByName(args[2].toLowerCase())

                    if (channel != null)
                        return sender.sendMessage(msg("error.notnullchannel"))

                    sender.sendMessage(msg("info.created-channel"))

                    manager.createPermanentChannel(
                        Channel(
                            manager,
                            WordUtils.capitalizeFully(args[2]),
                            args[2][0].toString().toLowerCase(),
                            "{default}",
                            '8',
                            true,
                            false,
                            0.0,
                            true,
                            0,
                            0.0,
                            false,
                            false
                        )
                    )
                }

                args[1].equals("delete", ignoreCase = true) -> {
                    val channel: Channel = manager.getChannelByName(args[2].toLowerCase())
                        ?: return sender.sendMessage(msg("error.nullchannel"))

                    sender.sendMessage(msg("info.deleted-channel", channel.name))
                    manager.deleteChannel(channel)
                }

                else -> sendUsage(sender, "/chat channel <create/delete> <channel>")
            }

            return
        }

        if(args[0].equals("spy", ignoreCase = true))
        {
            val set: PlayerSettings? = sender.asFocaPlayer?.settings

            if(set?.spying!!)
            {
                set.spying = false
                sender.sendMessage(msg("info.spyoff"))
            }
            else
            {
                set.spying = true
                sender.sendMessage(msg("info.spyon"))
            }

            return
        }

        if(args[0].equals("mute", ignoreCase = true))
        {
            if(args.size < 2)
                return sendUsage(sender, "/chat mute <channel>", "/chat mute global")

            val channel: Channel? = manager.getChannelByName(args[1].toLowerCase())
                ?: return sender.sendMessage(msg("error.nullchannel"))

            if(channel?.muted!!)
                return sender.sendMessage(msg("error.channel-muted"))

            channel.muted = true
            sender.sendMessage(msg("info.muted-channel", channel.name))

            return
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