package net.onewaycraft.owwr.core.persistence;

import net.onewaycraft.owwr.api.ResetState;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * @brief Persiste o ResetState em JSON atômico para crash recovery.
 *        Um arquivo por mundo: state/&lt;worldId&gt;.json
 */
public final class StateRepository {

    private final Path baseDir;

    public StateRepository(Path baseDir) {
        this.baseDir = baseDir;
    }

    public void save(ResetState state) {
        try { AtomicJson.write(stateFile(state.worldId()), state); }
        catch (IOException e) { throw new IllegalStateException("save failed", e); }
    }

    public Optional<ResetState> load(String worldId) {
        try { return Optional.ofNullable(AtomicJson.read(stateFile(worldId), ResetState.class)); }
        catch (IOException e) { throw new IllegalStateException("load failed", e); }
    }

    public void clear(String worldId) {
        try { Files.deleteIfExists(stateFile(worldId)); }
        catch (IOException e) { throw new IllegalStateException("clear failed", e); }
    }

    private Path stateFile(String worldId) {
        return baseDir.resolve("state").resolve(worldId + ".json");
    }
}
