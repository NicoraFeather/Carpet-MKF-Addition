package dev.mikufeather.carpetmkf.profile;

import com.google.gson.Gson;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

/** Bounded server-side storage: exact accumulators plus one 1200-tick bucket. */
public final class LongTermProfile {
    public static final int BUCKET_SIZE = 1200;
    private static final Gson JSON = new Gson();
    private static final String HEADER = "first_sample,last_sample,world_start,world_end,count,sum_rolling_mspt,sum_rolling_derived_tps,sum_average_mspt,max_rolling_mspt,over_budget,frozen,sprinting,elapsed_start_ms,elapsed_end_ms,phase\r\n";
    public record Totals(long count, double sumMspt, double sumTps, double sumAverage,
                         double maxMspt, long overBudget, long frozen, long sprinting) {}
    public record Bucket(long first, long last, long worldStart, long worldEnd, Totals totals,
                         double elapsedStart, double elapsedEnd, String phase) {}
    public record Snapshot(String mode, long startWorldTick, Totals totals, double elapsedMs,
                           String phase, String reason) {}
    private static final class Accumulator {
        long count, over, frozen, sprinting;
        double sum, sumTps, sumAverage, max;
        void add(double ms, double average, double tps, double target, boolean freeze, boolean sprint) {
            count++; sum += ms; sumTps += tps; sumAverage += average; max = Math.max(max, ms);
            if (ms > 1000 / target) over++;
            if (freeze) frozen++;
            if (sprint) sprinting++;
        }
        Totals snapshot() { return new Totals(count, sum, sumTps, sumAverage, max, over, frozen, sprinting); }
    }
    private final ThreadPoolExecutor writer;
    private volatile Path directory;
    private volatile Throwable failure;
    private final long startWorldTick;
    private final Accumulator global = new Accumulator();
    private Accumulator bucket = new Accumulator();
    private long bucketWorldStart, lastWorld;
    private double bucketElapsedStart, lastElapsed;
    private String phase = "开始记录";
    private int marks;

