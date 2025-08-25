package br.com.mz.database;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import br.com.mz.Main;
import org.bson.UuidRepresentation;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bukkit.configuration.file.FileConfiguration;

@Singleton
public class MongoManager {

    private final MongoClient _mongoClient;
    private final MongoDatabase _database;

    @Inject
    public MongoManager(Main plugin) {
        FileConfiguration config = plugin.getConfig();
        String connectionString = config.getString("mongodb.connection-string");
        String databaseName = config.getString("mongodb.database");

        CodecRegistry pojoCodecRegistry = CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build());
        CodecRegistry codecRegistry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), pojoCodecRegistry);

        assert connectionString != null;

        MongoClientSettings clientSettings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(connectionString))
                .uuidRepresentation(UuidRepresentation.STANDARD)
                .codecRegistry(codecRegistry)
                .build();

        _mongoClient = MongoClients.create(clientSettings);

        assert databaseName != null;
        _database = _mongoClient.getDatabase(databaseName);

        plugin.getLogger().info("MongoDB connection done!");
    }

    public MongoDatabase getDatabase() {
        return _database;
    }

    public void close() {
        if (_mongoClient != null) {
            _mongoClient.close();
        }
    }
}