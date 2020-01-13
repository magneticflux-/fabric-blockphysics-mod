package com.skaggsm.blockphysicsmod

import com.skaggsm.blockphysicsmod.BlockPhysicsMod.config
import com.skaggsm.blockphysicsmod.BlockPhysicsMod.log
import net.minecraft.server.MinecraftServer
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.*

val BlockPosComparator = compareBy(BlockPos::getY, BlockPos::getX, BlockPos::getZ)

data class PhysicsEvent(val world: World, val pos: BlockPos) : Comparable<PhysicsEvent> {
    val isAir: Boolean
        get() = world.isAir(pos)

    override fun compareTo(other: PhysicsEvent): Int = BlockPosComparator.compare(this.pos, other.pos)
}

fun addToPhysicsQueue(event: PhysicsEvent) {
    // Early check for useless physics event
    if (!event.isAir)
        physicsQueue.add(event)
}

fun addToPhysicsQueueBuffer(event: PhysicsEvent) {
    // Early check for useless physics event
    if (!event.isAir)
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
            i++
        }
    }

    if (i >= limit)
        log.info("Hit physics limit ($limit) this tick with $i falls. Deferred ${physicsQueue.size} to next tick.")
    else
        log.debug("Under physics limit ($limit) this tick with $i falls. Deferred ${physicsQueue.size} to next tick.")

    drainBuffer()
}

private fun drainBuffer() {
    physicsQueue.addAll(physicsQueueBuffer)
    physicsQueueBuffer.clear()
}
