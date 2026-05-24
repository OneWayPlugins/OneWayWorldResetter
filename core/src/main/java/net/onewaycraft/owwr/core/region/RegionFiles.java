package net.onewaycraft.owwr.core.region;

import java.util.regex.Pattern;

/**
 * @brief Validação e construção de nomes de arquivos de região (r.x.z.mca).
 */
public final class RegionFiles {

    private static final Pattern REGION = Pattern.compile("^r\\.-?\\d+\\.-?\\d+\\.mca$");

    private RegionFiles() {}

    public static boolean isRegionFile(String name) {
        return REGION.matcher(name).matches();
    }

    public static String filenameFor(int x, int z) {
        return "r." + x + "." + z + ".mca";
    }
}
