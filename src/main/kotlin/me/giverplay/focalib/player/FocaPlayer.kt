package me.giverplay.focalib.player

import me.giverplay.focalib.chat.Channel
import me.giverplay.focalib.utils.Messages
import org.bukkit.ChatColor
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer
import org.bukkit.entity.Player

class FocaPlayer(val player: Player)
{
    val settings: PlayerSettings = PlayerSettings(true, true, true, true, true, false, false)

    val name
      get() = player.name

    val craftPlayer
      get() = player as CraftPlayer

    val ping
      get() = craftPlayer.handle.ping

    val ignoring: ArrayList<String> = ArrayList()

    var focusedChannel: Channel? = null

    var tellLocked: FocaPlayer? = null

    var lastTell: FocaPlayer? = null

    fun hasPermission(permission: String): Boolean = player.hasPermission(permission)

    fun hasAnyPermission(vararg perms: String): Boolean
    {
        for (perm in perms)
            if(hasPermission(perm))
                return true

        return false
    }

    fun sendMessage(msg: String) = player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg))

    fun sendRaw(msg: String) = player.sendMessage(msg)

    fun send(msg: String) = Messages.msg(msg)?.let { player.sendMessage(it) }

    fun canUseChannel(channel: Channel): Boolean =
        hasPermission("foca.chat.channel.${channel.name.toLowerCase()}")

    fun hasChatColorPermission(color: ChatColor): Boolean =
        hasAnyPermission("foca.chat.color.all", "foca.chat.color.${color.name.toLowerCase()}")

    fun hasChatFormatPermission(color: ChatColor): Boolean =
        hasAnyPermission("foca.chat.format.all", "foca.chat.format.${color.name.toLowerCase()}")
}