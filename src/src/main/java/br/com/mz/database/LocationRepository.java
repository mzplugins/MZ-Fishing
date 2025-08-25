package br.com.mz.database;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.result.DeleteResult;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.Optional;

@Singleton
public class LocationRepository {

    private final MongoCollection<Document> _locationsCollection;

    @Inject
    public LocationRepository(MongoManager mongoManager) {
        _locationsCollection = mongoManager.getDatabase().getCollection("locations");
    }

    public void saveLocation(String locationName, Location location) {
        Document locationDocument = new Document("_id", locationName)
                .append("world", location.getWorld().getName())
                .append("x", location.getX())
                .append("y", location.getY())
                .append("z", location.getZ())
                .append("yaw", location.getYaw())
                .append("pitch", location.getPitch());

        ReplaceOptions options = new ReplaceOptions().upsert(true);
        _locationsCollection.replaceOne(Filters.eq("_id", locationName), locationDocument, options);
    }

    public Optional<Location> loadLocation(String locationName) {
        Document doc = _locationsCollection.find(Filters.eq("_id", locationName)).first();

        if (doc == null) {
            return Optional.empty(); // Nenhum documento encontrado
        }

        String worldName = doc.getString("world");
        if (Bukkit.getWorld(worldName) == null) {
            return Optional.empty();
        }

        Location location = new Location(
                Bukkit.getWorld(worldName),
                doc.getDouble("x"),
                doc.getDouble("y"),
                doc.getDouble("z"),
                doc.getDouble("yaw").floatValue(),
                doc.getDouble("pitch").floatValue()
        );

        return Optional.of(location);
    }

    public long removeAllShopLocations() {
        DeleteResult result = _locationsCollection.deleteMany(Filters.regex("_id", "^shop"));
        return result.getDeletedCount();
    }
}