package dev.mikufeather.carpetmkf;

import carpet.api.settings.Rule;
import carpet.api.settings.RuleCategory;

public final class MkfSettings {
    private MkfSettings() {}
    @Rule(categories = {RuleCategory.CREATIVE, RuleCategory.SURVIVAL, "MKF"})
    public static boolean mkfFlippinCactus = false;
}
