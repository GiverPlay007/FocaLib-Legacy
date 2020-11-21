package me.giverplay.focalib.listeners

import me.giverplay.focalib.FocaLib
import me.giverplay.focalib.chat.Channel
import me.giverplay.focalib.chat.ChannelManager
import me.giverplay.focalib.chat.ChatManager
import me.giverplay.focalib.player.FocaPlayer
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.player.PlayerJoinEvent

class ListenerChat(
    private var plugin: FocaLib,
    private val channelManager: ChannelManager,
    private val chatManager: ChatManager
): Listener
{
    @EventHandler(priority = EventPriority.LOWEST)
    fun onJoin(event: PlayerJoinEvent)
    {
        val player: FocaPlayer? = plugin.playerManager.getPlayer(event.player.name)
        player?.focusedChannel = channelManager.getDefaultChannel()
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    fun onChat(event: AsyncPlayerChatEvent)
    {
        event.isCancelled = true
        val player: FocaPlayer? = plugin.playerManager.getPlayer(event.player.name)

        if(player?.tellLocked != null)
        {
            chatManager.performTell(player, player.tellLocked!!, event.message)
            return
        }

        if(player?.focusedChannel != null)
        {
            chatManager.performMessage(player, event.message)
            return
        }

        player?.send("error.nochannel")
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private fun onChat(event: PlayerCommandPreprocessEvent)
    {
        val channel: Channel? = channelManager.checkChannel(event.message.toLowerCase()) ?: return

        event.isCancelled = true
        val player: FocaPlayer? = plugin.playerManager.getPlayer(event.player.name)
        val split = event.message.split(" ")

        if(split.size == 1) {
            player?.sendMessage("&c/${channel?.name} <msg>")
            return
        }

        if (player != null) {
            chatManager.performMessage(player, event.message)
        }
    }
}