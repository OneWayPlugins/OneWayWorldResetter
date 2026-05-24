package net.onewaycraft.owwr.core.world;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @brief Validação canonical de paths para evitar apagar fora do container.
 */
public final class PathSafety {

    private PathSafety() {}

    /**
     * @brief Retorna true sse {@code target} for um descendente estrito de {@code container}
     *        após resolução canonical (sem symlinks/traversal).
     */
    public static boolean isUnder(Path target, Path container) {
        try {
            Path c = container.toAbsolutePath().normalize().toRealPath();
            Path t = target.toAbsolutePath().normalize().toRealPath();
            return !t.equals(c) && t.startsWith(c);
        } catch (IOException e) {
            // Path não existe ainda → cair no normalize comparison sem realPath
            Path c = container.toAbsolutePath().normalize();
            Path t = target.toAbsolutePath().normalize();
            return !t.equals(c) && t.startsWith(c);
        }
    }
}
