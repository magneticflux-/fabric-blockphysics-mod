package com.skaggsm.blockphysicsmod.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import static com.skaggsm.blockphysicsmod.PhysicsKt.onBlockRemoved;

/**
 * Created by Mitchell Skaggs on 5/22/2019.
 */
@Mixin({Block.class})
public class MixinBlockRemoved {
    @Inject(method = "onBlockRemoved", at = @At("HEAD"), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    public void inOnBlockAdded(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved, CallbackInfo ci) {
        onBlockRemoved(state, world, pos, newState, moved, ci);
    }
}
