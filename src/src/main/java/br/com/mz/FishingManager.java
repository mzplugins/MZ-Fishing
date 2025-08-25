package br.com.mz;

import br.com.mz.configs.FishingConfig;
import br.com.mz.database.PlayerProfileRepository;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

@Singleton
public class FishingManager {

    private final Main _plugin;
    private final FishingConfig _fishingConfig;
    private final PlayerProfileRepository _profileRepository;
    private final MessagesManager _messagesManager;

    private final Set<UUID> playersInFishingMode = new HashSet<>();
    private final Map<UUID, BukkitTask> activeFishingTasks = new HashMap<>();
    private final Map<UUID, BukkitTask> actionBarTasks = new HashMap<>();

    private static record TemporaryMessage(String message, long expiryTime) {}
    private final Map<UUID, TemporaryMessage> temporaryMessageMap = new ConcurrentHashMap<>();

    private static List<String> PULSE_ANIMATION_FRAMES;

    @Inject
    public FishingManager(Main plugin, FishingConfig fishingConfig, PlayerProfileRepository profileRepository, MessagesManager messagesManager) {
        _plugin = plugin;
        _fishingConfig = fishingConfig;
        _profileRepository = profileRepository;
        _messagesManager = messagesManager;

        PULSE_ANIMATION_FRAMES = generateFadeOutFrames(
                _messagesManager.getMessage("action-bar.fishing"),
                20,
                15
        );
    }
    public void enterFishingMode(Player player) {
        playersInFishingMode.add(player.getUniqueId());
    }

    public void leaveFishingMode(Player player) {
        playersInFishingMode.remove(player.getUniqueId());
        stopFishing(player, false);
    }

    public boolean isInFishingMode(Player player) {
        return playersInFishingMode.contains(player.getUniqueId());
    }

    public void startFishing(Player player) {
        UUID playerUUID = player.getUniqueId();
        if (activeFishingTasks.containsKey(playerUUID)) {
            return;
        }

        long intervalTicks = _fishingConfig.getCatchIntervalSeconds() * 20L;

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(_plugin, () -> {
            double amount = _fishingConfig.getFishesPerCatch();
            _profileRepository.addToBalance(playerUUID, amount);
            String rewardMsg = _messagesManager.getMessage("action-bar.catch-reward", Map.of("%quantity%", String.valueOf((int)amount)));
            showTemporaryActionBar(player, rewardMsg, 3);

            if (ThreadLocalRandom.current().nextDouble(100.0) < _fishingConfig.getMultipleCatchChance()) {
                int bonusCatches = _fishingConfig.getMultipleCatchAmount();
                double bonusAmount = bonusCatches * amount;
                _profileRepository.addToBalance(playerUUID, bonusAmount);
                String multipleCatchMsg = _messagesManager.getMessage("action-bar.multiple-catch");
                String bonusMsg = _messagesManager.getMessage("action-bar.multiple-catch-reward", Map.of("%quantity%", String.valueOf((int)bonusAmount)));
                showTemporaryActionBar(player, multipleCatchMsg + " " + bonusMsg, 4);
            }

        }, intervalTicks, intervalTicks);

        activeFishingTasks.put(playerUUID, task);

        BukkitTask actionBarTask = Bukkit.getScheduler().runTaskTimer(_plugin, new Runnable() {
            private int animationFrame = 0;
            final List<String> animationFrames = PULSE_ANIMATION_FRAMES;

            @Override
            public void run() {
                TemporaryMessage tempMsg = temporaryMessageMap.get(playerUUID);

                if (tempMsg != null && System.currentTimeMillis() > tempMsg.expiryTime()) {
                    temporaryMessageMap.remove(playerUUID);
                    tempMsg = null;
                }

                if (tempMsg != null) {
                    player.sendActionBar(LegacyComponentSerializer.legacyAmpersand().deserialize(tempMsg.message()));
                } else {
                    String currentFrameText = PULSE_ANIMATION_FRAMES.get(animationFrame);
                    player.sendActionBar(LegacyComponentSerializer.legacyAmpersand().deserialize(currentFrameText));

                    animationFrame++;
                    if (animationFrame >= PULSE_ANIMATION_FRAMES.size()) {
                        animationFrame = 0;
                    }
                }
            }
        }, 0L, 2L);

        actionBarTasks.put(playerUUID, actionBarTask);
    }

    public void stopFishing(Player player, boolean showMessage) {
        BukkitTask rewardTask = activeFishingTasks.remove(player.getUniqueId());
        if (rewardTask != null) {
            rewardTask.cancel();
        }

        BukkitTask actionBarTask = actionBarTasks.remove(player.getUniqueId());
        if (actionBarTask != null) {
            actionBarTask.cancel();
        }

        temporaryMessageMap.remove(player.getUniqueId());

        if(!showMessage) {
            return;
        }

        new BukkitRunnable() {
            private int ticks = 0;
            @Override
            public void run() {
                int durationInSeconds = 3;
                if (ticks >= durationInSeconds) {
                    player.sendActionBar(Component.text(""));
                    this.cancel();
                    return;
                }
                player.sendActionBar(_messagesManager.getActionBarComponent("action-bar.stop-fishing"));
                ticks++;
            }
        }.runTaskTimer(_plugin, 0L, 20L);
    }

    public boolean isActivelyFishing(Player player) {
        return activeFishingTasks.containsKey(player.getUniqueId());
    }

    public void showTemporaryActionBar(Player player, String message, int durationSeconds) {
        long expiryTime = System.currentTimeMillis() + (durationSeconds * 1000L);
        temporaryMessageMap.put(player.getUniqueId(), new TemporaryMessage(message, expiryTime));
    }

    private static List<String> generateFadeOutFrames(String baseText, int fadeSteps, int invisibleSteps) {
        List<String> frames = new ArrayList<>();

        java.awt.Color start = new java.awt.Color(85, 255, 255);
        java.awt.Color end = new java.awt.Color(188, 236, 236);

        for (int i = 0; i < fadeSteps; i++) {
            float factor = (float) i / (fadeSteps - 1);

            int r = (int) (start.getRed() * (1 - factor) + end.getRed() * factor);
            int g = (int) (start.getGreen() * (1 - factor) + end.getGreen() * factor);
            int b = (int) (start.getBlue() * (1 - factor) + end.getBlue() * factor);

            String hex = String.format("&#%02x%02x%02x", r, g, b);
            frames.add(hex + baseText);
        }

        for (int i = 0; i < invisibleSteps; i++) {
            frames.add("");
        }

        return frames;
    }
}