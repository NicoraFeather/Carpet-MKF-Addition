# Carpet MKF Addition

## 0.6.2-test.1 图表显示修正

详细和长期报告均明确标注左轴MSPT / ms、右轴推算TPS，使用规整真实数值刻度；长期趋势不再归一化。详细报告悬停显示竖向辅助线和实际数据点，提示中的平均线明确命名为100刻滑动平均MSPT。此版本不改变采样和统计公式，旧HTML不会自动更新，请使用新生成的报告。

## 0.6.1-test.1 统计口径更新（优先于下文旧版描述）

长短记录统一每刻 `TPS = 1000 / 单刻MSPT`，不以20或目标刻率封顶，冻结时仍正常计算；它是处理能力推算值，不是实际运行刻率。平均TPS为逐刻推算值的算术平均，不是1000除以平均MSPT。零耗时无法求倒数，防御性记为0。游戏内原版Carpet的 `/log tps` 显示不改。

HTML统计仅显示平均MSPT、最大MSPT、平均TPS；详细报告横轴只保留绝对／相对世界刻（可多选），长期汇总报告也支持这两种世界刻横轴切换。移除HTML中的累计处理和实际经过毫秒轴及多余统计，底层CSV/JSON仍保留计时及状态元数据供分析。长期区间仍以完整汇总段加权。

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

## MSPT / TPS 记录器

```mcfunction
/mkfprofile start
/mkfprofile mark 启动机器
/mkfprofile mark 停止机器
/mkfprofile stop
```

需要2级命令权限；start必须由玩家执行，会订阅（不是切换）该玩家的log tps，stop不退订。全服同时一份记录，每个已完成服务端刻采样一次，使用主世界gameTime作为绝对世界刻，另存dayTime、服务端刻序号及单调时钟经过时间。mark后必须有空格，message支持空格，最多512字符。

停止后在游戏实例目录 `mkf-reports/mspt/<时间-唯一编号>/` 后台生成：

- `samples.csv`：原始采样，UTF-8 BOM，含单刻耗时、原版100刻平均MSPT、与/log tps同公式的TPS、目标TPS、累计处理耗时、冻结/冲刺状态。
- `marks.csv`：标记表，兼容CSV引号和换行，防止标记开头的公式字符在电子表格中执行。
- `capture.json`：完整原始记录及元信息。
- `chart.html`：单文件离线交互图表，不依赖CDN或网络。

图表支持MSPT/TPS多选、原版平均线、标记开关、多选横轴并列显示（绝对世界刻、相对世界刻、MSPT积分、实际经过毫秒）；滚轮缩放、拖动选择、双击恢复全局，也能输入样本序号区间或点击标记定位。显示全局及区间的平均单刻MSPT、平均Carpet TPS、平均线均值、峰值、累计耗时及实际采样吞吐率。

**MSPT积分是累计CPU侧刻处理耗时，不含等待时间，并非真实经过时间。** 实际经过时间另外记录。世界冻结时gameTime可能重复，显示的TPS是Carpet推算值，不是墙钟计数；平均TPS按每个样本等权计算。单刻计时沿用原版边界，采样本身发生在原版计时之后；记录器仍会产生一定开销。

采样上限200000、标记上限10000，内存缓冲后导出；达到上限或正常退出存档自动保存。崩溃或强制结束进程可能丢失未停止的记录，不提供崩溃恢复。报告生成工作线程不阻塞游戏主线程，日志中也会记录输出路径。

运行 `./gradlew generateProfileSimulation` 可生成固定随机种子的4800刻仿真报告，涵盖负载变化、尖峰、冻结、冲刺及目标刻率切换，不代表游戏实测性能。

## 长期挂机模式（0.6.0-test.1）

```mcfunction
/mkfprofile start long
/mkfprofile mark 空载测试
/mkfprofile mark 开启机器
/mkfprofile stop
```

原来的start或start detail仍是20万刻上限的逐刻详细模式。长期模式没有固定刻数上限，以long计数，逐刻累计原始MSPT/TPS总和、峰值、超预算刻、冻结和冲刺刻；不保存全部逐刻样本。全局平均值和图表降采样无关，始终按原始样本数加权。

每1200个服务端刻，或采样观察到距上次分段经过60秒时，将当前段保存。mark会结束当前段，后续段以该标记作为阶段名（相邻同名阶段合并展示）。输出buckets.csv、buckets.jsonl、marks.jsonl及summary.json；正常stop/退出后生成长期chart.html，提供全局均值、阶段对照、整段区间加权均值和低频趋势。长期模式不支持段内逐刻回溯，完整的多横轴交互仍由详细模式提供。

统计快照使用临时文件加原子替换（文件系统不支持时退回替换）。写盘采用单后台线程、有界队列；队列满或I/O失败会停止记录并提示，避免阻塞游戏线程及无声丢数据。崩溃后可检查最近summary.json和已写入汇总，但各文件不保证跨文件事务一致；不自动续接旧会话。游戏主线程完全停顿时无法按墙钟定时采样或保存。

录制阶段内存基本固定（只保留累计值、当前桶和最多8项写盘任务），停止生成HTML时会在后台读取所有汇总段，超长记录的最终报告仍会随汇总段数增长。没有固定上限不代表无限磁盘空间或零开销。

`./gradlew verifyLongProfile` 运行120万刻（50游戏日）仿真并检查全局总和与汇总段计数，不启动Minecraft。

## 构建

Windows：`gradlew.bat build`

Git Bash / Linux：`./gradlew build`

构建会运行 `verifyReverseSmelting`：用实际 Minecraft 1.21.1 配方实现读取三条JSON配方，检查开关、缓存ID重查、产物、时间、经验及配方类型。它是无世界的Java验证程序，不替代客户端验收；也可执行 `./gradlew verifyReverseSmelting` 单独运行。

构建产物：`build/libs/carpet-mkf-addition-0.6.2-test.1.jar`。

将构建产物放进目标实例的 `mods` 目录，保留对应 Carpet。不要把 `-sources.jar` 当成模组安装。

## 验收

当前的编译和运行验证状态见 `docs/verification.md`。实际游戏验证前请先备份存档。

## 参考源码

- https://github.com/gnembon/fabric-carpet （tag 1.4.147，e8ea8494b1a16715eaef623261e7b5c9158230a6）
- https://github.com/TISUnion/Carpet-TIS-Addition （研究时 HEAD：14f1aaae300c6ebadd5562b22ea68da2f3227e6f）

TIS 的 `versions/1.21.1/gradle.properties` 也将 `1.21-1.4.147+v240613` 用作该游戏版本的 Carpet 依赖。

尚未选择本项目的分发许可证；当前不代表授予第三方使用、修改或分发授权。Gradle wrapper 来自 Carpet 仓库，其上游许可见 `NOTICE`。
