package com.skaggsm.blockphysicsmod.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.FallingBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FallingBlockEntity.class)
public abstract class MixinFallingBlockEntity extends Entity {
    private boolean delayedRemove = false;

    public MixinFallingBlockEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    /**
     * This mixin makes falling blocks use the "falling block position" (which ends up being the position that the block was in before starting to fall) to remove its old block self from the world instead of the current position.
     * This means that falling blocks can have their positions set immediately elsewhere, and they still destroy their original block, so falling blocks can fall in locations other than straight down.
     */
    @Redirect(
            method = "tick",
            at = @At(
                    target = "(Lnet/minecraft/entity/Entity;)Lnet/minecraft/util/math/BlockPos;",
                    ordinal = 0,
                    value = "NEW"
            )
    )
    public BlockPos inTick(Entity entity) {
        return new BlockPos(((FallingBlockEntity) entity).getFallingBlockPos());
    }

    /**
     * Have a collision box for testing if a falling block is in the way.
     */
    @Override
    public Box getCollisionBox() {
        return this.getBoundingBox();
    }

    /**
     * Don't remove immediately (or run whatever removal code there may be at runtime), just set a flag.
     */
    @Redirect(
            method = "tick",
            at = @At(
                    target = "Lnet/minecraft/entity/FallingBlockEntity;remove()V",
                    ordinal = 2,
                    value = "INVOKE"
            )
    )
    public void delayRemove(FallingBlockEntity entity) {
        // Don't run the real method, just set a flag.
        // We'll take care of the real method after our logic
        assert (Object) entity == this;
        this.delayedRemove = true;
    }

    /**
     * Redirect for logging of falling block outcomes.
     */
    @Redirect(
            method = "tick",
            at = @At(
                    target = "Lnet/minecraft/block/FallingBlock;onDestroyedOnLanding(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V",
                    ordinal = 0,
                    value = "INVOKE"
            )
    )
    public void redirectOnDestroyedOnLanding(FallingBlock fallingBlock, World world, BlockPos pos) {
        //System.out.println("FallingBlockEntity destroyed on landing");
        fallingBlock.onDestroyedOnLanding(world, pos);
    }

    /**
     * Redirect for logging of falling block outcomes.
     */
    @Redirect(
            method = "tick",
            at = @At(
                    target = "Lnet/minecraft/entity/FallingBlockEntity;dropItem(Lnet/minecraft/item/ItemConvertible;)Lnet/minecraft/entity/ItemEntity;",
                    ordinal = 0,
                    value = "INVOKE"
            )
    )
    public ItemEntity redirectItemDropWhenSetBlockStateFailed(FallingBlockEntity fallingBlockEntity, ItemConvertible item) {
        //System.out.println("FallingBlockEntity's setBlockState failed for some reason");
        return fallingBlockEntity.dropItem(item);
    }

    /**
     * Don't always destroy the block. This code tests if it should destroy the falling block, or shift it around.
     */
    @Redirect(
            method = "tick",
            at = @At(
                    target = "Lnet/minecraft/entity/FallingBlockEntity;dropItem(Lnet/minecraft/item/ItemConvertible;)Lnet/minecraft/entity/ItemEntity;",
                    ordinal = 1,
                    value = "INVOKE"
            )
    )
    public ItemEntity redirectItemDropWhenBlockOccupied(FallingBlockEntity fallingBlockEntity, ItemConvertible item) {
        //System.out.println("FallingBlockEntity stopped inside a non-replaceable block");

        BlockPos currentPos = new BlockPos(this);
        BlockState state = this.world.getBlockState(currentPos);
        boolean fullBlock = state.isFullCube(this.world, currentPos);
        boolean isAir = state.isAir();

        if (fullBlock) {
            // Undo breaking it and try to move it somewhere else
            this.delayedRemove = false;

            fallingBlockEntity.setPosition(
                    fallingBlockEntity.getX(),
                    fallingBlockEntity.getY() + 1,
                    fallingBlockEntity.getZ()
            );
            fallingBlockEntity.setVelocity(Vec3d.ZERO);

            return null;
        } else if (isAir) {
            // Undo breaking it and don't move it somewhere else
            this.delayedRemove = false;
            return null;
        } else {
            // It is a torch or slab or something, so break and remove the falling block
            return fallingBlockEntity.dropItem(item);
        }
    }

    /**
     * Perform a remove if necessary at the end of the tick.
     */
    @Inject(
            method = "tick",
            at = @At(
                    value = "TAIL"
            )
    )
    public void checkForDelayedRemoveAtTail(CallbackInfo ci) {
        if (this.delayedRemove)
            this.remove();
        this.delayedRemove = false;
    }

    /**
     * This fixes a Mojang bug in which falling sand wouldn't break sea grass because it thinks the falling blocks are the same type (air), so a replacement wouldn't do anything. This makes it see that the type is actually sand.
     */
    @Redirect(
            method = "tick",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/item/ItemStack;EMPTY:Lnet/minecraft/item/ItemStack;",
                    ordinal = 0
            )
    )
    public ItemStack useFallingBlockItemStack() {
        FallingBlockEntity this_ = (FallingBlockEntity) (Object) this;
        return new ItemStack(this_.getBlockState().getBlock());
    }
}

