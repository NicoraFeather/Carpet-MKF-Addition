package dev.mikufeather.carpetmkf.mixin;

import dev.mikufeather.carpetmkf.MkfSettings;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Player.class)
public abstract class PlayerMixin {
    // Replace only the initial tool speed. Vanilla applies player/environment
    // modifiers afterwards; efficiency is skipped because this base speed is 1.
    @ModifyVariable(method = "getDestroySpeed", at = @At(value = "STORE", ordinal = 0), ordinal = 0)
    private float mkf$portalToolSpeed(float speed, BlockState state) {
        return MkfSettings.fragileNetherPortal && state.is(Blocks.NETHER_PORTAL) ? 1.0F : speed;
    }
}
