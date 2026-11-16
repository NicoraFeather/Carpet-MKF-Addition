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

## 竹子扳手

默认关闭，进入启用作弊的单人世界执行：

```mcfunction
/mkf flippinBamboo true
```

在当前存档中持久启用：

```mcfunction
/mkf setDefault flippinBamboo true
```

- 主手持 `minecraft:bamboo` 右键：复用 Carpet 仙人掌扳手的无更新旋转逻辑。
- 副手持竹子：复用 Carpet 的反向放置判定。
- 不需要开启 `/carpet flippinCactus true`，也不会改写这个全局规则。
- 保留原有仙人掌功能；不改变发射器的仙人掌旋转行为。
- 冒险模式等无建造权限时不能使用主手扳手。
- 支持的方块及点击规则由指定 Carpet 版本决定；不支持的方块继续执行普通交互，竹子可能照常种植。

本功能来自 Carpet 本体而非 TIS 独立实现。扩展通过小范围 Mixin 接入 `BlockRotator`，不复制上游代码，不替换 Minecraft 交互方法，避免与 Carpet 的交互 Redirect 冲突。该辅助类不是稳定 API，因此当前明确绑定 Carpet 1.4.147+v240613；升级须重新验证。

## 构建

Windows：`gradlew.bat build`

Git Bash / Linux：`./gradlew build`

构建产物：`build/libs/carpet-mkf-addition-0.1.0.jar`。

将构建产物放进目标实例的 `mods` 目录，保留对应 Carpet。不要把 `-sources.jar` 当成模组安装。

## 验收

当前的编译和运行验证状态见 `docs/verification.md`。实际游戏验证前请先备份存档。

## 参考源码

- https://github.com/gnembon/fabric-carpet （tag 1.4.147，e8ea8494b1a16715eaef623261e7b5c9158230a6）
- https://github.com/TISUnion/Carpet-TIS-Addition （研究时 HEAD：14f1aaae300c6ebadd5562b22ea68da2f3227e6f）

TIS 的 `versions/1.21.1/gradle.properties` 也将 `1.21-1.4.147+v240613` 用作该游戏版本的 Carpet 依赖。

尚未选择本项目的分发许可证；当前不代表授予第三方使用、修改或分发授权。Gradle wrapper 来自 Carpet 仓库，其上游许可见 `NOTICE`。
