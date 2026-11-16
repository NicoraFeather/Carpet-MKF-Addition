package dev.mikufeather.carpetmkf.mixin;

import carpet.CarpetSettings;
import carpet.helpers.BlockRotator;
import dev.mikufeather.carpetmkf.MkfSettings;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.ObserverBlock;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Narrow compatibility bridge to the installed Carpet 1.4.147 implementation. */
@Mixin(value = BlockRotator.class, remap = false)
public abstract class BlockRotatorMixin {
    @Inject(method = "flipBlockWithCactus", at = @At("HEAD"), cancellable = true, remap = false)
    private static void mkf$rotateWithCactus(BlockState state, Level world, Player player,
            InteractionHand hand, BlockHitResult hit, CallbackInfoReturnable<Boolean> cir) {
        if (!MkfSettings.mkfFlippinCactus || !player.getAbilities().mayBuild
                || !player.getMainHandItem().is(Items.CACTUS)) return;
        // Restore the exact prior value even if another extension has already set it.
        boolean previous = CarpetSettings.impendingFillSkipUpdates.get();
        try {
            CarpetSettings.impendingFillSkipUpdates.set(true);
            Block block = state.getBlock();
            if (block instanceof BaseRailBlock rail) {
                var shape = rail.getShapeProperty();
                RailShape next = switch (state.getValue(shape)) {
                    case NORTH_SOUTH -> RailShape.EAST_WEST;
                    case EAST_WEST -> RailShape.ASCENDING_EAST;
                    case ASCENDING_EAST -> RailShape.ASCENDING_WEST;
                    case ASCENDING_WEST -> RailShape.ASCENDING_NORTH;
                    case ASCENDING_NORTH -> RailShape.ASCENDING_SOUTH;
                    case ASCENDING_SOUTH -> shape.getPossibleValues().contains(RailShape.SOUTH_EAST)
                            ? RailShape.SOUTH_EAST : RailShape.NORTH_SOUTH;
                    case SOUTH_EAST -> RailShape.SOUTH_WEST;
                    case SOUTH_WEST -> RailShape.NORTH_WEST;
                    case NORTH_WEST -> RailShape.NORTH_EAST;
                    case NORTH_EAST -> RailShape.NORTH_SOUTH;
                };
                BlockState rotated = state.setValue(shape, next);
                world.setBlock(hit.getBlockPos(), rotated, Block.UPDATE_CLIENTS | 1024);
                world.setBlocksDirty(hit.getBlockPos(), state, rotated);
                cir.setReturnValue(true);
            } else if (block instanceof ObserverBlock || block instanceof DispenserBlock
                    || (block instanceof PistonBaseBlock && !state.getValue(PistonBaseBlock.EXTENDED))) {
                // Explicit order, independent of Direction enum ordering and player position.
                Direction next = switch (state.getValue(DirectionalBlock.FACING)) {
                    case DOWN -> Direction.UP;
                    case UP -> Direction.NORTH;
                    case NORTH -> Direction.SOUTH;
                    case SOUTH -> Direction.WEST;
                    case WEST -> Direction.EAST;
                    case EAST -> Direction.DOWN;
                };
                BlockState rotated = state.setValue(DirectionalBlock.FACING, next);
                world.setBlock(hit.getBlockPos(), rotated, Block.UPDATE_CLIENTS | 1024);
                world.setBlocksDirty(hit.getBlockPos(), state, rotated);
                cir.setReturnValue(true);
            } else {
                cir.setReturnValue(BlockRotator.flipBlock(state, world, player, hand, hit));
            }
        } finally {
            CarpetSettings.impendingFillSkipUpdates.set(previous);
        }
    }

    @Inject(method = "flippinEligibility", at = @At("RETURN"), cancellable = true, remap = false)
    private static void mkf$reversePlacementWithCactus(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (MkfSettings.mkfFlippinCactus && entity instanceof Player player
                && player.getOffhandItem().is(Items.CACTUS)) cir.setReturnValue(true);
    }
}
