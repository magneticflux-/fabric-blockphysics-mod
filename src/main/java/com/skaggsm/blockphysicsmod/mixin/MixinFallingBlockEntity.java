package com.skaggsm.blockphysicsmod.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemConvertible;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * This mixin makes falling blocks use the "falling block position" (which ends up being the position that the block was in before starting to fall) to remove its old block self from the world.
 * This means that falling blocks can have their positions set immediately elsewhere, and they still destroy their original block.
 * It is useful for making falling blocks fall in locations other than straight down.
 */
@Mixin(FallingBlockEntity.class)
public abstract class MixinFallingBlockEntity extends Entity {
    public MixinFallingBlockEntity(EntityType<?> type, World world) {
        super(type, world);
    }

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

    @Override
    public Box getCollisionBox() {
        return this.getBoundingBox();
    }


    @Redirect(
            method = "tick",
            at = @At(
                    target = "Lnet/minecraft/entity/FallingBlockEntity;dropItem(Lnet/minecraft/item/ItemConvertible;)Lnet/minecraft/entity/ItemEntity;",
                    ordinal = 1,
                    value = "INVOKE"
            )
    )
    public ItemEntity replaceItemDropWhenBlockOccupied(FallingBlockEntity fallingBlockEntity, ItemConvertible item) {
        this.removed = false;
        fallingBlockEntity.setPosition(
                fallingBlockEntity.getX(),
                fallingBlockEntity.getY() + 1,
                fallingBlockEntity.getZ()
        );
        return null;
    }
}

