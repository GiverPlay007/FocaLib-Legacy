package me.giverplay.focalib.player

import me.giverplay.focalib.FocaLib
import me.giverplay.focalib.listeners.ListenerPlayerManager
import me.giverplay.focalib.utils.Messages.Companion.msg
import org.apache.commons.lang.Validate
import org.bukkit.entity.Player

class PlayerManager(plugin: FocaLib)
{
    private val plugin: FocaLib = plugin
    private val players: HashMap<String, FocaPlayer> = HashMap()

    fun registerPlayer(player: FocaPlayer)
    {
        Validate.notNull(player, msg("error.internal.nullplayer"))
        Validate.isTrue(!players.containsKey(player.name), msg("error.internal.regplayer", player.name))

        synchronized(players)
        {
            players[player.name] = player
        }

        plugin.logger.info(msg("debug.internal.regplayer", player.name, msg("info.internal.reg")))
    }

    fun unregisterPlayer(player: Player)
    {
        Validate.notNull(player, msg("error.internal.nullplayer"))

        if(!players.containsKey(player.name))
        {
            plugin.logger.info(msg("error.internal.regnotfound", player.name))
            return
        }

        synchronized(players)
        {
            players.remove(player.name)
        }

        plugin.logger.info(msg("debug.internal.regplayer", player.name, msg("info.internal.unreg")))
    }

    fun getPlayer(name: String): FocaPlayer? = synchronized(players) { return players[name] }

    init {
        plugin.registerEvent(ListenerPlayerManager(this))
    }
}