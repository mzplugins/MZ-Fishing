package br.com.mz;

import br.com.mz.configs.FishingConfig;
import br.com.mz.database.PlayerProfileRepository;
import br.com.mz.ActionBar;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Singleton
public class FishingManager {

    private final Main _plugin;
    private final FishingConfig _fishingConfig;
    private final PlayerProfileRepository _profileRepository;
    private final MessagesManager _messagesManager;
    private final Set<UUID> playersInFishingMode = new HashSet<>();
    private final Map<UUID, BukkitTask> activeFishingTasks = new HashMap<>();
    private final Map<UUID, BukkitTask> actionBarTasks = new HashMap<>();
    private static class TemporaryMessage {
        final String message;
        final long expiryTime;
        TemporaryMessage(String message, long expiryTime) {
            this.message = message;
            this.expiryTime = expiryTime;
        }
    }
    private final Map<UUID, TemporaryMessage> temporaryMessageMap = new ConcurrentHashMap<>();

    @Inject
    public FishingManager(Main plugin, FishingConfig fishingConfig, PlayerProfileRepository profileRepository, MessagesManager messagesManager) {
        _plugin = plugin;
        _fishingConfig = fishingConfig;
        _profileRepository = profileRepository;
        _messagesManager = messagesManager;
    }

    public void enterFishingMode(Player player) { playersInFishingMode.add(player.getUniqueId()); }
    public void leaveFishingMode(Player player) { playersInFishingMode.remove(player.getUniqueId()); stopFishing(player, false); }
    public boolean isInFishingMode(Player player) { return playersInFishingMode.contains(player.getUniqueId()); }

    public void startFishing(Player player) {
        UUID playerUUID = player.getUniqueId();
        if (activeFishingTasks.containsKey(playerUUID)) {
            return;
        }
        long intervalTicks = (long)_fishingConfig.getCatchIntervalSeconds() * 20L;
        BukkitTask task = new BukkitRunnable() {
            public void run() {
                double amount = _fishingConfig.getFishesPerCatch();
                _profileRepository.addToBalance(playerUUID, amount);
                String rewardMsg = _messagesManager.getMessage("action-bar.catch-reward", Collections.singletonMap("%quantity%", String.valueOf((int)amount)));
                showTemporaryActionBar(player, rewardMsg, 3);
                if (ThreadLocalRandom.current().nextDouble(100.0) < _fishingConfig.getMultipleCatchChance()) {
                    int bonusCatches = _fishingConfig.getMultipleCatchAmount();
                    double bonusAmount = bonusCatches * amount;
                    _profileRepository.addToBalance(playerUUID, bonusAmount);
                    String multipleCatchMsg = _messagesManager.getMessage("action-bar.multiple-catch");
                    String bonusMsg = _messagesManager.getMessage("action-bar.multiple-catch-reward", Collections.singletonMap("%quantity%", String.valueOf((int)bonusAmount)));
                    showTemporaryActionBar(player, multipleCatchMsg + " " + bonusMsg, 4);
                }
            }
        }.runTaskTimer(_plugin, intervalTicks, intervalTicks);

        activeFishingTasks.put(playerUUID, task);

        BukkitTask actionBarTask = new BukkitRunnable() {
            public void run() {
                TemporaryMessage tempMsg = temporaryMessageMap.get(playerUUID);
                if (tempMsg != null && System.currentTimeMillis() > tempMsg.expiryTime) {
                    temporaryMessageMap.remove(playerUUID);
                    tempMsg = null;
                }
                String messageToSend;
                if (tempMsg != null) {
                    messageToSend = tempMsg.message;
                } else {
                    messageToSend = _messagesManager.getMessage("action-bar.fishing");
                }
                ActionBar.send(player, messageToSend);
            }
        }.runTaskTimer(_plugin, 0L, 20L);

        actionBarTasks.put(playerUUID, actionBarTask);
    }

    public void stopFishing(Player player, boolean showMessage) {
        BukkitTask rewardTask = activeFishingTasks.remove(player.getUniqueId());
        if (rewardTask != null)
            rewardTask.cancel();
        BukkitTask actionBarTask = actionBarTasks.remove(player.getUniqueId());
        if (actionBarTask != null)
            actionBarTask.cancel();
        temporaryMessageMap.remove(player.getUniqueId());
        if (showMessage) {
            String stopMessage = _messagesManager.getMessage("action-bar.stop-fishing");
            ActionBar.send(player, stopMessage);
        }
    }

    public boolean isActivelyFishing(Player player) { return activeFishingTasks.containsKey(player.getUniqueId()); }

    public void showTemporaryActionBar(Player player, String message, int durationSeconds) {
        long expiryTime = System.currentTimeMillis() + (durationSeconds * 1000L);
        temporaryMessageMap.put(player.getUniqueId(), new TemporaryMessage(message, expiryTime));
    }
}