package net.onewaycraft.owwr.integrations.discord;

import com.google.gson.JsonObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.logging.Logger;

/**
 * @brief Discord webhook poster (async, fire-and-forget).
 *
 * Usage: DiscordWebhook.post(url, "Reset complete: world_resource", logger);
 */
public final class DiscordWebhook {

    private static final HttpClient CLIENT = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .build();

    private DiscordWebhook() {}

    public static void post(String webhookUrl, String content, Logger logger) {
        if (webhookUrl == null || webhookUrl.isBlank()) return;
        JsonObject body = new JsonObject();
        body.addProperty("content", content);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(webhookUrl))
            .header("Content-Type", "application/json")
            .timeout(Duration.ofSeconds(10))
            .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
            .build();

        CLIENT.sendAsync(request, HttpResponse.BodyHandlers.discarding())
            .exceptionally(e -> {
                logger.warning("Discord webhook failed: " + e.getMessage());
                return null;
            });
    }
}
