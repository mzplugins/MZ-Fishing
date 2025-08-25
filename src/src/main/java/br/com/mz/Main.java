package br.com.mz;

import br.com.mz.commands.FishCommand;
import br.com.mz.database.MongoManager;
import br.com.mz.listeners.FishingListener;
import br.com.mz.listeners.ShopListener;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class Main extends JavaPlugin {
    private Injector _injector;
    private MongoManager _mongoManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        _injector = Guice.createInjector(new PluginModule(this));
        _mongoManager = _injector.getInstance(MongoManager.class);
        ShopManager shopManager = _injector.getInstance(ShopManager.class);
        ShopListener shopListener = _injector.getInstance(ShopListener.class);
        FishingListener fishingListener = _injector.getInstance(FishingListener.class);

        shopManager.loadInitialVillager();

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(shopListener, this);
        pm.registerEvents(fishingListener, this);

        FishCommand fishCommand = _injector.getInstance(FishCommand.class);
        Objects.requireNonNull(getCommand("pesca")).setExecutor(fishCommand);

        getLogger().info("Enabled!");
    }

    @Override
    public void onDisable() {

        if (_mongoManager != null) {
            _mongoManager.close();
        }

        getLogger().info("Disabled.");
    }
}
