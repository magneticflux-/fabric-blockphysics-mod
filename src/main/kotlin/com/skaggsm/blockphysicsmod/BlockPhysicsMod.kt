package com.skaggsm.blockphysicsmod

import com.skaggsm.blockphysicsmod.config.BlockPhysicsConfig
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig
import me.sargunvohra.mcmods.autoconfig1u.ConfigHolder
import me.sargunvohra.mcmods.autoconfig1u.serializer.Toml4jConfigSerializer
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.server.ServerTickCallback
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

/**
 * Created by Mitchell Skaggs on 5/29/2019.
 */
object BlockPhysicsMod : ModInitializer {
    const val MODID: String = "fabric-blockphysics-mod"

    val log: Logger = LogManager.getLogger(MODID)
    private lateinit var config_: ConfigHolder<BlockPhysicsConfig>
    val config: BlockPhysicsConfig
        get() = config_.config

    override fun onInitialize() {
        config_ = AutoConfig.register(BlockPhysicsConfig::class.java, ::Toml4jConfigSerializer)

        ServerTickCallback.EVENT.register(ServerTickCallback { processPhysicsQueue(it) })
    }
}
