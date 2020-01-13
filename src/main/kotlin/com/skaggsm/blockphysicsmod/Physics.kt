package com.skaggsm.blockphysicsmod

import net.minecraft.block.BlockState
import net.minecraft.block.FallingBlock.canFallThrough
import net.minecraft.block.Material
import net.minecraft.entity.FallingBlockEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction.UP
import net.minecraft.util.math.Vec3i
import net.minecraft.world.EntityView
import net.minecraft.world.World
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import kotlin.streams.toList

val allExtendedNeighbors = listOf(
        Vec3i(-1, -1, -1),
        Vec3i(-1, -1, 0),
        Vec3i(-1, -1, 1),
        Vec3i(-1, 0, -1),
        Vec3i(-1, 0, 0),
        Vec3i(-1, 0, 1),
        Vec3i(-1, 1, -1),
        Vec3i(-1, 1, 0),
        Vec3i(-1, 1, 1),
        Vec3i(0, -1, -1),
        Vec3i(0, -1, 0),
        Vec3i(0, -1, 1),
        Vec3i(0, 0, -1),
        // Vec3i(0, 0, 0), // Origin
        Vec3i(0, 0, 1),
        Vec3i(0, 1, -1),
        Vec3i(0, 1, 0),
        Vec3i(0, 1, 1),
        Vec3i(1, -1, -1),
        Vec3i(1, -1, 0),
        Vec3i(1, -1, 1),
        Vec3i(1, 0, -1),
        Vec3i(1, 0, 0),
        Vec3i(1, 0, 1),
        Vec3i(1, 1, -1),
        Vec3i(1, 1, 0),
        Vec3i(1, 1, 1)
)

val dirtSupport = listOf(
        Vec3i(-1, -1, -1),
        Vec3i(-1, -1, 0),
        Vec3i(-1, -1, 1),
        Vec3i(0, -1, -1),
        Vec3i(0, -1, 1),
        Vec3i(1, -1, -1),
        Vec3i(1, -1, 0),
        Vec3i(1, -1, 1)
)
val sandSupport = listOf(
        Vec3i(-1, -1, 0),
        Vec3i(0, -1, -1),
        Vec3i(0, -1, 1),
        Vec3i(1, -1, 0)
)
val stoneSupport = listOf(
        Vec3i(-1, -1, -1),
        Vec3i(-1, -1, 0),
        Vec3i(-1, -1, 1),
        Vec3i(-1, 0, -1),
        Vec3i(-1, 0, 0),
        Vec3i(-1, 0, 1),
        Vec3i(0, -1, -1),
        Vec3i(0, -1, 0),
        Vec3i(0, -1, 1),
        Vec3i(0, 0, -1),
        // Vec3i(0, 0, 0), // Origin
        Vec3i(0, 0, 1),
        Vec3i(1, -1, -1),
        Vec3i(1, -1, 0),
        Vec3i(1, -1, 1),
        Vec3i(1, 0, -1),
        Vec3i(1, 0, 0),
        Vec3i(1, 0, 1)
)

/**
 * @return true if it fell, false otherwise
 */
fun tryFall(world: World, pos: BlockPos): Boolean {
    assert(!world.isClient) { "Physics method was called on the wrong side!" }

    val potentialFall = findFallVector(world, pos)
    return if (potentialFall != null) {
        fall(world, pos, potentialFall)
        true
    } else false
}

fun fall(world: World, pos: BlockPos, fallVector: Vec3i) {
    val state = world.getBlockState(pos)
    assert(!state.isAir) { "Tried to make air fall!" }

    val collisions = (world as EntityView).getEntityCollisions(null, Box(pos.add(fallVector)), emptySet()).toList()
    if (collisions.isEmpty()) {
        val e = FallingBlockEntity(world, pos.x + 0.5, pos.y + 0.0, pos.z + 0.5, state)
        e.setPosition(e.x + fallVector.x, e.y + fallVector.y, e.z + fallVector.z)
        world.spawnEntity(e)
    } else {
        schedulePhysicsLater(world, pos) // Needs to be later to avoid looping the check up to the physics limit
    }
}

fun findFallVector(world: World, pos: BlockPos): Vec3i? {
    val state = world.getBlockState(pos)

    if (state.block.blastResistance >= 600F || state.isAir)
        return null

    val below = pos.down()
    val belowState = world.getBlockState(below)

    return when (state.material) {
        Material.SAND // Sand, gravel
        -> {
            if (canFallThrough(belowState)) Vec3i.ZERO
            else {
                sandSupport.firstOrNull {
                    val supportBelow = pos.add(it)
                    val blockingAbove = supportBelow.up()

                    canFallThrough(world.getBlockState(supportBelow)) && canFallThrough(world.getBlockState(blockingAbove))
                }?.offset(UP, 1)
            }
        }
        Material.EARTH, // Dirt, coarse dirt, farmland
        Material.ORGANIC // Grass, mycelium
        -> {
            if (canFallThrough(belowState)) {
                val supports = dirtSupport.count {
                    !canFallThrough(world.getBlockState(pos.add(it)))
                }
                if (supports < 2)
                    Vec3i.ZERO
                else null
            } else null
        }
        Material.STONE // Stone, ores, bricks
        -> {
            if (canFallThrough(belowState)) {
                val supports = stoneSupport.count {
                    !canFallThrough(world.getBlockState(pos.add(it)))
                }
                if (supports < 1)
                    Vec3i.ZERO
                else null
            } else null
        }
        else -> null
    }
}

fun schedulePhysicsNow(world: World, pos: BlockPos) = addToPhysicsQueue(PhysicsEvent(world, pos))

fun schedulePhysicsLater(world: World, pos: BlockPos) = addToPhysicsQueueBuffer(PhysicsEvent(world, pos))

@Suppress("UNUSED_PARAMETER")
fun onBlockAdded(state: BlockState, world: World, pos: BlockPos, oldState: BlockState, moved: Boolean, ci: CallbackInfo) {
    schedulePhysicsNow(world, pos)
    allExtendedNeighbors.forEach { schedulePhysicsNow(world, pos.add(it)) }
}

@Suppress("UNUSED_PARAMETER")
fun onBlockRemoved(state: BlockState, world: World, pos: BlockPos, newState: BlockState, moved: Boolean, ci: CallbackInfo) {
    allExtendedNeighbors.forEach { schedulePhysicsNow(world, pos.add(it)) }
}
