package com.skaggsm.blockphysicsmod

import com.skaggsm.blockphysicsmod.BlockPhysicsMod.config
import com.skaggsm.blockphysicsmod.BlockPhysicsMod.log
import net.minecraft.server.MinecraftServer
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.*

data class PhysicsEvent(val world: World, val pos: BlockPos) : Comparable<PhysicsEvent> {
    override fun compareTo(other: PhysicsEvent): Int = this.pos.y.compareTo(other.pos.y)
}

fun addToPhysicsQueue(event: PhysicsEvent) {
    physicsQueue.add(event)
}

fun addToPhysicsQueueBuffer(event: PhysicsEvent) {
    physicsQueueBuffer.add(event)
}

private val physicsQueueBuffer = ArrayDeque<PhysicsEvent>(config.physicsLimitPerTick)
private val physicsQueue = PriorityQueue<PhysicsEvent>(config.physicsLimitPerTick)

@Suppress("UNUSED_PARAMETER")
fun processPhysicsQueue(server: MinecraftServer) {
    val limit = config.physicsLimitPerTick
    var i = 0

    while (i < limit) {
        val event = physicsQueue.poll() ?: break
        if (tryFall(event.world, event.pos)) {
            log.trace("Block fell (was ${event.world.getBlockState(event.pos)})")
            i++
        } else {
            log.trace("Block did NOT fall (was ${event.world.getBlockState(event.pos)})")
        }
    }

    if (i >= limit)
        log.debug("Hit physics limit ($limit) this tick with $i falls. Deferred ${physicsQueue.size} to next tick.")
    else
        log.debug("Under physics limit ($limit) this tick with $i falls. Deferred ${physicsQueue.size} to next tick.")

    drainBuffer()
}

private fun drainBuffer() {
    physicsQueue.addAll(physicsQueueBuffer)
    physicsQueueBuffer.clear()
}
