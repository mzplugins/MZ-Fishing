package br.com.mz.listeners;

import br.com.mz.FishingManager;
import br.com.mz.database.PlayerProfileRepository;
import br.com.mz.models.PlayerProfile;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.*;


@Singleton
public class FishingListener implements Listener {

    private final FishingManager _fishingManager;
    private final PlayerProfileRepository _playerProfileRepository;

    @Inject
    public FishingListener(FishingManager fishingManager,  PlayerProfileRepository playerProfileRepository) {
        _fishingManager = fishingManager;
        _playerProfileRepository = playerProfileRepository;
    }

    @EventHandler
    public void onPlayerRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (player.getInventory().getItemInHand().getType() == Material.FISHING_ROD && _fishingManager.isInFishingMode(player)) {
            event.setCancelled(true);

            if (_fishingManager.isActivelyFishing(player)) {
                _fishingManager.stopFishing(player, true);
            } else {
                _fishingManager.startFishing(player);
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        _fishingManager.leaveFishingMode(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();

        if (!_fishingManager.isInFishingMode(player)) {
            return;
        }

        _fishingManager.leaveFishingMode(player);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerProfile profile = _playerProfileRepository.getProfile(player.getUniqueId());
        _playerProfileRepository.saveProfile(profile);
    }
}