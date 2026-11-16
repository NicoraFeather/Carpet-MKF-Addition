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
                "mkf.rule.mkfFlippinCactus.name", "MKF 的仙人掌扳手",
                "mkf.rule.mkfFlippinCactus.desc", "主手持仙人掌无更新旋转方块；观察者、发射器、投掷器和未伸出的活塞循环六个朝向。",
                "mkf.rule.mkfFlippinCactus.extra.0", "朝向顺序：下→上→北→南→西→东→下。副手仙人掌反向放置；不要求开启 flippinCactus。",
                "mkf.rule.mkfFlippinCactus.extra.1", "测试功能：普通铁轨循环10种形状，动力、探测和激活铁轨循环6种形状，仅修改shape且不触发更新。");
        return Map.of(
                "mkf.rule.mkfFlippinCactus.name", "MKF's cactus wrench",
                "mkf.rule.mkfFlippinCactus.desc", "Rotate blocks without updates; observers, dispensers, droppers and retracted pistons cycle six directions.",
                "mkf.rule.mkfFlippinCactus.extra.0", "Order: down, up, north, south, west, east. Offhand cactus reverses placement; independent of flippinCactus.",
                "mkf.rule.mkfFlippinCactus.extra.1", "Experimental: cycle 10 rail shapes or 6 powered, detector and activator rail shapes without updates.");
    }
}
