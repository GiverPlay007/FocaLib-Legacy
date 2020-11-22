package me.giverplay.focalib

import me.giverplay.focalib.command.CommandManager
import me.giverplay.focalib.command.commands.CommandFoca
import me.giverplay.focalib.player.PlayerManager
import me.giverplay.focalib.utils.DependencyException
import me.giverplay.focalib.utils.Messages
import org.bukkit.Bukkit
import org.bukkit.event.Listener

class FocaLib(plugin: FocaPlugin)
{
    val plugin: FocaPlugin = plugin

    val commandManager: CommandManager = CommandManager(plugin)
    val playerManager: PlayerManager = PlayerManager(this)

    val logger
      get() = plugin.logger

    val dataFolder
      get() = plugin.dataFolder

    fun enable()
    {
        if(Bukkit.getPluginManager().getPlugin("Vault") == null)
        {
            throw DependencyException(Messages.msg("error.dependency.vault"))
        }

        commandManager.registerCommand(CommandFoca(this))
    }

    fun disable()
    {

    }

    fun reload()
    {

    }

    fun registerEvent(listener: Listener) = Bukkit.getPluginManager().registerEvents(listener, plugin)
}