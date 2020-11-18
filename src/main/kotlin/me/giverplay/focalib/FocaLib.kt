package me.giverplay.focalib

import me.giverplay.focalib.command.CommandManager
import me.giverplay.focalib.player.PlayerManager
import me.giverplay.focalib.utils.DependencyException
import me.giverplay.focalib.utils.Messages
import org.bukkit.Bukkit
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener

class FocaLib(plugin: FocaPlugin)
{
    val plugin: FocaPlugin = plugin

    val commandManager: CommandManager = CommandManager(plugin)
    val playerManager: PlayerManager = PlayerManager(this)

    val logger
      get() = plugin.logger

    fun enable()
    {
        if(Bukkit.getPluginManager().getPlugin("Vault") == null)
        {
            throw DependencyException(Messages.msg("error.dependency.vault"))
        }
    }

    fun disable()
    {
        // Garantir que seja tudo limpo, caso esse método venha a ser chamado por outra criatura
        HandlerList.unregisterAll(plugin)
        Bukkit.getScheduler().cancelTasks(plugin)
    }

    fun registerEvent(listener: Listener) = Bukkit.getPluginManager().registerEvents(listener, plugin)
}