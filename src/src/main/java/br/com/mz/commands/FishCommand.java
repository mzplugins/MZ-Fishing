package br.com.mz.commands;

import br.com.mz.FishingManager;
import br.com.mz.MessagesManager;
import br.com.mz.ShopManager;
import br.com.mz.database.LocationRepository;
import com.google.inject.Inject;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;

public class FishCommand implements CommandExecutor {

    private final LocationRepository _locationRepository;
    private final ShopManager _shopManager;
    private final FishingManager _fishingManager;
    private final MessagesManager _messagesManager;

    @Inject
    public FishCommand(LocationRepository locationRepository, ShopManager shopManager,  FishingManager fishingManager,  MessagesManager messagesManager) {
        _locationRepository = locationRepository;
        _shopManager = shopManager;
        _fishingManager = fishingManager;
        _messagesManager = messagesManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {

        if (!(sender instanceof Player player)) {
            return true;
        }

        if(args.length == 0){
            fishCommand(player);
            return true;
        }

        if(args.length == 1 && args[0].equalsIgnoreCase("sair")){
            leaveFishCommand(player);
            return true;
        }

        if (args.length != 2 || (!args[0].equalsIgnoreCase("set") && !args[0].equalsIgnoreCase("remove"))) {
            player.sendMessage("§8§m------------§r §b§lAjuda: Pesca §r§8§m------------");
            player.sendMessage("§bComandos do Jogador:");
            player.sendMessage("§f/pesca §7- Teletransporta para a área de pesca.");
            player.sendMessage("§f/pesca sair §7- Sai da área de pesca e volta ao spawn.");

            if(!player.hasPermission("mz-fishing.admin") && !player.hasPermission("mz-fishing.*")) {
                player.sendMessage("§8§m-------------------------------------");
                return true;
            }

            player.sendMessage("§cComandos de Admin:");
            player.sendMessage("§f/pesca set spawn §7- Define o local de pesca.");
            player.sendMessage("§f/pesca set leave §7- Define o local de saída.");
            player.sendMessage("§f/pesca set shop §7- Define o local da loja.");
            player.sendMessage("§f/pesca remove shop §7- Remove todos os NPCs de loja.");
            player.sendMessage("§8§m-------------------------------------");

            return true;
        }

        if(!player.hasPermission("mz-fishing.admin")) {
            _messagesManager.sendMessage(player, "general.no-permission");
            return true;
        }

        if(args[0].equalsIgnoreCase("set")) {
            setCommand(args, player);
            return true;
        }

        if(args[0].equalsIgnoreCase("remove")) {
            removeCommand(args, player);
            return true;
        }

        return true;
    }

    private void leaveFishCommand(Player player){

        if(!player.hasPermission("mz-fishing.fish")) {
            _messagesManager.sendMessage(player, "general.no-permission");
            return;
        }

        Optional<Location> leaveLocationOpt = _locationRepository.loadLocation("leave");
        if (leaveLocationOpt.isEmpty()) {
            return;
        }

        _fishingManager.leaveFishingMode(player);
        player.teleport(leaveLocationOpt.get());
    }

    private void fishCommand(Player player){

        if(!player.hasPermission("mz-fishing.fish")) {
            _messagesManager.sendMessage(player, "general.no-permission");
            return;
        }

        Optional<Location> spawnLocationOpt = _locationRepository.loadLocation("spawn");

        if (spawnLocationOpt.isEmpty()) {
            _messagesManager.sendMessage(player, "general.location-not-set");
            return;
        }

        player.teleport(spawnLocationOpt.get());
        _fishingManager.enterFishingMode(player);
    }

    private void setCommand(String[] args, Player player) {
        String locationType = args[1].toLowerCase();
        Location playerLocation = player.getLocation();

        switch (locationType) {
            case "spawn":
                _locationRepository.saveLocation("spawn", playerLocation);
                _messagesManager.sendMessage(player, "commands.set-location-success", Map.of("%location_type%", "spawn"));
                break;
            case "leave":
                _locationRepository.saveLocation("leave", playerLocation);
                _messagesManager.sendMessage(player, "commands.set-location-success", Map.of("%location_type%", "leave"));
                break;
            case "shop":
                _locationRepository.saveLocation("shop", playerLocation);
                _shopManager.spawnOrUpdateVillager(playerLocation);
                _messagesManager.sendMessage(player, "commands.set-location-success", Map.of("%location_type%", "shop"));
                break;
            default:
                player.sendMessage("§cSet invalido. Use: spawn, leave ou shop.");
                break;
        }
    }

    private void removeCommand(String[] args, Player player){
        String locationType = args[1].toLowerCase();

        if (locationType.equals("shop")) {
            _locationRepository.removeAllShopLocations();
            _shopManager.removeAllShopVillagers();
            _messagesManager.sendMessage(player, "commands.remove-all-entities");
        } else {
            player.sendMessage("§cRemove invalido. Use: shop.");
        }
    }
}
