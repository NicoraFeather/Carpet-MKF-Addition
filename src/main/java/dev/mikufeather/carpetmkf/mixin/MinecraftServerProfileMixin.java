package dev.mikufeather.carpetmkf.mixin;

import dev.mikufeather.carpetmkf.profile.MsptProfiler;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerProfileMixin {
    @Inject(method = "tickServer", at = @At("TAIL"))
    private void mkf$sampleCompletedTick(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
        MsptProfiler.sample((MinecraftServer) (Object) this);
    }
}
