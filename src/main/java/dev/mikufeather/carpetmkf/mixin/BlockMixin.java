package dev.mikufeather.carpetmkf.mixin;

import dev.mikufeather.carpetmkf.MkfSettings;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public abstract class BlockMixin {
    @Inject(method = "getExplosionResistance", at = @At("HEAD"), cancellable = true)
    private void mkf$toughPortalResistance(CallbackInfoReturnable<Float> cir) {
        if (MkfSettings.toughNetherPortal >= 1 && (Object) this == Blocks.NETHER_PORTAL) {
            cir.setReturnValue(1200.0F);
        }
    }
}
