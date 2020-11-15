package me.giverplay.focalib

import org.bukkit.plugin.java.JavaPlugin;

class FocaLib: JavaPlugin()
{
    override fun onEnable()
    {
        logger.info("Habilitando!")
    }

    override fun onDisable()
    {
        logger.info("Agora sim")
    }
}