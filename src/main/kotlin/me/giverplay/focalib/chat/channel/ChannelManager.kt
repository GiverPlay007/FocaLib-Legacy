package me.giverplay.focalib.chat.channel

import br.com.devpaulo.legendchat.Legendchat
import br.com.devpaulo.legendchat.LegendchatAPI
import br.com.devpaulo.legendchat.events.ChatMessageEvent
import br.com.devpaulo.legendchat.listeners.Listeners
import me.giverplay.focalib.FocaLib
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.event.player.AsyncPlayerChatEvent
import java.io.File

class ChannelManager(private val plugin: FocaLib) {
    private val channels = HashMap<String, Channel>()
    fun createChannel(c: Channel) {
        if (existsChannel(c.name)) return
        channels[c.name.toLowerCase()] = c
    }

    fun createPermanentChannel(c: Channel) {
        if (existsChannel(c.name)) return
        channels[c.name.toLowerCase()] = c
        val channel =
            File(LegendchatAPI.getPlugin().dataFolder, "channels" + File.separator + c.name.toLowerCase() + ".yml")
        if (!channel.exists()) {
            try {
                channel.createNewFile()
            } catch (e: Exception) {
            }
            val channel2 = YamlConfiguration.loadConfiguration(channel)
            channel2["name"] = c.name
            channel2["nickname"] = c.nickname
            channel2["format"] = c.format
            channel2["color"] = c.stringColor
            channel2["shortcutAllowed"] = c.isShortcutAllowed
            channel2["needFocus"] = c.isFocusNeeded
            channel2["distance"] = c.maxDistance
            channel2["crossworlds"] = c.isCrossworlds
            channel2["delayPerMessage"] = c.delayPerMessage
            channel2["costPerMessage"] = c.costPerMessage
            channel2["showCostMessage"] = c.showCostMessage
            try {
                channel2.save(channel)
            } catch (e: Exception) {
            }
        }
    }

    fun deleteChannel(c: Channel) {
        if (!existsChannel(c.name)) return
        for (p in c.getPlayersFocusedInChannel()) LegendchatAPI.getPlayerManager()
            .setPlayerFocusedChannel(p, LegendchatAPI.getDefaultChannel(), false)
        channels.remove(c.name.toLowerCase())
        File(LegendchatAPI.getPlugin().dataFolder, "channels" + File.separator + c.name.toLowerCase() + ".yml").delete()
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
        val bungee = LegendchatAPI.getPlugin().config.getString("bungeecord.channel")!!
        channels.clear()
        for (channel in File(LegendchatAPI.getPlugin().dataFolder, "channels").listFiles()) {
            if (channel.name.toLowerCase().endsWith(".yml")) {
                if (channel.name.toLowerCase() != channel.name) channel.renameTo(
                    File(
                        LegendchatAPI.getPlugin().dataFolder,
                        "channels" + File.separator + channel.name.toLowerCase()
                    )
                )
                loadChannel(channel, bungee)
            }
        }
        for (p in Bukkit.getOnlinePlayers()) LegendchatAPI.getPlayerManager()
            .setPlayerFocusedChannel(p, LegendchatAPI.getDefaultChannel(), false)
    }

    private fun loadChannel(channel: File, bungee: String) {
        val channel2 = YamlConfiguration.loadConfiguration(channel)
        createPermanentChannel(
            Channel(
                channel2.getString("name"),
                channel2.getString("nickname")!!,
                channel2.getString("format")!!,
                channel2.getString("color")!!,
                channel2.getBoolean("shortcutAllowed"),
                channel2.getBoolean("needFocus"),
                channel2.getDouble("distance"),
                channel2.getBoolean("crossworlds"),
                channel2.getInt("delayPerMessage"),
                channel2.getDouble("costPerMessage")
                    .toInt(),
                channel2.getBoolean("showCostMessage")
            )
        )
    }

    fun translateStringColor(color: String): String {
        return " " // TODO
    }

    fun realMessage(channel: Channel, sender: Player?, message: String?, bukkitFormat: String?, cancelled: Boolean) {
        // TODO
    }

    fun otherMessage(channel: Channel, message: String?) {
        // TODO
    }

    fun fakeMessage(channel: Channel, sender: Player?, message: String?) {
        // TODO
    }

    fun translateChatColorToStringColor(c: ChatColor?): String {
        return " " // TODO
    }

    fun fakeMessage(c: Channel, sender: Player?, message: String?) {
        if (!LegendchatAPI.sendFakeMessageToChat()) {
            c.sendMessage(sender, message, "", false)
            return
        }
        val p = HashSet<Player?>()
        p.add(sender)
        val event = AsyncPlayerChatEvent(
            true,
            sender!!, "legendchat", p
        )
        Listeners.addFakeChat(event, false)
        Bukkit.getScheduler().runTaskAsynchronously(LegendchatAPI.getPlugin(), Runnable {
            Bukkit.getPluginManager().callEvent(event)
            c.sendMessage(sender, message, event.format, Listeners.getFakeChat(event))
            Listeners.removeFakeChat(event)
        })
    }

