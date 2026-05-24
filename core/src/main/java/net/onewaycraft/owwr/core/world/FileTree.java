package net.onewaycraft.owwr.core.world;

import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;

/**
 * @brief Helpers para apagar árvores de arquivos em background safely.
 */
public final class FileTree {

    private FileTree() {}

    public static void deleteRecursively(Path root) throws IOException {
        if (!Files.exists(root)) return;
        try (var stream = Files.walk(root)) {
            stream.sorted(Comparator.reverseOrder())
                .forEach(p -> {
                    try { Files.delete(p); }
                    catch (IOException e) { throw new RuntimeException(e); }
                });
        }
    }
}
