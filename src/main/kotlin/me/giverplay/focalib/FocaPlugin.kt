package me.giverplay.focalib

import org.bukkit.plugin.java.JavaPlugin

class FocaPlugin: JavaPlugin()
{
    override fun onEnable()
    {
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