    fun realMessage(c: Channel, sender: Player, message: String?, bukkit_format: String, cancelled: Boolean) {
        if (!sender.hasPermission("legendchat.channel." + c.name.toLowerCase() + ".chat") && !sender.hasPermission("legendchat.admin")) {
            sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("error2"))
            return
        }
        if (sender.hasPermission("legendchat.channel." + c.name.toLowerCase() + ".blockwrite") && !sender.hasPermission(
                "legendchat.admin"
            )
        ) {
            sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("error2"))
            return
        }
        if (c.isFocusNeeded) {
            if (LegendchatAPI.getPlayerManager().getPlayerFocusedChannel(sender) !== c) {
                sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("error12"))
                return
            }
        }
        val delay = LegendchatAPI.getDelayManager().getPlayerDelayFromChannel(sender.name, c)
        if (delay > 0) {
            sender.sendMessage(
                LegendchatAPI.getMessageManager().getMessage("error11").replace("@time", Integer.toString(delay))
            )
            return
        }
        if (LegendchatAPI.getMuteManager().isPlayerMuted(sender.name)) {
            val time = LegendchatAPI.getMuteManager().getPlayerMuteTimeLeft(sender.name)
            if (time == 0) {
                sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("mute_error4"))
            } else {
                sender.sendMessage(
                    LegendchatAPI.getMessageManager().getMessage("mute_error5").replace("@time", Integer.toString(time))
                )
            }
            return
        }
        if (LegendchatAPI.getMuteManager().isServerMuted) {
            sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("mute_error8"))
            return
        }
        if (LegendchatAPI.getIgnoreManager().hasPlayerIgnoredChannel(sender, c)) {
            sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("error14"))
            return
        }
        val recipients: MutableSet<Player> = HashSet()
        for (p in Bukkit.getOnlinePlayers()) {
            if (p.hasPermission("legendchat.channel." + c.name.toLowerCase() + ".chat") || p.hasPermission("legendchat.admin")) {
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
            if (LegendchatAPI.getIgnoreManager().hasPlayerIgnoredPlayer(p, sender.name)) {
                recipients.remove(p)
                continue
            }
            if (LegendchatAPI.getIgnoreManager().hasPlayerIgnoredChannel(p, c)) {
                recipients.remove(p)
                continue
            }
            if (c.isFocusNeeded) {
                if (LegendchatAPI.getPlayerManager().getPlayerFocusedChannel(p) !== c) {
                    recipients.remove(p)
                }
            }
        }
        var gastou = false
        if (!Legendchat.block_econ && c.getMessageCost() > 0) {
            if (!sender.hasPermission("legendchat.channel." + c.name.toLowerCase() + ".free") && !sender.hasPermission("legendchat.admin")) {
                if (Legendchat.econ.getBalance(sender.name) < c.getMessageCost()) {
                    sender.sendMessage(
                        LegendchatAPI.getMessageManager().getMessage("error3")
                            .replace("@price", java.lang.Double.toString(c.getMessageCost()))
                    )
                    return
                }
                Legendchat.econ.withdrawPlayer(sender.name, c.getMessageCost())
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
        tags["bprefix"] =
            if (LegendchatAPI.forceRemoveDoubleSpacesFromBukkit()) if (n_format_p_p == " ") "" else n_format_p_p.replace(
                "  ",
                " "
            ) else n_format_p_p
        tags["bprefix2"] =
            if (LegendchatAPI.forceRemoveDoubleSpacesFromBukkit()) if (n_format_p == " ") "" else n_format_p.replace(
                "  ",
                " "
            ) else n_format_p
        tags["bsuffix"] =
            if (LegendchatAPI.forceRemoveDoubleSpacesFromBukkit()) if (n_format_s == " ") "" else n_format_s.replace(
                "  ",
                " "
            ) else n_format_s
        tags["server"] = LegendchatAPI.getMessageManager().getMessage("bungeecord_server")
        tags["time_hour"] = Integer.toString(Calendar.getInstance()[Calendar.HOUR_OF_DAY])
        tags["time_min"] = Integer.toString(Calendar.getInstance()[Calendar.MINUTE])
        tags["time_sec"] = Integer.toString(Calendar.getInstance()[Calendar.SECOND])
        tags["date_day"] = Integer.toString(Calendar.getInstance()[Calendar.DAY_OF_MONTH])
        tags["date_month"] = Integer.toString(Calendar.getInstance()[Calendar.MONTH])
        tags["date_year"] = Integer.toString(Calendar.getInstance()[Calendar.YEAR])
        if (!Legendchat.block_chat) {
            tags["prefix"] = tag(Legendchat.chat.getPlayerPrefix(sender))
            tags["suffix"] = tag(Legendchat.chat.getPlayerSuffix(sender))
            tags["groupprefix"] =
                tag(Legendchat.chat.getGroupPrefix(sender.world, Legendchat.chat.getPrimaryGroup(sender)))
            tags["groupsuffix"] =
                tag(Legendchat.chat.getGroupSuffix(sender.world, Legendchat.chat.getPrimaryGroup(sender)))
            for (g in Legendchat.chat.getPlayerGroups(sender)) {
                tags[g.toLowerCase() + "prefix"] = tag(Legendchat.chat.getGroupPrefix(sender.world, g))
                tags[g.toLowerCase() + "suffix"] = tag(Legendchat.chat.getGroupSuffix(sender.world, g))
            }
        }
        val ttt = LegendchatAPI.textToTag()
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
            LegendchatAPI.format(c.format),
            c.format,
            bukkit_format,
            recipients,
            tags,
            cancelled
        )
        val effectiveGastou = gastou
        Bukkit.getScheduler().runTask(LegendchatAPI.getPlugin(), Runnable {
            Bukkit.getPluginManager().callEvent(e)
            realMessage0(e, c, effectiveGastou)
        })
    }

    private fun realMessage0(e: ChatMessageEvent, c: Channel, gastou: Boolean) {
        if (e.isCancelled) {
            return
        }
        val sender = e.sender
        val message = e.message
        var completa = e.format
        if (LegendchatAPI.blockRepeatedTags()) {
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
        }
        for (n in e.tags) completa =
            completa.replace("{$n}", ChatColor.translateAlternateColorCodes('&', e.getTagValue(n)))
        completa = completa.replace("{msg}", translateAlternateChatColorsWithPermission(sender, message)!!)
        for (p in e.recipients) p.sendMessage(completa)
        if (c.delayPerMessage > 0 && !sender.hasPermission("legendchat.channel." + c.name.toLowerCase() + ".nodelay") && !sender.hasPermission(
                "legendchat.admin"
            )
        ) {
            LegendchatAPI.getDelayManager().addPlayerDelay(sender.name, c)
        }
        if (c.maxDistance != 0.0) {
            if (LegendchatAPI.showNoOneHearsYou()) {
                var show = false
                if (e.recipients.size == 0) {
                    show = true
                } else if (e.recipients.size == 1 && e.recipients.contains(sender)) {
                    show = true
                } else {
                    show = true
                    for (p in e.recipients) if (p !== sender && !LegendchatAPI.getPlayerManager()
                            .isPlayerHiddenFromRecipients(p)
                    ) {
                        show = false
                        break
                    }
                }
                if (show) {
                    sender.sendMessage(LegendchatAPI.getMessageManager().getMessage("special"))
                }
            }
        }
        for (p in LegendchatAPI.getPlayerManager().getOnlineSpys()) if (!e.recipients.contains(p)) {
            p.sendMessage(
                ChatColor.translateAlternateColorCodes(
                    '&', LegendchatAPI.getFormat("spy").replace(
                        "{msg}",
                        ChatColor.stripColor(completa)!!
                    )
                )
            )
        }
        if (gastou) {
            if (c.showCostMessage()) {
                sender.sendMessage(
                    LegendchatAPI.getMessageManager().getMessage("message9")
                        .replace("@money", java.lang.Double.toString(c.costPerMessage))
                )
            }
        }
        if (LegendchatAPI.logToBukkit()) {
            Bukkit.getConsoleSender().sendMessage(completa)
        }
    }

    fun otherMessage(c: Channel, message: String?) {
        val recipients: MutableSet<Player> = HashSet()
        for (p in Bukkit.getOnlinePlayers()) {
            if (p.hasPermission("legendchat.channel." + c.name.toLowerCase() + ".chat") || p.hasPermission("legendchat.admin")) {
                recipients.add(p)
            }
        }
        val recipients2: MutableSet<Player> = HashSet()
        recipients2.addAll(recipients)
        for (p in recipients2) {
            if (LegendchatAPI.getIgnoreManager().hasPlayerIgnoredChannel(p, c)) {
                recipients.remove(p)
                continue
            }
            if (c.isFocusNeeded) {
                if (LegendchatAPI.getPlayerManager().getPlayerFocusedChannel(p) !== c) {
                    recipients.remove(p)
                }
            }
        }

        for (p in recipients) p.sendMessage(message!!)
        if (LegendchatAPI.logToBukkit()) {
            Bukkit.getConsoleSender().sendMessage(message!!)
        }
    }

    private fun tag(tag: String?): String {
        return tag ?: ""
    }
}