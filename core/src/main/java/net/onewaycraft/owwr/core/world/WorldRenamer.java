package net.onewaycraft.owwr.core.world;

import java.io.IOException;
import java.nio.file.*;

/**
 * @brief Swap em três passos: active → backup, staging → active, opcional backup → trash.
 *
 * Em caso de falha no segundo passo, restaura backup → active.
 */
public final class WorldRenamer {

    public void swap(Path active, Path staging) throws IOException {
        if (!Files.isDirectory(staging)) {
            throw new IOException("staging missing: " + staging);
        }
        Path backup = active.resolveSibling(active.getFileName() + "__backup");
        if (Files.exists(backup)) {
            throw new IOException("backup already exists; manual cleanup required: " + backup);
        }
        try {
            Files.move(active, backup, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException fallback) {
            Files.move(active, backup);
        }
        try {
            try {
                Files.move(staging, active, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException fallback) {
                Files.move(staging, active);
            }
        } catch (IOException primary) {
            try {
                Files.move(backup, active, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException rollback) {
                try { Files.move(backup, active); }
                catch (IOException rollback2) { primary.addSuppressed(rollback2); }
            }
            throw primary;
        }
        FileTree.deleteRecursively(backup);
    }
}
