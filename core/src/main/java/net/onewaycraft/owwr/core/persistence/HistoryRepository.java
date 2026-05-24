package net.onewaycraft.owwr.core.persistence;

import com.google.gson.reflect.TypeToken;
import net.onewaycraft.owwr.api.ResetRecord;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * @brief Append-only de ResetRecord em history.json (mantém últimas N entradas).
 */
public final class HistoryRepository {

    private static final Type LIST_TYPE = new TypeToken<List<ResetRecord>>() {}.getType();

    private final Path file;
    private final int maxEntries;

    public HistoryRepository(Path baseDir, int maxEntries) {
        this.file = baseDir.resolve("history.json");
        this.maxEntries = maxEntries;
    }

    public synchronized void append(ResetRecord record) {
        List<ResetRecord> all = loadAll();
        all.add(record);
        while (all.size() > maxEntries) all.remove(0);
        try { AtomicJson.write(file, all); }
        catch (IOException e) { throw new IllegalStateException("history append failed", e); }
    }

    public List<ResetRecord> recent(int n) {
        List<ResetRecord> all = loadAll();
        return all.subList(Math.max(0, all.size() - n), all.size());
    }

    private List<ResetRecord> loadAll() {
        if (!Files.exists(file)) return new ArrayList<>();
        try {
            String json = Files.readString(file, StandardCharsets.UTF_8);
            List<ResetRecord> list = AtomicJson.gson().fromJson(json, LIST_TYPE);
            return list != null ? list : new ArrayList<>();
        } catch (IOException e) { return new ArrayList<>(); }
    }
}
