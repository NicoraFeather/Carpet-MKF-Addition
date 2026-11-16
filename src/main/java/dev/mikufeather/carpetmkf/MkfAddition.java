package dev.mikufeather.carpetmkf;

import carpet.CarpetExtension;
import carpet.CarpetServer;
import carpet.api.settings.SettingsManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import java.util.Map;

public final class MkfAddition implements ModInitializer, CarpetExtension {
    public static final String ID = "carpet-mkf-addition";
    private final SettingsManager settings = new SettingsManager(
            FabricLoader.getInstance().getModContainer(ID).orElseThrow()
                    .getMetadata().getVersion().getFriendlyString(), "mkf", "Carpet MKF Addition");

    @Override public void onInitialize() { CarpetServer.manageExtension(this); }
    @Override public void onGameStarted() { settings.parseSettingsClass(MkfSettings.class); }
    @Override public SettingsManager extensionSettingsManager() { return settings; }
    @Override public String version() { return ID; }
    @Override public Map<String, String> canHasTranslations(String lang) {
        if ("zh_cn".equals(lang)) return Map.of(
                "mkf.rule.flippinBamboo.name", "竹子扳手",
                "mkf.rule.flippinBamboo.desc", "主手持竹子右键无更新旋转方块；副手持竹子反向放置方块。",
                "mkf.rule.flippinBamboo.extra.0", "复用 Carpet 1.4.147 的仙人掌扳手逻辑，不要求开启 flippinCactus。");
        return Map.of(
                "mkf.rule.flippinBamboo.name", "Bamboo wrench",
                "mkf.rule.flippinBamboo.desc", "Use bamboo to rotate blocks without updates; offhand bamboo reverses placement.",
                "mkf.rule.flippinBamboo.extra.0", "Uses Carpet 1.4.147 rotation logic independently of flippinCactus.");
    }
}
