package net.onewaycraft.owwr.core.notify;

/**
 * @brief Notifica jogadores em um mundo sobre estágio do reset.
 */
public interface NotificationService {
    void broadcast(String worldName, String message, boolean bossbar, boolean actionbar, boolean title);
    void clear(String worldName);
}
