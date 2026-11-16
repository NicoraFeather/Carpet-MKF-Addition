package dev.mikufeather.carpetmkf;

import carpet.api.settings.Rule;
import carpet.api.settings.RuleCategory;

public final class MkfSettings {
    private MkfSettings() {}
    @Rule(categories = {RuleCategory.CREATIVE, RuleCategory.SURVIVAL, "MKF"})
    public static boolean mkfFlippinCactus = false;

    @Rule(categories = {RuleCategory.SURVIVAL, "MKF"})
    public static boolean fragileNetherPortal = false;

    @Rule(categories = {RuleCategory.SURVIVAL, "MKF"}, options = {"0", "1", "2"}, strict = true)
    public static int toughNetherPortal = 0;
}
