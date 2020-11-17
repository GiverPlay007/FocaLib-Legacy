package me.giverplay.focalib.player

import me.giverplay.focalib.FocaLib
import org.apache.commons.lang.Validate
import org.bukkit.entity.Player

class PlayerManager(plugin: FocaLib)
{
    private val plugin: FocaLib = plugin
    private val players: HashMap<String, FocaPlayer> = HashMap()

    fun registerPlayer(player: FocaPlayer)
    {
        Validate.notNull(player, "O jogador a ser registrado não pode ser nulo!")
        Validate.isTrue(!players.containsKey(player.name), "Esse jogador já foi registrado!")

        synchronized(players)
        {
            players[player.name] = player
        }

        plugin.logger.info("Jogador ${player.name} foi registrado!")
    }

    fun unregisterPlayer(player: FocaPlayer)
    {
        Validate.notNull(player, "É impossível desregistrar um jogador nulo!")

        if(!players.containsKey(player.name))
        {
            plugin.logger.info("Houve uma tentativa de desregistrar ${player.name}, mas esse não foi registrado!")
            return
        }

        synchronized(players)
        {
            players.remove(player)
        }

        plugin.logger.info("Jogador ${player.name} foi desregistrado!")
    }

    fun getPlayer(name: String): FocaPlayer? = players[name]
}