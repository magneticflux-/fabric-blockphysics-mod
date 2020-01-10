package com.skaggsm.blockphysicsmod.config

import com.skaggsm.blockphysicsmod.BlockPhysicsMod.MODID
import io.github.prospector.modmenu.api.ModMenuApi
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.gui.screen.Screen
import java.util.function.Function

/**
 * Created by Mitchell Skaggs on 1/9/2020.
 */
@Environment(EnvType.CLIENT)
class BlockPhysicsModMenu : ModMenuApi {
    override fun getModId(): String = MODID

    override fun getConfigScreenFactory(): Function<Screen, out Screen> =
            Function { screen ->
                AutoConfig.getConfigScreen(BlockPhysicsConfig::class.java, screen).get()
            }
}
