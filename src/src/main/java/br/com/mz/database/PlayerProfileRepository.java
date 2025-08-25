package br.com.mz.database;

import br.com.mz.models.PlayerProfile;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import org.bson.conversions.Bson;
import java.util.UUID;
import java.util.Optional;

@Singleton
public class PlayerProfileRepository {

    private final MongoCollection<PlayerProfile> _profilesCollection;

    @Inject
    public PlayerProfileRepository(MongoManager mongoManager) {
        _profilesCollection = mongoManager.getDatabase().getCollection("players", PlayerProfile.class);
    }

    public PlayerProfile getProfile(UUID uuid) {
        return Optional.ofNullable(
                _profilesCollection
                        .find(Filters.eq("_id", uuid))
                        .first())
                .orElse(new PlayerProfile(uuid));
    }

    public void addToBalance(UUID uuid, double amount) {
        Bson filter = Filters.eq("_id", uuid);
        Bson update = Updates.inc("fishBalance", amount);
        UpdateOptions options = new UpdateOptions().upsert(true);

        _profilesCollection.updateOne(filter, update, options);
    }

    public void saveProfile(PlayerProfile profile) {
        Bson filter = Filters.eq("_id", profile.getUuid());
        _profilesCollection.replaceOne(filter, profile);
    }
}
