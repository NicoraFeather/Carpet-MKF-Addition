package dev.mikufeather.carpetmkf.mixin;

import carpet.CarpetServer;
import carpet.api.settings.SettingsManager;
import carpet.utils.Messenger;
import net.minecraft.commands.CommandSourceStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Adds a cross-manager category link without moving or duplicating rules. */
@Mixin(value = SettingsManager.class, remap = false)
public abstract class SettingsManagerMixin {
    @Inject(method = "listAllSettings", at = @At("TAIL"), remap = false)
    private void mkf$showCategory(CommandSourceStack source, CallbackInfoReturnable<Integer> cir) {
        if ((Object) this == CarpetServer.settingsManager) {
            Messenger.m(source, "c [MKF]", "^g Carpet MKF Addition", "!/mkf list MKF");
        }
    }
}
