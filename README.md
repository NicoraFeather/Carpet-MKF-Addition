# Carpet MKF Addition

MikuFeather 的 Carpet 扩展，优先用于 Minecraft Java 版单人游戏。

## 环境

| 组件 | 版本 |
| --- | --- |
| Minecraft | 1.21.1 |
| Fabric Loader | 0.18.4 |
| Fabric API（开发运行环境，当前功能不依赖 API） | 0.116.9+1.21.1 |
| Carpet | 1.4.147+v240613（fabric-carpet-1.21-1.4.147+v240613） |
| Gradle / Loom | 9.2.1 / 1.14.10 |

编译使用 JDK 21 或更高版本；输出 Java 21 字节码。Gradle 在 JDK 25 上运行需要当前配置的 Gradle 9.2.1。

## MKF 分类入口

执行 `/carpet` 后，输出末尾出现可点击的 `[MKF]`，点击执行 `/mkf list MKF`，列出本扩展规则。也可直接执行 `/mkf` 浏览本扩展的分类。规则仍由独立设置管理器管理，配置保存在存档的 `mkf.conf`。

## MKF 的仙人掌扳手

默认关闭，进入启用作弊的单人世界执行：

```mcfunction
/mkf mkfFlippinCactus true
```

在当前存档中持久启用：

```mcfunction
/mkf setDefault mkfFlippinCactus true
```

- 主手持 `minecraft:cactus` 右键：无更新旋转方块。
- 观察者、发射器、投掷器、未伸出的普通／黏性活塞按固定顺序循环：**下→上→北→南→西→东→下**。每次右键前进一步，六次回到初始朝向；仅改变 facing 属性。
- 铁轨改为下述实验性 shape 循环；其余方块沿用 Carpet 仙人掌扳手规则，伸出的活塞不旋转。
- 副手持仙人掌：复用 Carpet 的反向放置判定。
- 不需要开启 `/carpet flippinCactus true`，也不会改写这个全局规则。两者同时开启时，MKF 主手逻辑优先；关闭 MKF 后恢复由 Carpet 自身规则控制。
- 不改变发射器内仙人掌的旋转功能。
- 0.1.2 起移除旧规则 `flippinBamboo`；请在存档 `mkf.conf` 中移除旧项，并用新的 setDefault 命令重新保存。竹子不再触发本扩展的扳手功能。
- 冒险模式等无建造权限时不能使用主手扳手。
- 除上述六方向修改外，支持的方块及点击规则由指定 Carpet 版本决定；不支持的方块继续执行普通交互，仙人掌可能照常种植。

本功能来自 Carpet 本体而非 TIS 独立实现。扩展通过小范围 Mixin 接入 `BlockRotator`，不复制上游代码，不替换 Minecraft 交互方法，避免与 Carpet 的交互 Redirect 冲突。该辅助类不是稳定 API，因此当前明确绑定 Carpet 1.4.147+v240613；升级须重新验证。

## 铁轨形状循环（0.1.3-test.1 测试功能）

开启 `mkfFlippinCactus` 后，主手持仙人掌右键铁轨，每次切换到下一个 shape。

普通铁轨：

```text
north_south → east_west → ascending_east → ascending_west
→ ascending_north → ascending_south → south_east → south_west
→ north_west → north_east → north_south
```

动力、探测和激活铁轨只循环前六种，`ascending_south` 后回到 `north_south`。以方块实际允许的 shape 值判断是否支持转弯。

仅修改 `shape`，保留 `powered`、`waterlogged` 等其余属性。沿用扳手的更新抑制和客户端同步方式，不主动调整相邻轨道的连接、不补齐坡道支撑。后续外部更新仍可能使铁轨重算形状或因缺少支撑掉落。

## 易碎下界传送门

默认关闭：

```mcfunction
/mkf fragileNetherPortal true
/mkf setDefault fragileNetherPortal true
```

开启后，`minecraft:nether_portal` 的硬度由 -1 改为 0.3，空手与任意工具都可挖掘。所有工具使用空手的基础挖掘速度，效率附魔不能加速；急迫、挖掘疲劳、水下和腾空等原版修正保留。关闭后立即恢复不可挖掘。

