package br.com.mz.guis;

import br.com.mz.ShopItem;
import br.com.mz.configs.ShopConfig;
import br.com.mz.database.PlayerProfileRepository;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class ShopMenu {

    public static final String TITLE = ChatColor.translateAlternateColorCodes('&', "&bMercado da Pesca");

    private final ShopConfig _shopConfig;
    private final PlayerProfileRepository _playerProfileRepository;

    @Inject
    public ShopMenu(ShopConfig shopConfig,  PlayerProfileRepository playerProfileRepository) {
        _shopConfig = shopConfig;
        _playerProfileRepository = playerProfileRepository;
    }

    public void open(Player player) {

        Inventory inv = Bukkit.createInventory(null, 54, TITLE);

        for (ShopItem shopItem : _shopConfig.getShopItems().values()) {
            inv.setItem(shopItem.getSlot(), shopItem.toItemStack());
        }

        double fishQuantity = _playerProfileRepository.getProfile(player.getUniqueId()).getFishBalance();
        String formattedFishQuantity = String.format("%.2fK", fishQuantity / 1000.0);
        ItemStack fishQuantityItem = createMenuItem(Material.RAW_FISH, "§bSaldo", Collections.singletonList("§7Você possui §b" + formattedFishQuantity + " Peixes."));
        fishQuantityItem.setDurability((short) 2);
        inv.setItem(48, fishQuantityItem);

        ItemStack optionsItem = createMenuItem(Material.PAPER, "§bOpções", Arrays.asList("§fBotão esquerdo §7para comprar 1 item", "§fBotão direito §7para selecionar a quantidade"));
        inv.setItem(50, optionsItem);

        player.openInventory(inv);
    }

    private ItemStack createMenuItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(name);

            if (lore != null && !lore.isEmpty()) {
                List<String> coloredLore = lore.stream()
                        .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                        .collect(Collectors.toList());
                meta.setLore(coloredLore);
            }
            item.setItemMeta(meta);
        }
        return item;
    }
}
