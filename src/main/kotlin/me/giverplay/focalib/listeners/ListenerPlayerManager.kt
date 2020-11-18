package me.giverplay.focalib.listeners

import me.giverplay.focalib.player.FocaPlayer
import me.giverplay.focalib.player.PlayerManager
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class ListenerPlayerManager(manager: PlayerManager): Listener
{
    private val manager: PlayerManager = manager

    @EventHandler(priority = EventPriority.LOWEST)
    fun onJoin(event: PlayerJoinEvent) = manager.registerPlayer(FocaPlayer(event.player))

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onLeave(event: PlayerQuitEvent) = manager.unregisterPlayer(event.player)
}