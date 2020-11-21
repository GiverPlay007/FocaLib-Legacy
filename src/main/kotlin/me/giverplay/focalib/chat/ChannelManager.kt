package me.giverplay.focalib.chat

import me.giverplay.focalib.FocaLib
import me.giverplay.focalib.player.FocaPlayer
import me.giverplay.focalib.utils.ColorUtils
import me.giverplay.focalib.utils.Messages.Companion.msg
import net.milkbowl.vault.chat.Chat
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.event.player.AsyncPlayerChatEvent
import java.io.File

class ChannelManager(private val plugin: FocaLib)
{
    private val channels = HashMap<String, Channel>()

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
        File(plugin.dataFolder, "channels" + File.separator + channel.name.toLowerCase() + ".yml").delete()
    }

    fun getChannelByName(name: String): Channel? {
        var name = name
        name = name.toLowerCase()
        return if (existsChannel(name)) channels[name] else null
    }

    fun getChannelByNickname(nickname: String?): Channel? {
        for (c in getChannels()) if (c.nickname.equals(nickname, ignoreCase = true)) return c
        return null
    }

    fun getChannelByNameOrNickname(name_or_nickname: String): Channel? {
        var c = getChannelByName(name_or_nickname)
        if (c == null) c = getChannelByNickname(name_or_nickname)
        return c
    }

    fun existsChannel(name: String): Boolean {
        return channels.containsKey(name.toLowerCase())
    }

    fun existsChannelAdvanced(name_or_nickname: String): Boolean {
        var e = channels.containsKey(name_or_nickname.toLowerCase())
        if (!e) e = if (getChannelByNickname(name_or_nickname) == null) false else true
        return e
    }

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

    fun fakeMessage(c: Channel, sender: Player?, message: String?) {
        if (!sendFakeMessageToChat()) {
            c.sendMessage(sender, message, "", false)
            return
        }
        val p = HashSet<Player?>()
        p.add(sender)
        val event = AsyncPlayerChatEvent(
            true,
            sender!!, "legendchat", p
        )
        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            Bukkit.getPluginManager().callEvent(event)
            c.sendMessage(sender, message, event.format, Listeners.getFakeChat(event))
        })
    }

    fun realMessage(c: Channel, sender: Player, message: String?, bukkit_format: String, cancelled: Boolean) {
        if (!sender.hasPermission("channel." + c.name.toLowerCase() + ".chat") && !sender.hasPermission("admin")) {
            sender.sendMessage(msg("error2"))
            return
        }
        if (sender.hasPermission("channel." + c.name.toLowerCase() + ".blockwrite") && !sender.hasPermission(
                "admin"
            )
        ) {
            sender.sendMessage(msg("error2"))
            return
        }
        if (c.isFocusNeeded) {
            if (getPlayerManager().getPlayerFocusedChannel(sender) !== c) {
                sender.sendMessage(msg("error12"))
                return
            }
        }
        val delay = getDelayManager().getPlayerDelayFromChannel(sender.name, c)
        if (delay > 0) {
            sender.sendMessage(
                msg("error11").replace("@time", Integer.toString(delay))
            )
            return
        }

        if (getMuteManager().isServerMuted) {
            sender.sendMessage(msg("mute_error8"))
            return
        }

        val recipients: MutableSet<Player> = HashSet()
        for (p in Bukkit.getOnlinePlayers()) {
            if (p.hasPermission("channel." + c.name.toLowerCase() + ".chat") || p.hasPermission("admin")) {
                recipients.add(p)
            }
        }
        val recipients2: MutableSet<Player> = HashSet()
        recipients2.addAll(recipients)
        for (p in recipients2) {
            if (c.maxDistance != 0.0) {
                if (sender.world !== p.world) {
                    recipients.remove(p)
                    continue
                } else if (sender.location.distance(p.location) > c.maxDistance) {
                    recipients.remove(p)
                    continue
                }
            } else {
                if (!c.isCrossworlds) {
                    if (sender.world !== p.world) {
                        recipients.remove(p)
                        continue
                    }
                }
            }
            if (getIgnoreManager().hasPlayerIgnoredPlayer(p, sender.name)) {
                recipients.remove(p)
                continue
            }
            if (getIgnoreManager().hasPlayerIgnoredChannel(p, c)) {
                recipients.remove(p)
                continue
            }
        }
        var gastou = false
        if (!block_econ && c.msgCost() > 0) {
            if (!sender.hasPermission("channel." + c.name.toLowerCase() + ".free") && !sender.hasPermission("admin")) {
                if (econ.getBalance(sender.name) < c.msgCost()) {
                    sender.sendMessage(
                        msg("error3")
                            .replace("@price", java.lang.Double.toString(c.msgCost()))
                    )
                    return
                }
                econ.withdrawPlayer(sender.name, c.msgCost())
                gastou = true
            }
        }
        var n_format_p_p = ""
        var n_format_p = ""
        var n_format_s = ""
        if (bukkit_format.contains("<") && bukkit_format.contains(">")) {
            var name_code: String? = null
            if (bukkit_format.contains("%1\$s")) {
                name_code = "%1\$s"
            } else if (bukkit_format.contains("%s")) {
                name_code = "%s"
            }
            val seploc = bukkit_format.indexOf(name_code!!)
            var finalloc = -1
            for (i in seploc downTo 0) if (bukkit_format[i] == '<') {
                finalloc = i
                break
            }
            if (finalloc != -1) {
                n_format_p_p = bukkit_format.substring(0, finalloc)
                if (name_code != null) {
                    val n_format = bukkit_format.substring(finalloc + 1).split(">").toTypedArray()[0].split(name_code)
                        .toTypedArray()
                    if (n_format.size > 0) {
                        n_format_p = n_format[0].replace(name_code, "").replace("{factions_relcolor}", "")
                    }
                    if (n_format.size > 1) {
                        n_format_s = n_format[1]
                    }
                }
            }
        }
        val tags = HashMap<String, String>()
        tags["name"] = c.name
        tags["nick"] = c.nickname
        tags["color"] = c.color
        tags["sender"] = sender.displayName
        tags["plainsender"] = sender.name
        tags["world"] = sender.world.name
        tags["bprefix"] = if (n_format_p_p == " ") "" else n_format_p_p.replace("  ", " ")
        tags["bprefix2"] = if (n_format_p == " ") "" else n_format_p.replace("  ", " ")
        tags["bsuffix"] = if (n_format_s == " ") "" else n_format_s.replace("  ", " ")

        if (!block_chat) {
            tags["prefix"] = tag(chat.getPlayerPrefix(sender))
            tags["suffix"] = tag(chat.getPlayerSuffix(sender))
            tags["groupprefix"] = tag(chat.getGroupPrefix(sender.world, chat.getPrimaryGroup(sender)))
            tags["groupsuffix"] = tag(chat.getGroupSuffix(sender.world, chat.getPrimaryGroup(sender)))
            for (g in chat.getPlayerGroups(sender)) {
                tags[g.toLowerCase() + "prefix"] = tag(chat.getGroupPrefix(sender.world, g))
                tags[g.toLowerCase() + "suffix"] = tag(chat.getGroupSuffix(sender.world, g))
            }
        }
        val ttt = textToTag()
        if (ttt.size > 0) {
            val p = HashSet<Player>()
            p.add(sender)
            var i = 1
            for (n in ttt.keys) {
                var tag = ""
                tag = try {
                    bukkit_format.split("°" + i + "º°").toTypedArray()[1].split("°" + (i + 1) + "º°").toTypedArray()[0]
                } catch (e: Exception) {
                    ""
                }
                tags[n] = tag
                i++
            }
        }
        val e = ChatMessageEvent(
            c,
            sender,
            message,
            format(c.format),
            c.format,
            bukkit_format,
            recipients,
            tags,
            cancelled
        )
        val effectiveGastou = gastou
        Bukkit.getScheduler().runTask(plugin, Runnable {
            Bukkit.getPluginManager().callEvent(e)
            realMessage0(e, c, effectiveGastou)
        })
    }

    fun performTell(player: FocaPlayer, other: FocaPlayer, message: Array<out String>) {
        // TODO
    }

    fun performTell(player: FocaPlayer, other: FocaPlayer, msg: String) {
        // todo
    }

    fun performMessage(player: FocaPlayer, message: String) {
        // TODO
    }

    fun checkChannel(string: String): Channel? {
        // TODO
        return null
    }

    fun muteAll() {

    }

    fun unmuteAll() {

    }

    private fun realMessage0(e: ChatMessageEvent, c: Channel, gastou: Boolean) {
        if (e.isCancelled) {
            return
        }
        val sender = e.sender
        val message = e.message
        var completa = e.format
            if (e.tags.contains("prefix") && e.tags.contains("groupprefix")) {
                if (e.getTagValue("prefix") == e.getTagValue("groupprefix")) {
                    e.setTagValue("prefix", "")
                }
            }
            if (e.tags.contains("suffix") && e.tags.contains("groupsuffix")) {
                if (e.getTagValue("suffix") == e.getTagValue("groupsuffix")) {
                    e.setTagValue("suffix", "")
                }
            }
        for (n in e.tags) completa =
            completa.replace("{$n}", ChatColor.translateAlternateColorCodes('&', e.getTagValue(n)))
        completa = completa.replace("{msg}", ColorUtils.translateAlternateChatColorsWithPermission(sender, message)!!)
        for (p in e.recipients) p.sendMessage(completa)
        if (c.delayPerMessage > 0 && !sender.hasPermission("channel." + c.name.toLowerCase() + ".nodelay") && !sender.hasPermission(
                "admin"
            )
        ) {
            getDelayManager().addPlayerDelay(sender.name, c)
        }
        if (c.maxDistance != 0.0) {
            if (showNoOneHearsYou()) {
                var show = false
                if (e.recipients.size == 0) {
                    show = true
                } else if (e.recipients.size == 1 && e.recipients.contains(sender)) {
                    show = true
                } else {
                    show = true
                    for (p in e.recipients) if (p !== sender && !getPlayerManager()
                            .isPlayerHiddenFromRecipients(p)
                    ) {
                        show = false
                        break
                    }
                }
                if (show) {
                    sender.sendMessage(msg("special"))
                }
            }
        }
        for (p in getPlayerManager().getOnlineSpys()) if (!e.recipients.contains(p)) {
            p.sendMessage(
                ChatColor.translateAlternateColorCodes(
                    '&', getFormat("spy")?.replace(
                        "{msg}", ChatColor.stripColor(
                            completa
                        )!!
                    )
                )
            )
        }
        if (gastou) {
            if (c.showCostMessage())
                sender.sendMessage(msg("message9").replace("@money", java.lang.Double.toString(c.costPerMessage)))
        }
        Bukkit.getConsoleSender().sendMessage(completa)
    }

    fun otherMessage(c: Channel, message: String?) {
        val recipients: MutableSet<Player> = HashSet()
        for (p in Bukkit.getOnlinePlayers()) {
            if (p.hasPermission("channel." + c.name.toLowerCase() + ".chat") || p.hasPermission("admin")) {
                recipients.add(p)
            }
        }
        val recipients2: MutableSet<Player> = HashSet()
        recipients2.addAll(recipients)

        for (p in recipients) p.sendMessage(message!!)
            Bukkit.getConsoleSender().sendMessage(message!!)
    }

    private fun tag(tag: String?): String = tag ?: ""

    private var showNoOneHearsYou = false
    private var sendFakeMessageToChat = false
    private var blockShortcutsWhenCancelled = false
    private var maintainSpyMode = false
    private var defaultChannel: Channel? = null
    private val formats = HashMap<String, String>()
    private val pm_formats = HashMap<String, String>()
    private val text_to_tag = HashMap<String, String>()

    fun getDefaultChannel(): Channel? = defaultChannel

    fun showNoOneHearsYou(): Boolean = showNoOneHearsYou

    fun sendFakeMessageToChat(): Boolean = sendFakeMessageToChat

    fun blockShortcutsWhenCancelled(): Boolean = blockShortcutsWhenCancelled

    fun format(msg: String): String? {
        var msg = msg
        for (f in formats.keys) msg = msg.replace("{$f}", formats[f]!!)
        return msg
    }

    fun getFormat(base_format: String): String? = formats[base_format.toLowerCase()]

    fun getPrivateMessageFormat(format: String): String? = pm_formats[format.toLowerCase()]

    fun textToTag(): HashMap<String, String>? {
        val h = HashMap<String, String>()
        h.putAll(text_to_tag)
        return h
    }

    fun load(all: Boolean) {
        val fc = Bukkit.getPluginManager().getPlugin("Legendchat")!!.config
        defaultChannel = getChannelByName(fc.getString("default_channel", "local")!!.toLowerCase())
        showNoOneHearsYou = fc.getBoolean("show_no_one_hears_you", true)
        sendFakeMessageToChat = fc.getBoolean("send_fake_message_to_chat", true)
        blockShortcutsWhenCancelled = fc.getBoolean("block_shortcuts_when_cancelled", true)
        maintainSpyMode = fc.getBoolean("maintain_spy_mode", false)
        formats.clear()
        pm_formats.clear()
        for (f in fc.getConfigurationSection("format")!!.getKeys(false)) formats[f.toLowerCase()] =
            fc.getString("format.$f")!!
        for (f in fc.getConfigurationSection("private_message_format")!!.getKeys(false)) pm_formats[f.toLowerCase()] =
            fc.getString("private_message_format.$f")!!
        for (f in fc.getStringList("text_to_tag")) {
            val s = f.split(";").toTypedArray()
            text_to_tag[s[0].toLowerCase()] = s[1]
        }
    }

    var econ: Economy? = null
    var chat: Chat? = null
    var block_econ = false
    var block_chat = false

    fun onEnable() {
        load(false)
        val file: File = File(plugin.getDataFolder(), "config.yml")
        if (!file.exists()) {
            try {
                plugin.saveResource("config_template.yml", false)
                val file2: File = File(plugin.getDataFolder(), "config_template.yml")
                file2.renameTo(File(plugin.getDataFolder(), "config.yml"))
            } catch (ignored: Exception) {
            }
        }
        try {
            if (!File(getDataFolder(), "Lang.yml").exists()) {
                plugin.saveResource("Lang.yml", false)
                plugin.getLogger().info("Saved Lang.yml")
            }
        } catch (e: Exception) {
        }
        val channels: File = File(getDataFolder(), "channels")
        if (!channels.exists()) {
            createPermanentChannel(Channel(this, "global", "g", "{default}", '7', true, 0.0, true, 0, 0.0, true, false))
            createPermanentChannel(
                Channel(
                    this,
                    "local",
                    "l",
                    "{default}",
                    'e',
                    true,
                    60.0,
                    false,
                    0,
                    0.0,
                    true,
                    false
                )
            )
        }
        loadChannels()
    }
}