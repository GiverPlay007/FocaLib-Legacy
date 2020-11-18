package me.giverplay.focalib.command

import me.giverplay.focalib.utils.Messages
import org.apache.commons.lang.Validate
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class CommandSource(sender: CommandSender)
{
    val sender: CommandSender

    val name
        get() = sender.name

    val isPlayer
        get() = sender is Player

    fun getPlayer(): Player ? = if (sender is Player) sender else null

    fun sendMessage(message: String?) = sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message!!))

    fun hasPermission(permission: String?): Boolean = sender.hasPermission(permission!!)

    fun hasAnyPermissions(vararg permissions: String?): Boolean = permissions.any { sender.hasPermission(it!!) }

    init {
        Validate.notNull(sender, Messages.msg("error.internal.nullsender"))
        this.sender = sender
    }
}