    public LongTermProfile(Path base, long startWorldTick) {
        this.startWorldTick = startWorldTick;
        this.lastWorld = startWorldTick;
        writer = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS, new ArrayBlockingQueue<>(8), r -> {
            Thread thread = new Thread(r, "MKF-long-profile-writer");
            thread.setDaemon(false);
            return thread;
        }, new ThreadPoolExecutor.AbortPolicy());
        Snapshot initial = snapshot("recording");
        submit(() -> {
            Files.createDirectories(base);
            directory = Files.createTempDirectory(base, LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) + "-long-");
            Files.writeString(directory.resolve("buckets.csv"), "\uFEFF" + HEADER, StandardCharsets.UTF_8);
            Files.writeString(directory.resolve("buckets.jsonl"), "", StandardCharsets.UTF_8);
            saveSnapshot(initial);
        });
    }
    // Package-private deterministic test barrier; never used on the game thread.
    void awaitCheckpoint() throws Exception { writer.submit(() -> {}).get(30, TimeUnit.SECONDS); }
    public long count() { return global.count; }
    public Throwable failure() { return failure; }
    public boolean canMark() { return marks < 10000; }

    public void add(long world, double elapsed, double ms, double average, double tps,
                    double target, boolean frozen, boolean sprinting) {
        if (bucket.count == 0) bucketWorldStart = world;
        global.add(ms, average, tps, target, frozen, sprinting);
        bucket.add(ms, average, tps, target, frozen, sprinting);
        lastWorld = world; lastElapsed = elapsed;
        if (bucket.count >= BUCKET_SIZE || elapsed - bucketElapsedStart >= 60_000) flush("recording");
    }
    public void mark(String message) {
        flush("recording");
        phase = message;
        marks++;
        Snapshot snapshot = snapshot("recording");
        long index = count();
        long world = lastWorld;
        submit(() -> {
            Files.writeString(directory.resolve("marks.jsonl"), JSON.toJson(Map.of(
                    "sample", index, "worldTick", world, "message", message)) + "\n",
                    StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            saveSnapshot(snapshot);
        });
    }
    private Snapshot snapshot(String reason) {
        return new Snapshot("long", startWorldTick, global.snapshot(), lastElapsed, phase, reason);
    }
    private void flush(String reason) {
        Snapshot snapshot = snapshot(reason);
        Bucket frozenBucket = bucket.count == 0 ? null : new Bucket(global.count - bucket.count,
                global.count - 1, bucketWorldStart, lastWorld, bucket.snapshot(), bucketElapsedStart, lastElapsed, phase);
        bucket = new Accumulator();
        bucketElapsedStart = lastElapsed;
        submit(() -> {
            if (frozenBucket != null) {
                Files.writeString(directory.resolve("buckets.jsonl"), JSON.toJson(frozenBucket) + "\n",
                        StandardCharsets.UTF_8, StandardOpenOption.APPEND);
                Files.writeString(directory.resolve("buckets.csv"), csv(frozenBucket),
                        StandardCharsets.UTF_8, StandardOpenOption.APPEND);
            }
            saveSnapshot(snapshot);
        });
    }
    public CompletableFuture<Path> finish(String reason) {
        flush(reason);
        Snapshot snapshot = snapshot(reason);
        CompletableFuture<Path> result = new CompletableFuture<>();
        if (failure != null) {
            writer.shutdown();
            result.completeExceptionally(failure);
            return result;
        }
        submit(() -> {
            try {
                if (failure != null) throw new IOException("Earlier checkpoint failed", failure);
                List<Bucket> buckets = new ArrayList<>();
                try (var lines = Files.lines(directory.resolve("buckets.jsonl"))) {
                    lines.filter(line -> !line.isBlank()).forEach(line -> buckets.add(JSON.fromJson(line, Bucket.class)));
                }
                String data = JSON.toJson(Map.of("snapshot", snapshot, "buckets", buckets));
                String template;
                try (var stream = LongTermProfile.class.getResourceAsStream("/assets/carpet-mkf-addition/long-profile.html")) {
                    if (stream == null) throw new IOException("Missing long profile template");
                    template = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
                }
                Files.writeString(directory.resolve("chart.html"), template.replace("__MKF_DATA__", data), StandardCharsets.UTF_8);
                result.complete(directory.resolve("chart.html"));
            } catch (Throwable e) { result.completeExceptionally(e); }
        });
        writer.shutdown();
        if (failure != null) result.completeExceptionally(failure);
        return result;
    }
    private void saveSnapshot(Snapshot snapshot) throws IOException {
        Path temp = directory.resolve("summary.json.tmp");
        Files.writeString(temp, JSON.toJson(snapshot), StandardCharsets.UTF_8);
        try { Files.move(temp, directory.resolve("summary.json"), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE); }
        catch (AtomicMoveNotSupportedException e) {
            Files.move(temp, directory.resolve("summary.json"), StandardCopyOption.REPLACE_EXISTING);
        }
    }
    private void submit(IOAction action) {
        if (failure != null) return;
        try { writer.execute(() -> { try { action.run(); } catch (Throwable e) { failure = e; } }); }
        catch (RejectedExecutionException e) { failure = e; writer.shutdown(); }
    }
    private static String csv(Bucket b) {
        Totals t = b.totals();
        String text = b.phase();
        if (!text.isEmpty() && "=+-@\t\r\n".indexOf(text.charAt(0)) >= 0) text = "'" + text;
        return b.first()+","+b.last()+","+b.worldStart()+","+b.worldEnd()+","+t.count()+","+t.sumMspt()+","+
                t.sumTps()+","+t.sumAverage()+","+t.maxMspt()+","+t.overBudget()+","+t.frozen()+","+t.sprinting()+","+
                b.elapsedStart()+","+b.elapsedEnd()+",\""+text.replace("\"", "\"\"")+"\"\r\n";
    }
    @FunctionalInterface private interface IOAction { void run() throws Exception; }
}
