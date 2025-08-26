package br.com.mz.listeners;

import br.com.mz.Main;
import br.com.mz.MessagesManager;
import br.com.mz.PurchaseManager;
import br.com.mz.ShopItem;
import br.com.mz.configs.ShopConfig;
import br.com.mz.database.PlayerProfileRepository;
import br.com.mz.guis.ShopMenu;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class ShopListener implements Listener {

    private final Main _plugin;
    private final ShopMenu _shopMenu;
    private final ShopConfig _shopConfig;
    private final PurchaseManager _purchaseManager;
    private final PlayerProfileRepository _profileRepository;
    private final MessagesManager _messagesManager;

    @Inject
    public ShopListener(Main plugin, ShopMenu shopMenu, ShopConfig shopConfig,  PurchaseManager purchaseManager, PlayerProfileRepository profileRepository,  MessagesManager messagesManager) {
        _plugin = plugin;
        _shopMenu = shopMenu;
        _shopConfig = shopConfig;
        _purchaseManager = purchaseManager;
        _profileRepository = profileRepository;
        _messagesManager = messagesManager;
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Entity clickedEntity = event.getRightClicked();

        if (clickedEntity.hasMetadata("FISHING_SHOP_VILLAGER")) {
            event.setCancelled(true);
            _shopMenu.open(event.getPlayer());
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {

        if (!event.getView().getTitle().equals(ShopMenu.TITLE)) {
            return;
        }

        event.setCancelled(true);

        ShopItem clickedItem = _shopConfig.getItemBySlot(event.getRawSlot());

        if (clickedItem != null) {
            Player player = (Player) event.getWhoClicked();

            if(event.isLeftClick()){
                handlePurchase(player, clickedItem, 1);
            } else if (event.isRightClick()) {
                _purchaseManager.startCustomPurchase(player, clickedItem);
            }
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        if (_purchaseManager.isWaitingForInput(player)) {
            event.setCancelled(true);

            String plainMessage = event.getMessage();

            if (plainMessage.equalsIgnoreCase("cancelar")) {
                _purchaseManager.cancelPurchase(player);
                return;
            }

            try {
                int quantity = Integer.parseInt(plainMessage);

                if (quantity <= 0) {
                    _messagesManager.sendMessage(player, "shop.positive-number-required");
                    return;
                }

                ShopItem itemToBuy = _purchaseManager.getPendingPurchaseItem(player);

                Bukkit.getScheduler().runTask(_plugin, () -> {
                    handlePurchase(player, itemToBuy, quantity);
                });

            } catch (NumberFormatException e) {
                _messagesManager.sendMessage(player, "shop.invalid-number", Collections.singletonMap("%input%", plainMessage));
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();

        if (entity.hasMetadata("FISHING_SHOP_VILLAGER") || entity.hasMetadata("FISHING_SHOP_VILLAGER_NAME")) {
            event.setCancelled(true);
        }
    }

    private void handlePurchase(Player player, ShopItem item, int quantity) {
        double totalPrice = item.getPrice() * quantity;
        double playerBalance = _profileRepository.getProfile(player.getUniqueId()).getFishBalance();

        if (playerBalance >= totalPrice) {
            _profileRepository.addToBalance(player.getUniqueId(), -totalPrice);
            _purchaseManager.executePurchaseCommand(player, item, quantity);

            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("%quantity%", String.valueOf(quantity));
            placeholders.put("%item_name%", item.getDisplayName());
            placeholders.put("%price%", String.valueOf(totalPrice));
            _messagesManager.sendMessage(player, "shop.purchase-success", placeholders);

            if(_purchaseManager.isWaitingForInput(player)) {
                _purchaseManager.clearPendingPurchase(player);
            }

        }else {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("%price%", String.valueOf(totalPrice));
            placeholders.put("%balance%", String.valueOf(playerBalance));
            _messagesManager.sendMessage(player, "shop.insufficient-funds", placeholders);
        }
    }
}