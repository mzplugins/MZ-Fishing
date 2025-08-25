package br.com.mz;

import br.com.mz.database.LocationRepository;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Singleton
public class ShopManager {

    private final Main _plugin;
    private final LocationRepository _locationRepository;
    private Villager _shopVillager;
    private final List<ArmorStand> _shopVillagerName;

    @Inject
    public ShopManager(Main plugin, LocationRepository locationRepository) {
        _plugin = plugin;
        _locationRepository = locationRepository;
        _shopVillagerName = new ArrayList<>();
    }

    public void loadInitialVillager() {
        _locationRepository.loadLocation("shop").ifPresent(this::spawnOrUpdateVillager);
    }

    public void spawnOrUpdateVillager(Location location) {
        _shopVillager = (Villager) location.getWorld().spawnEntity(location, EntityType.VILLAGER);

        _shopVillager.setProfession(Villager.Profession.FISHERMAN);
        _shopVillager.setVillagerType(Villager.Type.PLAINS);
        _shopVillager.setAI(false);
        _shopVillager.setSilent(true);
        _shopVillager.setInvulnerable(true);
        _shopVillager.setCollidable(false);

        _shopVillager.setMetadata("FISHING_SHOP_VILLAGER", new FixedMetadataValue(_plugin, true));

        List<String> lines = Arrays.asList(
                "§b§lLOJA DA PESCA",
                "§fClique para abrir"
        );

        Location currentLineLocation = location.clone().add(0, 2.1, 0);
        double lineSpacing = 0.25;

        for (String line : lines) {
            ArmorStand armorStand = (ArmorStand) location.getWorld().spawnEntity(currentLineLocation, EntityType.ARMOR_STAND);

            armorStand.setGravity(false);
            armorStand.setVisible(false);
            armorStand.setSmall(true);
            armorStand.setMarker(true);
            armorStand.setInvulnerable(true);

            Component name = LegacyComponentSerializer.legacySection().deserialize(line);
            armorStand.customName(name);
            armorStand.setCustomNameVisible(true);

            armorStand.setMetadata("FISHING_SHOP_VILLAGER_NAME", new FixedMetadataValue(_plugin, true));

            currentLineLocation.subtract(0, lineSpacing, 0);

            _shopVillagerName.add(armorStand);
        }

        _plugin.getLogger().info("Shop set successfully!");
    }

    public Villager getShopVillager() {
        return _shopVillager;
    }

    public void removeAllShopVillagers() {
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity.hasMetadata("FISHING_SHOP_VILLAGER")) {
                    entity.remove();
                }
                if (entity.hasMetadata("FISHING_SHOP_VILLAGER_NAME")) {
                    entity.remove();
                }
            }
        }
    }
}
