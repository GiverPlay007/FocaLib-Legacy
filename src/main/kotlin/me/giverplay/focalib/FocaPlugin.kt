package me.giverplay.focalib

import me.giverplay.focalib.utils.Messages
import org.bukkit.plugin.java.JavaPlugin

class FocaPlugin: JavaPlugin()
{
    override fun onEnable()
    {
        Messages.setup(this)
        instance = this
        lib = FocaLib(this)
        lib?.enable()
    }

    override fun onDisable()
    {
        lib?.disable()
        lib = null
    }

    companion object {
        var lib: FocaLib? = null
            private set

        var instance: FocaPlugin? = null
            private set
    }
}