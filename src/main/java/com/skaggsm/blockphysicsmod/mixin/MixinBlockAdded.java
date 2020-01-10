package com.skaggsm.blockphysicsmod.mixin;

import com.skaggsm.blockphysicsmod.PhysicsKt;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FallingBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

/**
 * Created by Mitchell Skaggs on 5/22/2019.
 */
@Mixin({Block.class, FallingBlock.class})
public class MixinBlockAdded {
    @Inject(method = "onBlockAdded", at = @At("HEAD"), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    public void inOnBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean moved, CallbackInfo ci) {
        PhysicsKt.onBlockAdded(state, world, pos, oldState, moved, ci);
    }
}
