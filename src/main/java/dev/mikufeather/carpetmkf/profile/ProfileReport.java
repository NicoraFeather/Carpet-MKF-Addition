package dev.mikufeather.carpetmkf.profile;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class ProfileReport {
    private static final Gson JSON = new GsonBuilder().create();
    private ProfileReport() {}

    public static Path write(Path base, MsptProfiler.Capture capture) throws IOException {
        Files.createDirectories(base);
        String name = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        Path directory = Files.createTempDirectory(base, name + "-");
        StringBuilder csv = new StringBuilder("\uFEFFsample_index,server_tick,world_tick,relative_tick,day_time,elapsed_ms,tick_mspt,carpet_average_mspt,carpet_tps,target_tps,cumulative_processing_ms,frozen,sprinting\r\n");
        for (var s : capture.samples()) {
            csv.append(s.index()).append(',').append(s.serverTick()).append(',').append(s.worldTick()).append(',')
                    .append(s.worldTick() - capture.startWorldTick()).append(',').append(s.dayTime()).append(',')
                    .append(s.elapsedMs()).append(',').append(s.mspt()).append(',').append(s.averageMspt()).append(',')
                    .append(s.tps()).append(',').append(s.targetTps()).append(',').append(s.cumulativeMs()).append(',')
                    .append(s.frozen()).append(',').append(s.sprinting()).append("\r\n");
        }
        Files.writeString(directory.resolve("samples.csv"), csv, StandardCharsets.UTF_8);
        StringBuilder marks = new StringBuilder("\uFEFFsample_index,world_tick,message\r\n");
        for (var mark : capture.marks()) {
            // Neutralize spreadsheet formulas while preserving the exact message in JSON/HTML.
            String message = mark.message();
            if (!message.isEmpty() && "=+-@\t\r\n".indexOf(message.charAt(0)) >= 0) message = "'" + message;
            marks.append(mark.sampleIndex()).append(',').append(mark.worldTick()).append(',')
                    .append('"').append(message.replace("\"", "\"\"")).append("\"\r\n");
        }
        Files.writeString(directory.resolve("marks.csv"), marks, StandardCharsets.UTF_8);
        String data = JSON.toJson(capture);
        Files.writeString(directory.resolve("capture.json"), data, StandardCharsets.UTF_8);
        String template;
        try (var stream = ProfileReport.class.getResourceAsStream("/assets/carpet-mkf-addition/profile.html")) {
            if (stream == null) throw new IOException("Missing profile template");
            template = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
        // Gson's default HTML escaping protects embedded JSON from script termination.
        Files.writeString(directory.resolve("chart.html"), template.replace("__MKF_DATA__", data), StandardCharsets.UTF_8);
        return directory.resolve("chart.html");
    }
}
