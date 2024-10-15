package com.bonker.stardewfishing.common;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;

import java.util.Random;

public class FishBehavior {
    private int idleTime;
    private float topSpeed;
    private float upAcceleration;
    private float downAcceleration;
    private int avgDistance;
    private int moveVariation;
    private String fishTexture;
    private int moveType;
    private float pointGain;
    private float pointLoss;


    public FishBehavior(int idleTime, float topSpeed, float upAcceleration, float downAcceleration, int avgDistance, int moveVariation, String fishTexture, int moveType) {
        this.idleTime = idleTime;
        this.topSpeed = topSpeed;
        this.upAcceleration = upAcceleration;
        this.downAcceleration = downAcceleration;
        this.avgDistance = avgDistance;
        this.moveVariation = moveVariation;
        this.fishTexture = fishTexture;
        this.moveType = moveType;
        this.pointGain = 1;
        this.pointLoss = 1;
    }
    public FishBehavior(int idleTime, float topSpeed, float upAcceleration, float downAcceleration, int avgDistance, int moveVariation, String fishTexture, int moveType, float pointGain, float pointLoss) {
        this.idleTime = idleTime;
        this.topSpeed = topSpeed;
        this.upAcceleration = upAcceleration;
        this.downAcceleration = downAcceleration;
        this.avgDistance = avgDistance;
        this.moveVariation = moveVariation;
        this.fishTexture = fishTexture;
        this.moveType = moveType;
        this.pointGain = pointGain;
        this.pointLoss = pointLoss;
    }



    public static final Codec<FishBehavior> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.INT.fieldOf("idle_time").forGetter(FishBehavior::getIdleTime),
            Codec.FLOAT.fieldOf("top_speed").forGetter(FishBehavior::getTopSpeed),
            Codec.FLOAT.fieldOf("up_acceleration").forGetter(FishBehavior::getUpAcceleration),
            Codec.FLOAT.fieldOf("down_acceleration").forGetter(FishBehavior::getDownAcceleration),
            Codec.INT.fieldOf("avg_distance").forGetter(FishBehavior::getAvgDistance),
            Codec.INT.fieldOf("move_variation").forGetter(FishBehavior::getMoveVariation),
            Codec.STRING.fieldOf("fish_texture").forGetter(FishBehavior::getFishTexture),
            Codec.INT.fieldOf("move_type").forGetter(FishBehavior::getMoveType)
    ).apply(inst, FishBehavior::new));

    public static final int MAX_HEIGHT = 127;

    public FishBehavior(FriendlyByteBuf buf) {
        this(buf.readVarInt(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readVarInt(), buf.readVarInt(), buf.readUtf(), buf.readVarInt(), buf.readFloat(), buf.readFloat());
    }

    public void writeToBuffer(FriendlyByteBuf buf) {
        buf.writeVarInt(idleTime);
        buf.writeFloat(topSpeed);
        buf.writeFloat(upAcceleration);
        buf.writeFloat(downAcceleration);
        buf.writeVarInt(avgDistance);
        buf.writeVarInt(moveVariation);
        buf.writeUtf(fishTexture);
        buf.writeVarInt(moveType);
        buf.writeFloat(pointGain);
        buf.writeFloat(pointLoss);
    }

    public boolean shouldMoveNow(int idleTicks, Random random) {
        if (idleTime == 0) return true;
        if (idleTime == 1) return idleTicks == 1;

        int variation = idleTicks / 2;
        float chancePerTick = 1F / variation;

        if (idleTicks >= idleTime - variation) {
            return random.nextFloat() <= chancePerTick;
        }

        return false;
    }

    public int pickNextTargetPos(int oldPos, Random random) {
        int shortestDistance = avgDistance - moveVariation;
        int longestDistance = avgDistance + moveVariation;

        int downLowerLimit = oldPos - shortestDistance;
        int upLowerLimit = oldPos + shortestDistance;

        boolean canGoDown = downLowerLimit >= 0;
        boolean canGoUp = upLowerLimit <= MAX_HEIGHT;

        boolean goingUp;
        if (canGoUp && canGoDown) {
            goingUp = random.nextBoolean();
        } else {
            goingUp = canGoUp;
        }

        int distance = random.nextInt(shortestDistance, longestDistance + 1);

        return Mth.clamp(oldPos + distance * (goingUp ? 1 : -1), 0, MAX_HEIGHT);
    }

    public int getIdleTime() {
        return idleTime;
    }
    public void setIdleTime(int idleTime) {
        this.idleTime = idleTime;
    }
    public float getDownAcceleration() {
        return downAcceleration;
    }
    public void setDownAcceleration(float downAcceleration) {
        this.downAcceleration = downAcceleration;
    }
    public float getTopSpeed() {
        return topSpeed;
    }
    public void setTopSpeed(float topSpeed) {
        this.topSpeed = topSpeed;
    }
    public float getUpAcceleration() {
        return upAcceleration;
    }
    public void setUpAcceleration(float upAcceleration) {
        this.upAcceleration = upAcceleration;
    }
    public int getAvgDistance() {
        return avgDistance;
    }
    public void setAvgDistance(int avgDistance) {
        this.avgDistance = avgDistance;
    }
    public int getMoveVariation() {
        return moveVariation;
    }
    public void setMoveVariation(int moveVariation) {
        this.moveVariation = moveVariation;
    }
    public String getFishTexture() {
        return fishTexture;
    }
    public void setFishTexture(String fishTexture) {
        this.fishTexture = fishTexture;
    }
    public int getMoveType() {
        return moveType;
    }
    public void setMoveType(int moveType) {
        this.moveType = moveType;
    }
    public float getPointGain() {return pointGain;}
    public void setPointGain(float pointGain) {
        this.pointGain = pointGain;
    }
    public float getPointLoss() {
        return pointLoss;
    }
    public void setPointLoss(float pointLoss) {
        this.pointLoss = pointLoss;
    }
}
