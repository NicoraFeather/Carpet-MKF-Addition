package dev.mikufeather.carpetmkf;

import dev.mikufeather.carpetmkf.recipe.ReverseSmeltingRecipe;
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

    @Override public void onInitialize() {
        ReverseSmeltingRecipe.register();
        CarpetServer.manageExtension(this);
    }
    @Override public void onGameStarted() { settings.parseSettingsClass(MkfSettings.class); }
    @Override public SettingsManager extensionSettingsManager() { return settings; }
    @Override public String version() { return ID; }
    @Override public Map<String, String> canHasTranslations(String lang) {
        if ("zh_cn".equals(lang)) return Map.ofEntries(
                Map.entry("mkf.rule.mkfFlippinCactus.name", "MKF 的仙人掌扳手"),
                Map.entry("mkf.rule.mkfFlippinCactus.desc", "主手持仙人掌无更新旋转方块；观察者、发射器、投掷器和未伸出的活塞循环六个朝向。"),
                Map.entry("mkf.rule.mkfFlippinCactus.extra.0", "朝向顺序：下→上→北→南→西→东→下。副手仙人掌反向放置；不要求开启 flippinCactus。"),
                Map.entry("mkf.rule.mkfFlippinCactus.extra.1", "测试功能：普通铁轨循环10种形状，动力、探测和激活铁轨循环6种形状，仅修改shape且不触发更新。"),
                Map.entry("mkf.rule.fragileNetherPortal.name", "易碎下界传送门"),
                Map.entry("mkf.rule.fragileNetherPortal.desc", "将下界传送门方块硬度改为0.3，空手和任意工具均可挖掘且工具不加速，无掉落。"),
                Map.entry("mkf.rule.fragileNetherPortal.extra.0", "效率附魔不加速；急迫、挖掘疲劳及水下、腾空修正保留。除非坚韧的下界传送门设为2，破坏仍触发原版连锁熄灭。"),
                Map.entry("mkf.rule.toughNetherPortal.name", "坚韧的下界传送门"),
                Map.entry("mkf.rule.toughNetherPortal.desc", "0：关闭；1：爆炸抗性1200；2：抗性1200且门方块或框架缺失不引起连锁熄灭。"),
                Map.entry("mkf.rule.toughNetherPortal.extra.0", "2档保留周围方块更新，仍可直接破坏门方块。降档后残缺门在下次相关更新时恢复原版检查。"),
                Map.entry("mkf.rule.reversibleFurnace.name", "可逆熔炉"),
                Map.entry("mkf.rule.reversibleFurnace.desc", "普通熔炉可将金锭、铁锭、铜锭以1:1烧制为对应粗矿，每件200游戏刻，正常消耗燃料。"),
                Map.entry("mkf.rule.reversibleFurnace.extra.0", "反向配方不产经验；不支持高炉或烟熏炉。关闭后停止反向烧制，原版正向配方保留。"));
        return Map.ofEntries(
                Map.entry("mkf.rule.mkfFlippinCactus.name", "MKF's cactus wrench"),
                Map.entry("mkf.rule.mkfFlippinCactus.desc", "Rotate blocks without updates; observers, dispensers, droppers and retracted pistons cycle six directions."),
                Map.entry("mkf.rule.mkfFlippinCactus.extra.0", "Order: down, up, north, south, west, east. Offhand cactus reverses placement; independent of flippinCactus."),
                Map.entry("mkf.rule.mkfFlippinCactus.extra.1", "Experimental: cycle 10 rail shapes or 6 powered, detector and activator rail shapes without updates."),
                Map.entry("mkf.rule.fragileNetherPortal.name", "Fragile nether portal"),
                Map.entry("mkf.rule.fragileNetherPortal.desc", "Nether portal hardness is 0.3; mine by hand or any tool at the same base speed, with no drops."),
                Map.entry("mkf.rule.fragileNetherPortal.extra.0", "Efficiency gives no bonus. Vanilla player effects and environmental penalties remain. Portal collapse is prevented only with toughNetherPortal 2."),
                Map.entry("mkf.rule.toughNetherPortal.name", "Tough nether portal"),
                Map.entry("mkf.rule.toughNetherPortal.desc", "0: disabled; 1: explosion resistance 1200; 2: also prevent collapse from missing portal or frame blocks."),
                Map.entry("mkf.rule.toughNetherPortal.extra.0", "Mode 2 preserves surrounding updates and allows direct removal. Lower modes restore integrity checks on subsequent updates."),
                Map.entry("mkf.rule.reversibleFurnace.name", "Reversible furnace"),
                Map.entry("mkf.rule.reversibleFurnace.desc", "Smelt gold, iron and copper ingots into raw metals, 1:1, in a regular furnace over 200 ticks using fuel."),
                Map.entry("mkf.rule.reversibleFurnace.extra.0", "Reverse recipes give no XP and exclude blast furnaces and smokers. Disabling stops reverse smelting; vanilla forward recipes remain."));
    }
}
