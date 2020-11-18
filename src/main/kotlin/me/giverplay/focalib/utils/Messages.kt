package me.giverplay.focalib.utils

import me.giverplay.focalib.FocaPlugin
import org.bukkit.ChatColor
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.text.MessageFormat
import java.util.logging.Level

class Messages
{
    companion object
    {
        private val cache: HashMap<String, MessageFormat?> = HashMap()
        private val messages: YamlConfiguration = YamlConfiguration()

        private var plugin: FocaPlugin? = null

        fun setup(plugin: FocaPlugin)
        {
            this.plugin = plugin
            messages.load(File(plugin.dataFolder, "Messages.yml"))
        }

        fun msg(path: String, vararg args: String?): String?
        {
            val raw: String = raw(path)

            if (args.isEmpty())
            {
                return raw
            }

            return format(path, *args)?.let { ChatColor.translateAlternateColorCodes('&', it) }
        }

        // EssentialsX - https://github.com/EssentialsX/Essentials/blob/2.x/Essentials/src/com/earth2me/essentials/I18n.java
        private fun format(str: String, vararg args: String?): String?
        {
            var format: String = raw(str)
            var messageFormat: MessageFormat? = cache[format]

            if (messageFormat == null)
            {
                try
                {
                    messageFormat = MessageFormat(format)
                }
                catch (e: IllegalArgumentException)
                {
                    plugin?.logger?.log(Level.SEVERE, "Invalid Translation key for '" + str + "': " + e.message)
                    format = format.replace("\\{(\\D*?)\\}".toRegex(), "\\[$1\\]")
                    messageFormat = MessageFormat(format)
                }

                cache[format] = messageFormat
            }

            return messageFormat?.format(args)?.replace(' ', ' ')
        }

        private fun raw(path: String): String = messages.getString(path) ?: "Undefined"
    }
}