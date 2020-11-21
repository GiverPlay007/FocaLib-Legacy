package me.giverplay.focalib.chat

import me.giverplay.focalib.FocaLib
import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

class ChannelManager(private val plugin: FocaLib, private val chatManager: ChatManager)
{
    private val channels = HashMap<String, Channel>()
    private var maintainSpyMode = false
    private var defaultChannel: Channel? = null
    private val formats = HashMap<String, String>()
    private val pm_formats = HashMap<String, String>()

    fun createPermanentChannel(channel: Channel)
    {
        if (existsChannel(channel.name))
            return

        channels[channel.name.toLowerCase()] = channel

        val file = File(plugin.dataFolder, "channels${File.separator}${channel.name.toLowerCase()}.yml")

        if (!file.exists())
        {
            val channelConfig = YamlConfiguration()
            channelConfig["name"] = channel.name
            channelConfig["nickname"] = channel.nickname
            channelConfig["format"] = channel.format
            channelConfig["color"] = channel.color?.name?.toLowerCase()
            channelConfig["shortcutAllowed"] = channel.isShortcutAllowed
            channelConfig["distance"] = channel.maxDistance
            channelConfig["crossworlds"] = channel.isCrossworlds
            channelConfig["delayPerMessage"] = channel.delayPerMessage
            channelConfig["costPerMessage"] = channel.costPerMessage
            channelConfig["showCostMessage"] = channel.showCostMessage

            try {
                channelConfig.save(file)
            } catch (e: Exception) { }
        }
    }

    fun deleteChannel(channel: Channel)
    {
        if (!existsChannel(channel.name))
            return

        plugin.playerManager.getPlayers().forEach { player -> player.focusedChannel = defaultChannel }
        channels.remove(channel.name.toLowerCase())
        File(plugin.dataFolder, "channels${File.separator}${channel.name.toLowerCase()}.yml").delete()
    }

    fun checkChannel(string: String): Channel?
    {
        return null
    }

    fun getChannel(name: String): Channel? = if (existsChannel(name.toLowerCase())) channels[name.toLowerCase()] else null

    fun existsChannel(name: String): Boolean = channels.containsKey(name.toLowerCase())

    fun getChannels(): List<Channel> {
        val c: MutableList<Channel> = ArrayList()
        c.addAll(channels.values)
        return c
    }

    fun loadChannels() {
        val bungee = plugin.config.getString("bungeecord.channel")!!
        channels.clear()
        for (channel in File(plugin.dataFolder, "channels").listFiles()) {
            if (channel.name.toLowerCase().endsWith(".yml")) {
                if (channel.name.toLowerCase() != channel.name) channel.renameTo(
                    File(
                        plugin.dataFolder,
                        "channels" + File.separator + channel.name.toLowerCase()
                    )
                )
                loadChannel(channel, bungee)
            }
        }
        for (p in Bukkit.getOnlinePlayers()) getPlayerManager()
            .setPlayerFocusedChannel(p, getDefaultChannel(), false)
    }

    private fun loadChannel(channel: File, bungee: String) {
        val channel2 = YamlConfiguration.loadConfiguration(channel)
        createPermanentChannel(
            Channel(
                this,
                channel2.getString("name")!!,
                channel2.getString("nickname")!!,
                channel2.getString("format")!!,
                channel2.getString("color")!![0],
                channel2.getBoolean("shortcutAllowed"),
                channel2.getDouble("distance"),
                channel2.getBoolean("crossworlds"),
                channel2.getInt("delayPerMessage"),
                channel2.getDouble("costPerMessage"),
                channel2.getBoolean("showCostMessage"),
                false
            )
        )
    }

    fun muteAll() = getChannels().forEach { c -> c.muted = true }

    fun unmuteAll() = getChannels().forEach { c -> c.muted = false }

    fun getDefaultChannel(): Channel? = defaultChannel

    fun format(msg: String): String? {
        var msg = msg
        for (f in formats.keys) msg = msg.replace("{$f}", formats[f]!!)
        return msg
    }

    fun getFormat(base_format: String): String? = formats[base_format.toLowerCase()]

    fun getPrivateMessageFormat(format: String): String? = pm_formats[format.toLowerCase()]

    fun load() {
        val fc = Bukkit.getPluginManager().getPlugin("Legendchat")!!.config
        defaultChannel = getChannel(fc.getString("default_channel", "local")!!.toLowerCase())
        maintainSpyMode = fc.getBoolean("maintain_spy_mode", false)
        formats.clear()
        pm_formats.clear()
        for (f in fc.getConfigurationSection("format")!!.getKeys(false)) formats[f.toLowerCase()] =
            fc.getString("format.$f")!!
        for (f in fc.getConfigurationSection("private_message_format")!!.getKeys(false)) pm_formats[f.toLowerCase()] =
            fc.getString("private_message_format.$f")!!
    }

    fun onEnable() {
        val file: File = File(plugin.getDataFolder(), "config.yml")
        if (!file.exists()) {
            try {
                plugin.saveResource("config_template.yml", false)
                val file2: File = File(plugin.getDataFolder(), "config_template.yml")
                file2.renameTo(File(plugin.getDataFolder(), "config.yml"))
            } catch (ignored: Exception) {
            }
        }

        val channels: File = File(getDataFolder(), "channels")

        if (!channels.exists()) {
            createPermanentChannel(Channel(chatManager, "global", "g", "{default}", '7', true, 0.0, true, 0, 0.0, true, false))
            createPermanentChannel(Channel(chatManager,  "local", "l", "{default}", 'e', true, 60.0, false, 0, 0.0, true, false))
        }

        loadChannels()
    }
}