保留原版无掉落行为（包括精准采集）。只改挖掘硬度，不改爆炸抗性、黑曜石框架和末地传送门。正常破坏会触发原版邻居更新；若未开启下述坚韧规则2档，可能使整面传送门熄灭。本功能不是无更新破坏。创造模式仍遵循原版即时破坏和持剑限制。

实现参考 Org 易碎黑曜石的动态硬度调整思路，独立实现，没有复制其源码。

## 坚韧的下界传送门

整数规则 `toughNetherPortal`，严格限定为 `0`、`1`、`2`，默认 `0`。

| 值 | 效果 |
| --- | --- |
| 0 | 关闭，原版爆炸抗性和完整性检查 |
| 1 | 下界传送门方块的爆炸抗性改为1200 |
| 2 | 抗性1200，并保留因门方块或黑曜石框架缺失而不完整的传送门，防止连锁熄灭 |

```mcfunction
/mkf toughNetherPortal 2
/mkf setDefault toughNetherPortal 2
```

2档只让传送门保留自身状态，周围红石、观察者等方块仍正常收到更新。可直接破坏选中的门方块，其余门方块保留；框架仍可正常拆除。规则不拦截命令替换、水等直接替换门方块的行为，不改变新传送门的点火条件和传送算法。

从2档降到1或0后，不扫描清除已有残缺门；它们在下次相关形状更新时恢复原版完整性检查。1200是抗性值，不是对所有爆炸或破坏方式绝对免疫。

与 `fragileNetherPortal` 独立，可同时开启：硬度0.3便于手挖，爆炸抗性1200，并能逐块拆门而不连锁熄灭。

## 可逆熔炉

默认关闭，开启或在当前存档持久开启：

```mcfunction
/mkf reversibleFurnace true
/mkf setDefault reversibleFurnace true
```

普通熔炉新增三条独立配方：金锭→粗金、铁锭→粗铁、铜锭→粗铜，均为1:1、每件200游戏刻（20 TPS时10秒）、正常消耗燃料，反向配方经验为0。高炉、烟熏炉不支持这些反向配方，原版粗矿烧锭配方不变。

规则在配方匹配时检查，包括熔炉缓存的配方。关闭后不能继续完成反向烧制；已产出的粗矿保留，输入锭不丢失。熔炉未完成进度按原版处理；已经点燃的燃料仍会继续计时消耗，规则关闭不返还燃料。输出槽满或物品不兼容时按原版停止烧制，支持正常漏斗输入输出及存档保存。

反向不产经验，但将粗矿再次烧成锭仍获得原版经验，因此允许消耗燃料循环获取经验。三条配方由本模组的 `reverse_smelting` 序列化器加载，配方ID为 `carpet-mkf-addition:raw_gold_from_ingot`、`raw_iron_from_ingot`、`raw_copper_from_ingot`，不覆盖原版配方。

## 构建

Windows：`gradlew.bat build`

Git Bash / Linux：`./gradlew build`

构建会运行 `verifyReverseSmelting`：用实际 Minecraft 1.21.1 配方实现读取三条JSON配方，检查开关、缓存ID重查、产物、时间、经验及配方类型。它是无世界的Java验证程序，不替代客户端验收；也可执行 `./gradlew verifyReverseSmelting` 单独运行。

构建产物：`build/libs/carpet-mkf-addition-0.4.0-test.1.jar`。

将构建产物放进目标实例的 `mods` 目录，保留对应 Carpet。不要把 `-sources.jar` 当成模组安装。

## 验收

当前的编译和运行验证状态见 `docs/verification.md`。实际游戏验证前请先备份存档。

## 参考源码

- https://github.com/gnembon/fabric-carpet （tag 1.4.147，e8ea8494b1a16715eaef623261e7b5c9158230a6）
- https://github.com/TISUnion/Carpet-TIS-Addition （研究时 HEAD：14f1aaae300c6ebadd5562b22ea68da2f3227e6f）

TIS 的 `versions/1.21.1/gradle.properties` 也将 `1.21-1.4.147+v240613` 用作该游戏版本的 Carpet 依赖。

尚未选择本项目的分发许可证；当前不代表授予第三方使用、修改或分发授权。Gradle wrapper 来自 Carpet 仓库，其上游许可见 `NOTICE`。
