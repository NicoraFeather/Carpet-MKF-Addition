package dev.mikufeather.carpetmkf.mixin;

import dev.mikufeather.carpetmkf.MkfSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NetherPortalBlock.class)
public abstract class NetherPortalBlockMixin {
    // Preserve this portal state instead of allowing an incomplete frame or
    // missing adjacent portal block to turn it into air. Other updates proceed.
    @Inject(method = "updateShape", at = @At("HEAD"), cancellable = true)
    private void mkf$preserveIncompletePortal(BlockState state, Direction direction,
            BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos,
            CallbackInfoReturnable<BlockState> cir) {
        if (MkfSettings.toughNetherPortal == 2) cir.setReturnValue(state);
    }
}
