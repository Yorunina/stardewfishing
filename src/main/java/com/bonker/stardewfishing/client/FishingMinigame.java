package com.bonker.stardewfishing.client;

import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.common.FishBehavior;
import net.minecraft.util.Mth;

import java.util.Random;

public class FishingMinigame {
    public static final int POINTS_TO_FINISH = 120;

    private static final float UP_ACCELERATION = 0.7F;
    private static final float GRAVITY = -0.7F;
    private static final int MAX_BOBBER_HEIGHT = 106;
    private static final int MAX_FISH_HEIGHT = FishBehavior.MAX_HEIGHT;

    private final Random random = new Random();
    private final FishingScreen screen;
    private final FishBehavior behavior;

    private double bobberPos = 0;
    private double bobberVelocity = 0;

    private double fishPos = 0;
    private double fishVelocity = 0;
    private int fishTarget = -1;
    private boolean fishIsIdle = false;
    private int fishIdleTicks = 0;

    private boolean bobberOnFish = true;
    private float points = POINTS_TO_FINISH / 5;
    private int successTicks = 0;
    private int totalTicks = 0;

    public FishingMinigame(FishingScreen screen, FishBehavior behavior) {
        this.screen = screen;
        this.behavior = behavior;
    }

    public void tick(boolean mouseDown) {
        // bobber movement
        if (mouseDown) {
            if (bobberVelocity < 0) {
                bobberVelocity *= 0.9;
            }
            bobberVelocity += UP_ACCELERATION;
        } else if (bobberPos > 0) {
            bobberVelocity += GRAVITY;
        }

        bobberPos += bobberVelocity;
        if (bobberPos > MAX_BOBBER_HEIGHT) {
            bobberVelocity = 0;
            bobberPos = MAX_BOBBER_HEIGHT;
        } else if (bobberPos <= 0) {
            bobberPos = 0;
            if (bobberVelocity < 2 * GRAVITY) {
                bobberVelocity *= -0.4;
            } else {
                bobberVelocity = 0;
            }
        }

        // fish movement
        if (fishTarget == -1 || behavior.shouldMoveNow(fishIdleTicks, random)) {
            fishTarget = behavior.pickNextTargetPos((int) fishPos, random);
            fishIsIdle = false;
            fishIdleTicks = 0;
        }

        if (fishIsIdle) {
            fishIdleTicks++;
            if (Math.abs(fishVelocity) > 0) {
                boolean up = fishVelocity > 0;
                fishVelocity -= (up ? behavior.getUpAcceleration() : behavior.getDownAcceleration()) * Math.signum(fishVelocity);
                if (fishVelocity == 0 || up && fishVelocity < 0 || !up && fishVelocity > 0) {
                    fishVelocity = 0;
                }
            }
        } else {
            double distanceLeft = fishTarget - fishPos;
            double acceleration = (distanceLeft > 0 ? behavior.getUpAcceleration() : behavior.getDownAcceleration()) * Math.signum(distanceLeft);
            fishVelocity = Mth.clamp(fishVelocity + acceleration, -behavior.getTopSpeed(), behavior.getTopSpeed());
        }

        fishPos += fishVelocity;
        if (Math.abs(fishTarget - fishPos) < fishVelocity) {
            fishIsIdle = true;
        } else if (fishPos > MAX_FISH_HEIGHT) {
            fishVelocity = 0;
            fishPos = MAX_FISH_HEIGHT;
            fishIsIdle = true;
        } else if (fishPos < 0) {
            fishVelocity = 0;
            fishPos = 0;
            fishIsIdle = true;
        }

        // game logic
        int min = Mth.floor(bobberPos) - 2;
        int max = Mth.ceil(bobberPos) + 24;
        boolean wasOnFish = bobberOnFish;
        bobberOnFish = fishPos >= min && fishPos <= max;

        totalTicks++;
        if (bobberOnFish) {
            successTicks++;
        }

        if (wasOnFish != bobberOnFish) {
            screen.stopReelingSounds();
            screen.playSound(StardewFishing.DWOP.get());
            screen.reelSoundTimer = 1;
        }

        if (bobberOnFish) {
            points += behavior.getPointGain();
            if (points >= POINTS_TO_FINISH) {
                screen.setResult(true, (double) successTicks / totalTicks);
            }
        } else {
            points -= behavior.getPointLoss();
            if (points <= 0) {
                screen.setResult(false, 0);
            }
        }
    }

    public float getBobberPos() {
        return (float) bobberPos;
    }

    public float getFishPos() {
        return (float) fishPos;
    }

    public boolean isBobberOnFish() {
        return bobberOnFish;
    }

    public float getProgress() {
        return (float) points / POINTS_TO_FINISH;
    }
}
