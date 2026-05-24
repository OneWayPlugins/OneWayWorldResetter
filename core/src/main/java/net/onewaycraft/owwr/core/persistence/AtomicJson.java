package net.onewaycraft.owwr.core.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

/**
 * @brief I/O JSON com escrita atômica (temp file + ATOMIC_MOVE).
 */
public final class AtomicJson {

    private static final Gson GSON = new GsonBuilder()
        .setPrettyPrinting()
        .serializeNulls()
        .registerTypeAdapter(java.time.Instant.class,
            (com.google.gson.JsonSerializer<java.time.Instant>) (src, t, c) ->
                new com.google.gson.JsonPrimitive(src.toString()))
        .registerTypeAdapter(java.time.Instant.class,
            (com.google.gson.JsonDeserializer<java.time.Instant>) (json, t, c) ->
                java.time.Instant.parse(json.getAsString()))
        .registerTypeAdapter(java.time.Duration.class,
            (com.google.gson.JsonSerializer<java.time.Duration>) (src, t, c) ->
                new com.google.gson.JsonPrimitive(src.toString()))
        .registerTypeAdapter(java.time.Duration.class,
            (com.google.gson.JsonDeserializer<java.time.Duration>) (json, t, c) ->
                java.time.Duration.parse(json.getAsString()))
        .create();

    private AtomicJson() {}

    public static Gson gson() { return GSON; }

    public static void write(Path target, Object payload) throws IOException {
        Files.createDirectories(target.getParent());
        Path tmp = target.resolveSibling(target.getFileName() + ".tmp");
        Files.writeString(tmp, GSON.toJson(payload), StandardCharsets.UTF_8,
            StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        try {
            Files.move(tmp, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public static <T> T read(Path source, Class<T> type) throws IOException {
        if (!Files.exists(source)) return null;
        String json = Files.readString(source, StandardCharsets.UTF_8);
        return GSON.fromJson(json, type);
    }
}
