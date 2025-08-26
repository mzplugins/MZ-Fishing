package br.com.mz;

import br.com.mz.database.LocationRepository;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Singleton
public class ShopManager {

    private final Main _plugin;
    private final LocationRepository _locationRepository;
    private final List<ArmorStand> _shopVillagerName;

    private BukkitTask anchorTask;

    @Inject
    public ShopManager(Main plugin, LocationRepository locationRepository) {
        _plugin = plugin;
        _locationRepository = locationRepository;
        _shopVillagerName = new ArrayList<>();
    }

    public void loadInitialVillager() {
        _locationRepository.loadLocation("shop").ifPresent(this::spawnOrUpdateVillager);
    }

    public void spawnOrUpdateVillager(final Location location) {
        removeAllShopVillagers();

        final Villager shopVillager = (Villager) location.getWorld().spawnEntity(location, EntityType.VILLAGER);

        shopVillager.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 255, true, false));
        shopVillager.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 128, true, false));
        shopVillager.setMetadata("FISHING_SHOP_VILLAGER", new FixedMetadataValue(_plugin, true));

        final Location anchorLocation = location.clone();

        this.anchorTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!shopVillager.isValid() || shopVillager.getLocation().distanceSquared(anchorLocation) > 0.01) {
                    shopVillager.teleport(anchorLocation);
                }

                shopVillager.setVelocity(new Vector(0, 0, 0));
            }
        }.runTaskTimer(_plugin, 0L, 5L);


        List<String> lines = Arrays.asList(
                "§b§lLOJA DA PESCA",
                "§fClique para abrir"
        );

        Location currentLineLocation = location.clone().add(0, 1.95, 0);
        double lineSpacing = 0.25;

        for (String line : lines) {
            ArmorStand armorStand = (ArmorStand) location.getWorld().spawnEntity(currentLineLocation, EntityType.ARMOR_STAND);

            armorStand.setGravity(false);
            armorStand.setVisible(false);
            armorStand.setSmall(true);
            armorStand.setMarker(true);

            armorStand.setCustomName(ChatColor.translateAlternateColorCodes('&', line));
            armorStand.setCustomNameVisible(true);

            armorStand.setMetadata("FISHING_SHOP_VILLAGER_NAME", new FixedMetadataValue(_plugin, true));

            currentLineLocation.subtract(0, lineSpacing, 0);
            _shopVillagerName.add(armorStand);
        }

        _plugin.getLogger().info("Shop set successfully!");
    }

    public void removeAllShopVillagers() {
        if (this.anchorTask != null) {
            this.anchorTask.cancel();
            this.anchorTask = null;
        }

        _shopVillagerName.forEach(Entity::remove);
        _shopVillagerName.clear();

        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity.hasMetadata("FISHING_SHOP_VILLAGER") || entity.hasMetadata("FISHING_SHOP_VILLAGER_NAME")) {
                    if (entity.isValid()) {
                        entity.remove();
                    }
                }
            }
        }
    }
}