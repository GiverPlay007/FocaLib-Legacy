package me.giverplay.focalib.command.commands

import me.giverplay.focalib.FocaPlugin
import me.giverplay.focalib.chat.Channel
import me.giverplay.focalib.chat.ChannelManager
import me.giverplay.focalib.command.CommandSource
import me.giverplay.focalib.command.FocaCommand
import me.giverplay.focalib.player.FocaPlayer
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class CommandChannel(private val manager: ChannelManager): FocaCommand("channel", false)
{
    override fun execute(sender: CommandSource, args: Array<out String>)
    {
        if (args.isEmpty())
            return sendUsage(sender, "/ch <channel>", "/ch global")

        val channel: Channel? = manager.getChannelByNameOrNickname(args[0])
            ?: return sender.sendMessage(msg("error.nullchannel"))

        val player: FocaPlayer?

        if(args.size == 1)
        {
            player = sender.asFocaPlayer
            player?.focusedChannel = channel
            sender.sendMessage(msg("info.changed-channel", channel?.name))
            return
        }

        if(!sender.hasPermission("foca.command.channel.other"))
            return sender.sendMessage("error.no-perm")

        val bukkitPlayer: Player? = Bukkit.getPlayer(args[1])

        if(bukkitPlayer != null)
        {
            player = FocaPlugin.lib?.playerManager?.getPlayer(bukkitPlayer.name)
            player?.focusedChannel = channel
            msg("info.changed-channel", channel?.name)?.let { player?.sendRaw(it) }
            sender.sendMessage(msg("info.changed-channel-other", channel?.name, player?.name))
            return
        }

        sender.sendMessage("error.nullplayer")
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