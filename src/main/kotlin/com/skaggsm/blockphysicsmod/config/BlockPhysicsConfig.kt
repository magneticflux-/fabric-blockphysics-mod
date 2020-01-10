package com.skaggsm.blockphysicsmod.config

import com.skaggsm.blockphysicsmod.BlockPhysicsMod.MODID
import me.sargunvohra.mcmods.autoconfig1u.ConfigData
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config
import me.sargunvohra.mcmods.autoconfig1u.annotation.ConfigEntry

@Config(name = MODID)
class BlockPhysicsConfig : ConfigData {
    @ConfigEntry.Category("physics_parameters")
    val physicsLimitPerTick: Int = 250
}
