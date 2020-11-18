package me.giverplay.focalib.utils

import me.giverplay.focalib.player.FocaPlayer
import org.bukkit.ChatColor

class ColorUtils
{
    fun translateAlternateChatColorsWithPermission(player: FocaPlayer, msg: String): String?
    {
        var str = msg

        for(color: ChatColor in ChatColor.values())
        {
            when {
                color.isColor -> {
                    if(player.hasChatColorPermission(color))
                        str = str.replace("&${color.char}", color.toString())
                }

                player.hasChatFormatPermission(color) -> str = str.replace("&${color.char}", color.toString())
            }
        }

        return str
    }

    fun translateChatColorToStringColor(color: ChatColor?): String? = if (color != null && color.isColor)
            color.name.toLowerCase()
        else
            ChatColor.WHITE.name.toLowerCase()

    fun translateStringColor(color: String): ChatColor = ChatColor.valueOf(color)
}