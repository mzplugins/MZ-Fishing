package br.com.mz;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.stream.Collectors;

public class ShopItem {

    private final String key;
    private final int slot;
    private final Material material;
    private final String displayName;
    private final List<String> lore;
    private final double price;
    private final int customModelData;
    private final String command;

    public ShopItem(String key, ConfigurationSection section) {
        this.key = key;
        this.slot = section.getInt("slot");
        this.material = Material.matchMaterial(section.getString("material", "STONE"));
        this.displayName = section.getString("display-name", "&cNome Inválido");
        this.lore = section.getStringList("lore");
        this.price = section.getDouble("price", 0.0);
        this.customModelData = section.getInt("custom-model-data", 0);
        this.command = section.getString("command", "");
    }

    public ItemStack toItemStack() {
        ItemStack item = new ItemStack(this.material, 1);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            Component componentName = LegacyComponentSerializer.legacySection().deserialize(this.displayName.replace('&', '§'));
            meta.displayName(componentName);

            List<Component> componentLore = lore.stream()
                    .map(line -> line.replace('&', '§'))
                    .map(line -> {
                        String formattedValue = String.format("%.0fK", this.price / 1000.0);
                        return line.replace("%price%", formattedValue);
                    })
                    .map(line -> LegacyComponentSerializer.legacySection().deserialize(line))
                    .collect(Collectors.toList());

            meta.lore(componentLore);

            if (this.customModelData > 0) {
                meta.setCustomModelData(this.customModelData);
            }

            item.setItemMeta(meta);
        }

        return item;
    }

    public String getDisplayName() { return displayName; }
    public int getSlot() { return slot; }
    public double getPrice() { return price; }
    public String getKey() { return key; }
    public String getCommand() { return command; }
}
