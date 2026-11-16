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
                "mkf.rule.mkfFlippinCactus.extra.1", "测试功能：普通铁轨循环10种形状，动力、探测和激活铁轨循环6种形状，仅修改shape且不触发更新。",
                "mkf.rule.fragileNetherPortal.name", "易碎下界传送门",
                "mkf.rule.fragileNetherPortal.desc", "将下界传送门方块硬度改为0.3，空手和任意工具均可挖掘且工具不加速，无掉落。",
                "mkf.rule.fragileNetherPortal.extra.0", "效率附魔不加速；急迫、挖掘疲劳及水下、腾空修正保留。破坏仍触发原版传送门连锁熄灭。");
        return Map.of(
                "mkf.rule.mkfFlippinCactus.name", "MKF's cactus wrench",
                "mkf.rule.mkfFlippinCactus.desc", "Rotate blocks without updates; observers, dispensers, droppers and retracted pistons cycle six directions.",
                "mkf.rule.mkfFlippinCactus.extra.0", "Order: down, up, north, south, west, east. Offhand cactus reverses placement; independent of flippinCactus.",
                "mkf.rule.mkfFlippinCactus.extra.1", "Experimental: cycle 10 rail shapes or 6 powered, detector and activator rail shapes without updates.",
                "mkf.rule.fragileNetherPortal.name", "Fragile nether portal",
                "mkf.rule.fragileNetherPortal.desc", "Nether portal hardness is 0.3; mine by hand or any tool at the same base speed, with no drops.",
                "mkf.rule.fragileNetherPortal.extra.0", "Efficiency gives no bonus. Vanilla player effects, environmental penalties and portal collapse remain.");
    }
}
