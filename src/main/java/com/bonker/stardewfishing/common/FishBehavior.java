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
    private float gravity;
    private float bobberUpAcceleration;
    private float pointLoss;
    private float pointGain;
    private int bobberHeight;

    public FishBehavior(int idleTime, float topSpeed, float upAcceleration, float downAcceleration, int avgDistance, int moveVariation, String fishTexture, float gravity, float bobberUpAcceleration, float pointLoss, float pointGain, int bobberHeight) {
        this.idleTime = idleTime;
        this.topSpeed = topSpeed;
        this.upAcceleration = upAcceleration;
        this.downAcceleration = downAcceleration;
        this.avgDistance = avgDistance;
        this.moveVariation = moveVariation;
        this.fishTexture = fishTexture;
        this.gravity = gravity;
        this.bobberUpAcceleration = bobberUpAcceleration;
        this.pointLoss = pointLoss;
        this.pointGain = pointGain;
        this.bobberHeight = bobberHeight;
    }

    public FishBehavior(int idleTime, float topSpeed, float upAcceleration, float downAcceleration, int avgDistance, int moveVariation, String fishTexture) {
        this(idleTime,topSpeed, upAcceleration, downAcceleration, avgDistance, moveVariation, fishTexture, -0.7F, 0.7F, 1.0F, 1.0F, 0);
    }

    public static final Codec<FishBehavior> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.INT.fieldOf("idle_time").forGetter(FishBehavior::getIdleTime),
            Codec.FLOAT.fieldOf("top_speed").forGetter(FishBehavior::getTopSpeed),
            Codec.FLOAT.fieldOf("up_acceleration").forGetter(FishBehavior::getUpAcceleration),
            Codec.FLOAT.fieldOf("down_acceleration").forGetter(FishBehavior::getDownAcceleration),
            Codec.INT.fieldOf("avg_distance").forGetter(FishBehavior::getAvgDistance),
            Codec.INT.fieldOf("move_variation").forGetter(FishBehavior::getMoveVariation),
            Codec.STRING.fieldOf("fish_texture").forGetter(FishBehavior::getFishTexture)
    ).apply(inst, FishBehavior::new));

    public static final int MAX_HEIGHT = 127;

    public FishBehavior(FriendlyByteBuf buf) {
        this(buf.readVarInt(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readVarInt(), buf.readVarInt(), buf.readUtf(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat(),  buf.readVarInt());
    }
//    public FishBehavior(FriendlyByteBuf buf) {
//        this(buf.readVarInt(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readVarInt(), buf.readVarInt(), buf.readUtf());
//    }

    public void writeToBuffer(FriendlyByteBuf buf) {
        buf.writeVarInt(getIdleTime());
        buf.writeFloat(getTopSpeed());
        buf.writeFloat(getUpAcceleration());
        buf.writeFloat(getDownAcceleration());
        buf.writeVarInt(getAvgDistance());
        buf.writeVarInt(getMoveVariation());
        buf.writeUtf(getFishTexture());
        buf.writeFloat(getGravity());
        buf.writeFloat(getBobberUpAcceleration());
        buf.writeFloat(getPointLoss());
        buf.writeFloat(getPointGain());
        buf.writeVarInt(getBobberHeight());
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
        return this.idleTime;
    }
    public void setIdleTime(int idleTime) {
        this.idleTime = idleTime;
    }
    public float getDownAcceleration() {
        return this.downAcceleration;
    }
    public void setDownAcceleration(float downAcceleration) {
        this.downAcceleration = downAcceleration;
    }
    public float getTopSpeed() {
        return this.topSpeed;
    }
    public void setTopSpeed(float topSpeed) {
        this.topSpeed = topSpeed;
    }
    public float getUpAcceleration() {
        return this.upAcceleration;
    }
    public void setUpAcceleration(float upAcceleration) {
        this.upAcceleration = upAcceleration;
    }
    public int getAvgDistance() {
        return this.avgDistance;
    }
    public void setAvgDistance(int avgDistance) {
        this.avgDistance = avgDistance;
    }
    public int getMoveVariation() {
        return this.moveVariation;
    }
    public void setMoveVariation(int moveVariation) {
        this.moveVariation = moveVariation;
    }
    public String getFishTexture() {
        return this.fishTexture;
    }
    public void setFishTexture(String fishTexture) {
        this.fishTexture = fishTexture;
    }
    public float getGravity() {
        return this.gravity;
    }
    public void setGravity(float gravity) {
        this.gravity = gravity;
    }
    public float getBobberUpAcceleration() {
        return this.bobberUpAcceleration;
    }
    public void setBobberUpAcceleration(float bobberUpAcceleration) {
        this.bobberUpAcceleration = bobberUpAcceleration;
    }
    public float getPointLoss() {
        return this.pointLoss;
    }
    public void setPointLoss(float pointLoss) {
        this.pointLoss = pointLoss;
    }
    public float getPointGain() {
        return this.pointGain;
    }
    public void setPointGain(float pointGain) {
        this.pointGain = pointGain;
    }
    public int getBobberHeight() {
        return this.bobberHeight;
    }
    public void setBobberHeight(int bobberHeight) {
        this.bobberHeight = bobberHeight;
    }
}
