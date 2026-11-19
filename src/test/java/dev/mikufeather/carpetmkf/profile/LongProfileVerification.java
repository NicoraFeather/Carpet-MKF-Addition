package dev.mikufeather.carpetmkf.profile;

import com.google.gson.JsonParser;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

public final class LongProfileVerification {
    public static void main(String[] args) throws Exception {
        Path base = Path.of("build/long-simulation");
        var profile = new LongTermProfile(base, 100000);
        profile.awaitCheckpoint();
        double expected = 0;
        for (int i = 0; i < 1_200_000; i++) {
            double ms = i < 400_000 ? 5 : i < 800_000 ? 30 : 70;
            expected += ms;
            profile.add(100001L+i, (i+1)*50.0, ms, ms, Math.min(20, 1000/ms), 20, false, false);
            if (i == 399_999) profile.mark("机器开启");
            if (i == 799_999) profile.mark("满负载 </script>");
            // Simulate real spacing: do not flood the intentionally bounded I/O queue.
            if ((i+1)%1200 == 0) profile.awaitCheckpoint();
            if (profile.failure() != null) throw new AssertionError(profile.failure());
        }
        Path report = profile.finish("simulation_complete").get(30, TimeUnit.SECONDS);
        var json = JsonParser.parseString(Files.readString(report.resolveSibling("summary.json"))).getAsJsonObject();
        var totals = json.getAsJsonObject("totals");
        if (totals.get("count").getAsLong() != 1_200_000) throw new AssertionError("count");
        if (Math.abs(totals.get("sumMspt").getAsDouble() - expected) > 0.001) throw new AssertionError("sum");
        if (Files.readString(report).contains("</script>\"")) throw new AssertionError("escaping");
        long count;
        try(var lines = Files.lines(report.resolveSibling("buckets.jsonl"))) {
            count = lines.map(line -> JsonParser.parseString(line).getAsJsonObject()
                    .getAsJsonObject("totals").get("count").getAsLong()).mapToLong(Long::longValue).sum();
        }
        if (count != 1_200_000) throw new AssertionError("bucket count");
        System.out.println("Long-term verification passed: 1,200,000 ticks / 50 game days. " + report.toAbsolutePath());
    }
}
