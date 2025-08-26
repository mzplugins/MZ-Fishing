package br.com.mz;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import java.io.File;
import java.util.Collections;
import java.util.Map;

@Singleton
public class MessagesManager {

    private final Main _plugin;
    private FileConfiguration _config;

    @Inject
    public MessagesManager(Main plugin) {
        _plugin = plugin;
        loadMessages();
    }

    private void loadMessages() {
        File configFile = new File(_plugin.getDataFolder(), "messages.yml");

        if (!configFile.exists()) {
            _plugin.saveResource("messages.yml", false);
        }

        _config = YamlConfiguration.loadConfiguration(configFile);
    }

    public String getMessage(String key, Map<String, String> placeholders) {
        String message = _config.getString(key, "");

        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace(entry.getKey(), entry.getValue());
        }

        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public String getMessage(String key) {
        return getMessage(key, Collections.emptyMap());
    }

    public void sendMessage(Player player, String key, Map<String, String> placeholders) {
        String message = getMessage(key, placeholders);
        player.sendMessage(message);
    }

    public void sendMessage(Player player, String key) {
        sendMessage(player, key, Collections.emptyMap());
    }
}
