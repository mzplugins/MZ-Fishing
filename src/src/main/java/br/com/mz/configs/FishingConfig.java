package br.com.mz.configs;

import br.com.mz.Main;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;

@Singleton
public class FishingConfig {

    private final Main _plugin;
    private FileConfiguration _config;

    private int _catchIntervalSeconds;
    private int _fishesPerCatch;
    private double _multipleCatchChance;
    private int _multipleCatchAmount;

    @Inject
    public FishingConfig(Main plugin) {
        _plugin = plugin;
        loadConfig();
    }

    private void loadConfig() {
        File configFile = new File(_plugin.getDataFolder(), "fishing.yml");

        if (!configFile.exists()) {
            _plugin.saveResource("fishing.yml", false);
        }

        _config = YamlConfiguration.loadConfiguration(configFile);
        loadValues();
    }

    private void loadValues() {
        _catchIntervalSeconds = _config.getInt("catch-interval-seconds", 5);
        _fishesPerCatch = _config.getInt("fishes-per-catch", 10);
        _multipleCatchChance = _config.getDouble("multiple-catch-chance", 15.0);
        _multipleCatchAmount = _config.getInt("multiple-catch-amount", 3);
    }

    public int getCatchIntervalSeconds() {
        return _catchIntervalSeconds;
    }

    public int getFishesPerCatch() {
        return _fishesPerCatch;
    }

    public double getMultipleCatchChance() {
        return _multipleCatchChance;
    }

    public int getMultipleCatchAmount() {
        return _multipleCatchAmount;
    }
}
