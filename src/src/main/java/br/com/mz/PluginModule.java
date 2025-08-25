package br.com.mz;

import br.com.mz.commands.FishCommand;
import br.com.mz.configs.FishingConfig;
import br.com.mz.configs.ShopConfig;
import br.com.mz.database.LocationRepository;
import br.com.mz.database.MongoManager;
import br.com.mz.database.PlayerProfileRepository;
import br.com.mz.guis.ShopMenu;
import br.com.mz.listeners.FishingListener;
import br.com.mz.listeners.ShopListener;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class PluginModule extends AbstractModule {
    private final Main _plugin;

    public PluginModule(Main plugin) {
        _plugin = plugin;
    }

    @Override
    protected void configure() {
        bind(Main.class).toInstance(_plugin);

        bind(MongoManager.class).in(Scopes.SINGLETON);
        bind(LocationRepository.class).in(Scopes.SINGLETON);
        bind(PlayerProfileRepository.class).in(Scopes.SINGLETON);

        bind(ShopManager.class).in(Scopes.SINGLETON);
        bind(FishingManager.class).in(Scopes.SINGLETON);
        bind(PurchaseManager.class).in(Scopes.SINGLETON);

        bind(ShopMenu.class).in(Scopes.SINGLETON);

        bind(ShopConfig.class).in(Scopes.SINGLETON);
        bind(FishingConfig.class).in(Scopes.SINGLETON);

        bind(FishCommand.class).in(Scopes.SINGLETON);

        bind(ShopListener.class).in(Scopes.SINGLETON);
        bind(FishingListener.class).in(Scopes.SINGLETON);
    }
}
