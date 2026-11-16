package dev.mikufeather.carpetmkf.mixin;

import carpet.CarpetSettings;
import carpet.helpers.BlockRotator;
import dev.mikufeather.carpetmkf.MkfSettings;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Narrow compatibility bridge to the installed Carpet 1.4.147 implementation. */
@Mixin(value = BlockRotator.class, remap = false)
public abstract class BlockRotatorMixin {
    @Inject(method = "flipBlockWithCactus", at = @At("HEAD"), cancellable = true, remap = false)
    private static void mkf$rotateWithBamboo(BlockState state, Level world, Player player,
            InteractionHand hand, BlockHitResult hit, CallbackInfoReturnable<Boolean> cir) {
        if (!MkfSettings.flippinBamboo || !player.getAbilities().mayBuild
                || !player.getMainHandItem().is(Items.BAMBOO)) return;
        // Restore the exact prior value even if another extension has already set it.
        boolean previous = CarpetSettings.impendingFillSkipUpdates.get();
        try {
            CarpetSettings.impendingFillSkipUpdates.set(true);
            cir.setReturnValue(BlockRotator.flipBlock(state, world, player, hand, hit));
        } finally {
            CarpetSettings.impendingFillSkipUpdates.set(previous);
        }
    }

    @Inject(method = "flippinEligibility", at = @At("RETURN"), cancellable = true, remap = false)
    private static void mkf$reversePlacementWithBamboo(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (MkfSettings.flippinBamboo && entity instanceof Player player
                && player.getOffhandItem().is(Items.BAMBOO)) cir.setReturnValue(true);
    }
}
