package me.giverplay.focalib

import me.giverplay.focalib.command.CommandManager
import org.bukkit.Bukkit
import org.bukkit.event.HandlerList

class FocaLib(plugin: FocaPlugin)
{
    val plugin: FocaPlugin = plugin

    val commandManager: CommandManager = CommandManager(plugin)

    fun enable()
    {

    }

    fun disable()
    {
        // Garantir que seja tudo limpo, caso esse método venha a ser chamado por outra criatura
        HandlerList.unregisterAll(plugin)
        Bukkit.getScheduler().cancelTasks(plugin)
    }
}