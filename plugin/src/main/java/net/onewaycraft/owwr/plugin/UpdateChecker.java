package net.onewaycraft.owwr.plugin;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.logging.Logger;

/**
 * @brief Verifica releases mais recentes no GitHub e loga aviso se desatualizado.
 *        Fire-and-forget assíncrono; nunca bloqueia onEnable.
 */
public final class UpdateChecker {

    private static final HttpClient CLIENT = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5)).build();

    private UpdateChecker() {}

    public static void checkLatest(String repo, String currentVersion, Logger logger) {
        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create("https://api.github.com/repos/" + repo + "/releases/latest"))
            .header("Accept", "application/vnd.github+json")
            .timeout(Duration.ofSeconds(10))
            .GET().build();

        CLIENT.sendAsync(req, HttpResponse.BodyHandlers.ofString())
            .thenAccept(resp -> {
                if (resp.statusCode() != 200) return;
                try {
                    JsonObject body = JsonParser.parseString(resp.body()).getAsJsonObject();
                    String latest = body.get("tag_name").getAsString();
                    if (!latest.equalsIgnoreCase(currentVersion)
                        && !latest.equalsIgnoreCase("v" + currentVersion)) {
                        logger.info("Update available: " + latest + " (running " + currentVersion + ")");
                    }
                } catch (Exception e) {
                    logger.fine("Update check parse failed: " + e.getMessage());
                }
            })
            .exceptionally(e -> {
                logger.fine("Update check failed: " + e.getMessage());
                return null;
            });
    }
}
