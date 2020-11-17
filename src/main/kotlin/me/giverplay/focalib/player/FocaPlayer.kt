package me.giverplay.focalib.player

import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer
import org.bukkit.entity.Player

class FocaPlayer(player: Player)
{
    val player: Player = player
    val settings: PlayerSettings = PlayerSettings()

    val name
      get() = player.name

    val craftPlayer
      get() = player as CraftPlayer

    val ping
      get() = craftPlayer.handle.ping
}