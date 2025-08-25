package br.com.mz.configs;

import br.com.mz.Main;
import br.com.mz.ShopItem;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Singleton
public class ShopConfig {

    private final Main _plugin;
    private FileConfiguration _config;
    private Map<String, ShopItem> _shopItems;
    private Map<Integer, ShopItem> _itemsBySlot;

    @Inject
    public ShopConfig(Main plugin) {
        _plugin = plugin;
        loadConfig();
    }

    private void loadConfig() {
        File configFile = new File(_plugin.getDataFolder(), "shop.yml");

        if (!configFile.exists()) {
            _plugin.saveResource("shop.yml", false);
        }

        _config = YamlConfiguration.loadConfiguration(configFile);
        loadShopItems();
    }

    private void loadShopItems() {

        ConfigurationSection itemsSection = _config.getConfigurationSection("shop-items");

        if (itemsSection == null) {
            _shopItems = Collections.emptyMap();
            _itemsBySlot = Collections.emptyMap();
            return;
        }

        _shopItems = itemsSection.getKeys(false).stream()
                .collect(Collectors.toMap(
                        key -> key,
                        key -> new ShopItem(key, Objects.requireNonNull(itemsSection.getConfigurationSection(key)))
                ));

        _itemsBySlot = _shopItems.values().stream().collect(Collectors.toMap(ShopItem::getSlot, item -> item));

        _plugin.getLogger().info(_shopItems.size() + " items loaded from shop.yml");
    }

    public Map<String, ShopItem> getShopItems() {
        return _shopItems;
    }

    public ShopItem getItemBySlot(int slot) {
        return _itemsBySlot.get(slot);
    }
}
