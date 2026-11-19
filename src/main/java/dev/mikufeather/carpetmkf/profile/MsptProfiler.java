package dev.mikufeather.carpetmkf.profile;

import carpet.logging.LoggerRegistry;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/** One server-thread-owned session; export uses an immutable snapshot off-thread. */
public final class MsptProfiler {
    private static final Logger LOG = LoggerFactory.getLogger("MKF Profiler");
    private static final int MAX_SAMPLES = 200_000;
    private static Session active;

    private MsptProfiler() {}

    public record Sample(int index, long serverTick, long worldTick, long dayTime,
                         double elapsedMs, double mspt, double averageMspt, double tps,
                         double targetTps, double cumulativeMs, boolean frozen, boolean sprinting) {}
    public record Mark(int sampleIndex, long worldTick, String message) {}
    public record Capture(String name, long startWorldTick, List<Sample> samples, List<Mark> marks,
                          String reason, boolean simulated) {}

    private static final class Session {
        final MinecraftServer server;
        final CommandSourceStack owner;
        final long startNanos = System.nanoTime();
        final long startWorldTick;
        final List<Sample> samples = new ArrayList<>();
        final List<Mark> marks = new ArrayList<>();
        double cumulative;
        final LongTermProfile longTerm;

        Session(CommandSourceStack owner, boolean longMode) {
            this.owner = owner;
            server = owner.getServer();
            startWorldTick = server.overworld().getGameTime();
            longTerm = longMode ? new LongTermProfile(FabricLoader.getInstance().getGameDir()
                    .resolve("mkf-reports/mspt"), startWorldTick) : null;
        }
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("mkfprofile")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("start").executes(context -> start(context.getSource(), false))
                        .then(Commands.literal("long").executes(context -> start(context.getSource(), true)))
                        .then(Commands.literal("detail").executes(context -> start(context.getSource(), false))))
                .then(Commands.literal("mark")
                        .then(Commands.argument("message", StringArgumentType.greedyString())
                                .executes(context -> mark(context.getSource(),
                                        StringArgumentType.getString(context, "message")))))
                .then(Commands.literal("stop").executes(context -> stop(context.getSource()))));
    }

    private static int start(CommandSourceStack source, boolean longMode) {
        if (active != null) {
            source.sendFailure(Component.literal("已有记录正在进行，请先执行 /mkfprofile stop。"));
            return 0;
        }
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("请由玩家开始记录，以自动订阅 /log tps。"));
            return 0;
        }
        // Subscribe rather than toggle: an existing subscription must stay enabled.
        LoggerRegistry.subscribePlayer(player.getScoreboardName(), "tps", null);
        active = new Session(source, longMode);
        source.sendSuccess(() -> Component.literal(longMode
                ? "长期记录已开始，无固定刻数上限；每1200刻或60秒落盘，mark划分阶段，已订阅log tps。"
                : "详细记录已开始，最多200000个服务端刻；长时间挂机请用 /mkfprofile start long。"), false);
        return 1;
    }

    private static int mark(CommandSourceStack source, String message) {
        if (active == null || active.server != source.getServer()) {
            source.sendFailure(Component.literal("当前没有记录。"));
            return 0;
        }
        if (message.length() > 512 || (active.longTerm != null
                ? !active.longTerm.canMark() : active.marks.size() >= 10_000)) {
            source.sendFailure(Component.literal("标记最多512字符，每次记录最多10000个标记。"));
            return 0;
        }
        if (active.longTerm != null) active.longTerm.mark(message);
        else active.marks.add(new Mark(active.samples.size(), active.server.overworld().getGameTime(), message));
        source.sendSuccess(() -> Component.literal("已添加标记：" + message), false);
        return 1;
    }

    private static int stop(CommandSourceStack source) {
        if (active == null || active.server != source.getServer()) {
            source.sendFailure(Component.literal("当前没有记录。"));
            return 0;
        }
        finish("manual", source);
        return 1;
    }

    public static void sample(MinecraftServer server) {
        Session session = active;
        if (session == null || session.server != server) return;
        long tick = server.getTickCount();
        double mspt = server.getTickTimesNanos()[(int) (tick % 100)] / 1_000_000.0;
        double average = server.getAverageTickTimeNanos() / 1_000_000.0;
        var rate = server.tickRateManager();
        double target = rate.tickrate();
        double tps = average > 0 ? 1000.0 / average : 0;
        if (!Double.isFinite(tps)) tps = 0;
        if (session.longTerm != null) {
            if (session.longTerm.failure() != null) {
                LOG.error("Long-term profile writer failed", session.longTerm.failure());
                finish("io_failure", session.owner);
                return;
            }
            session.longTerm.add(server.overworld().getGameTime(),
                    (System.nanoTime() - session.startNanos) / 1_000_000.0,
                    average, average, tps, target, rate.isFrozen(), rate.isSprinting());
            return;
        }
        session.cumulative += mspt;
        session.samples.add(new Sample(session.samples.size(), tick, server.overworld().getGameTime(),
                server.overworld().getDayTime(), (System.nanoTime() - session.startNanos) / 1_000_000.0,
                mspt, average, tps, target, session.cumulative, rate.isFrozen(), rate.isSprinting()));
        if (session.samples.size() >= MAX_SAMPLES) finish("sample_limit", session.owner);
    }

    public static void onServerClosed(MinecraftServer server) {
        if (active != null && active.server == server) finish("world_closed", null);
    }

    private static void finish(String reason, CommandSourceStack notify) {
        Session session = active;
        active = null;
        if (session.longTerm != null) {
            if (notify != null) notify.sendSuccess(() -> Component.literal("长期记录结束，共"
                    + session.longTerm.count() + "个样本，正在汇总；log tps保持开启。"), false);
            session.longTerm.finish(reason).whenComplete((path, error) -> {
                if (error != null) LOG.error("Unable to export long-term report", error);
                else LOG.info("Long-term profile saved: {}", path);
                if (notify != null && !session.server.isStopped()) session.server.execute(() -> {
                    if (error != null) notify.sendFailure(Component.literal("长期导出失败，已落盘检查点仍在报告目录，请查看日志。"));
                    else notify.sendSuccess(() -> Component.literal("长期报告已保存：" + path.toAbsolutePath()), false);
                });
            });
            return;
        }
        Capture capture = new Capture("MKF MSPT Profile", session.startWorldTick,
                List.copyOf(session.samples), List.copyOf(session.marks), reason, false);
        Path base = FabricLoader.getInstance().getGameDir().resolve("mkf-reports/mspt");
        if (notify != null) notify.sendSuccess(() -> Component.literal(
                "记录结束，共" + capture.samples().size() + "个样本，正在后台导出。log tps保持开启。"), false);
        // Non-daemon worker finishes a pending report even when the integrated server closes.
        CompletableFuture.runAsync(() -> {
            try {
                Path report = ProfileReport.write(base, capture);
                LOG.info("MKF profile saved: {}", report);
                if (notify != null && !session.server.isStopped()) session.server.execute(() ->
                        notify.sendSuccess(() -> Component.literal("报告已保存：" + report.toAbsolutePath()), false));
            } catch (Exception e) {
                LOG.error("Unable to export MKF profile", e);
                if (notify != null && !session.server.isStopped()) session.server.execute(() ->
                        notify.sendFailure(Component.literal("报告导出失败，请查看日志：" + e.getMessage())));
            }
        }, runnable -> {
            Thread worker = new Thread(runnable, "MKF-profile-export");
            worker.setDaemon(false);
            worker.start();
        });
    }
}
