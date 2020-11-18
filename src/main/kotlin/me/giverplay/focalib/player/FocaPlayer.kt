package me.giverplay.focalib.player

import me.giverplay.focalib.chat.channel.Channel
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

    var focusedChannel: Channel? = null

    fun hasPermission(permission: String): Boolean = player.hasPermission(permission)

    fun hasAnyPermission(vararg perms: String): Boolean
    {
        for (perm in perms)
            if(hasPermission(perm))
                return true

        return false
    }

    fun canSeeChannel(channel: Channel): Boolean =
        hasPermission("foca.chat.channel.${channel.name.toLowerCase()}")

    fun hasChatColorPermission(color: ChatColor): Boolean =
        hasAnyPermission("foca.chat.color.all", "foca.chat.color.${color.name.toLowerCase()}")

    fun hasChatFormatPermission(color: ChatColor): Boolean =
        hasAnyPermission("foca.chat.format.all", "foca.chat.format.${color.name.toLowerCase()}")
}