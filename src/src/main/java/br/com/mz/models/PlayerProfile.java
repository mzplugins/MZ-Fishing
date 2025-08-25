package br.com.mz.models;

import org.bson.codecs.pojo.annotations.BsonId;
import java.util.UUID;

public class PlayerProfile {

    @BsonId
    private UUID uuid;
    private double fishBalance;

    public PlayerProfile() {}

    public PlayerProfile(UUID uuid) {
        this.uuid = uuid;
        this.fishBalance = 0.0;
    }

    public UUID getUuid() {
        return uuid;
    }

    public double getFishBalance() {
        return fishBalance;
    }

    public void setFishBalance(double fishBalance) {
        this.fishBalance = fishBalance;
    }
}
