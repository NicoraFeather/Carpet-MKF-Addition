package dev.mikufeather.carpetmkf.profile;

import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Deterministic offline demonstration; no Minecraft instance is started. */
public final class ProfileSimulation {
    public static void main(String[] args) throws Exception {
        Random random = new Random(20261116);
        List<MsptProfiler.Sample> samples = new ArrayList<>();
        List<MsptProfiler.Mark> marks = new ArrayList<>();
        ArrayDeque<Double> window = new ArrayDeque<>();
        double sum = 0, cumulative = 0, elapsed = 0;
        long start = 1_248_000, world = start;
        for (int i = 0; i < 4800; i++) {
            boolean frozen = i >= 3400 && i < 3500;
            boolean sprint = i >= 4000 && i < 4300;
            double target = i >= 4300 ? 10 : 20;
            double base = i < 900 ? 6 : i < 1800 ? 28 : i < 2800 ? 66 : 12;
            double ms = frozen ? 0.7 + random.nextDouble() : Math.max(0.2, base + random.nextGaussian()*base*.12);
            if (i % 347 == 0) ms += 120 + random.nextDouble()*180;
            window.addLast(ms); sum += ms;
            if (window.size() > 100) sum -= window.removeFirst();
            double average = sum / window.size();
            double tps = 1000 / ms;
            cumulative += ms; elapsed += sprint ? ms : Math.max(ms, 1000 / target);
            if (!frozen) world++;
            samples.add(new MsptProfiler.Sample(i, 10_000+i, world, world % 24_000,
                    elapsed, ms, average, tps, target, cumulative, frozen, sprint));
            String label = switch (i) {
                case 0 -> "基线 · 空载";
                case 900 -> "启动机器 · 中等负载";
                case 1800 -> "全负荷 · 超过50ms预算";
                case 2800 -> "停机 · 负载恢复";
                case 3400 -> "冻结世界 · 世界刻不再推进";
                case 3500 -> "恢复模拟";
                case 4000 -> "冲刺运行 · 不等待下一刻";
                case 4300 -> "目标刻率切换为10 TPS";
                case 4700 -> "转义测试 </script><img src=x onerror=alert(1)> \"逗号,换行\n第二行";
                default -> null;
            };
            if (label != null) marks.add(new MsptProfiler.Mark(i, world, label));
        }
        var capture = new MsptProfiler.Capture("仿真：机器负载 / 尖峰 / 冻结 / 冲刺 / 刻率切换",
                start, List.copyOf(samples), List.copyOf(marks), "simulation_complete", true);
        Path report = ProfileReport.write(Path.of(args.length == 0 ? "build/simulation" : args[0]), capture);
        String html = java.nio.file.Files.readString(report);
        if (html.contains("</script><img") || html.contains("__MKF_DATA__")) throw new AssertionError("Unsafe or unfilled HTML");
        if (samples.size() != 4800 || marks.size() != 9) throw new AssertionError("Wrong simulation size");
        System.out.println("Simulation report: " + report.toAbsolutePath());
    }
}
