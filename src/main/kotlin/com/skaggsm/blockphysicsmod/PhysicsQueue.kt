package com.skaggsm.blockphysicsmod

import net.minecraft.server.MinecraftServer
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.*

data class PhysicsEvent(val world: World, val pos: BlockPos)

internal val physicsQueue: Queue<PhysicsEvent> = LinkedList()

fun processPhysicsQueue(server: MinecraftServer) {
    val limit = BlockPhysicsMod.config.config.physicsLimitPerTick
    var i = 0
    while (i < limit) {
        val event = physicsQueue.poll() ?: break
        if (tryFall(event.world, event.pos))
            i++
    }
}
