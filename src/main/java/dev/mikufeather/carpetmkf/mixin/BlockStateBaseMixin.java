package dev.mikufeather.carpetmkf.mixin;

import dev.mikufeather.carpetmkf.MkfSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateBaseMixin {
    @Shadow public abstract Block getBlock();

    @Inject(method = "getDestroySpeed", at = @At("HEAD"), cancellable = true)
    private void mkf$fragileNetherPortal(BlockGetter level, BlockPos pos,
            CallbackInfoReturnable<Float> cir) {
        if (MkfSettings.fragileNetherPortal && getBlock() == Blocks.NETHER_PORTAL) {
            cir.setReturnValue(0.3F);
        }
    }
}
