package me.giverplay.focalib.chat.channel

import org.bukkit.ChatColor
import org.bukkit.entity.Player

class Channel(private val manager: ChannelManager,
              var name: String,
              var nickname: String,
              var format: String,
              color: String,
              var isShortcutAllowed: Boolean,
              var isFocusNeeded: Boolean,
              var maxDistance: Double,
              var isCrossworlds: Boolean,
              var delayPerMessage: Int,
              var costPerMessage: Double,
              var showCostMessage: Boolean, var stringColor: String = color.toLowerCase()
) {

    var color: String = manager.translateStringColor(color)

    fun setColor(c: ChatColor?) {
        stringColor = manager.translateChatColorToStringColor(c)
        color = manager.translateStringColor(stringColor)
    }

    fun sendMessage(message: String?) = manager.otherMessage(this, message)

    fun sendMessage(sender: Player?, message: String?) = manager.fakeMessage(this, sender, message)

    fun sendMessage(sender: Player?, message: String?, bukkit_format: String?, cancelled: Boolean) =
        manager.realMessage(this, sender, message, bukkit_format, cancelled)
}