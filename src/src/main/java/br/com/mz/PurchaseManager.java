package br.com.mz;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Singleton
public class PurchaseManager {

    private final Map<UUID, ShopItem> pendingPurchases = new HashMap<>();
    private final MessagesManager messagesManager;

    @Inject
    public PurchaseManager(MessagesManager messagesManager) {
        this.messagesManager = messagesManager;
    }

    public boolean isWaitingForInput(Player player) {
        return pendingPurchases.containsKey(player.getUniqueId());
    }

    public void startCustomPurchase(Player player, ShopItem item) {
        pendingPurchases.put(player.getUniqueId(), item);
        player.closeInventory();

        messagesManager.sendMessage(player, "shop.custom-purchase-prompt", Collections.singletonMap("%item_name%", item.getDisplayName()));
        messagesManager.sendMessage(player, "shop.custom-purchase-cancel-prompt");
    }

    public void cancelPurchase(Player player) {
        pendingPurchases.remove(player.getUniqueId());
        messagesManager.sendMessage(player, "shop.purchase-cancelled");
    }

    public ShopItem getPendingPurchaseItem(Player player) {
        return pendingPurchases.get(player.getUniqueId());
    }

    public void clearPendingPurchase(Player player) {
        pendingPurchases.remove(player.getUniqueId());
    }

    public void executePurchaseCommand(Player player, ShopItem item, int quantity) {
        if (item.getCommand().isEmpty()) {
            messagesManager.sendMessage(player, "shop.item-no-command");
            return;
        }

        String commandToExecute = item.getCommand()
                .replace("%player%", player.getName())
                .replace("%quantity%", String.valueOf(quantity));

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), commandToExecute);
    }
}