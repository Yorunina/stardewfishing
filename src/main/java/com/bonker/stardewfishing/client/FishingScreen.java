package com.bonker.stardewfishing.client;

import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.client.util.Animation;
import com.bonker.stardewfishing.client.util.RenderUtil;
import com.bonker.stardewfishing.client.util.Shake;
import com.bonker.stardewfishing.common.FishBehavior;
import com.bonker.stardewfishing.common.networking.C2SCompleteMinigamePacket;
import com.bonker.stardewfishing.common.networking.SFNetworking;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

public class FishingScreen extends Screen {
    private static final Component TITLE = Component.literal("Fishing Minigame");
    private static final ResourceLocation TEXTURE = new ResourceLocation(StardewFishing.MODID, "textures/minigame.png");

    private static final int GUI_WIDTH = 38;
    private static final int GUI_HEIGHT = 152;
    private static final int HIT_WIDTH = 73;
    private static final int HIT_HEIGHT = 29;
    private static final int PERFECT_WIDTH = 41;
    private static final int PERFECT_HEIGHT = 12;

    private static final float ALPHA_PER_TICK = 1F / 10;
    private static final float HANDLE_ROT_FAST = Mth.PI / 3;
    private static final float HANDLE_ROT_SLOW = Mth.PI / -7F;

    private static final int REEL_FAST_LENGTH = 30;
    private static final int REEL_SLOW_LENGTH = 20;
    private static final int CREAK_LENGTH = 6;

    private int leftPos, topPos;
    private final FishingMinigame minigame;
    public Status status = Status.HIT_TEXT;
    public double accuracy = -1;
    private boolean mouseDown = false;
    private int animationTimer = 0;

    private final Animation textSize = new Animation(0);
    private final Animation progressBar;
    private final Animation bobberPos = new Animation(0);
    private final Animation bobberAlpha = new Animation(1);
    private final Animation fishPos = new Animation(0);
    private final Animation handleRot = new Animation(0);

    private final Shake shake = new Shake(0.75F, 1);

    public int reelSoundTimer = -1;
    private int creakSoundTimer = 0;
    private ResourceLocation fishTexture;
    private FishBehavior fishBehavior;

    public FishingScreen(FishBehavior behavior) {
        super(TITLE);
        this.minigame = new FishingMinigame(this, behavior);
        this.progressBar = new Animation(minigame.getProgress());
        this.fishTexture = new ResourceLocation(behavior.getFishTexture());
        this.fishBehavior = behavior;
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        final float partialTick = minecraft.getFrameTime();

        PoseStack poseStack = pGuiGraphics.pose();

        if (!isPauseScreen()) {
            // render HIT!
            float scale = textSize.getInterpolated(partialTick) * 1.5F;
            float x = (width - HIT_WIDTH * scale) / 2;
            float y = (height - HIT_HEIGHT * scale) / 3;

            poseStack.pushPose();
            poseStack.scale(scale, scale, 1);
            RenderUtil.blitF(pGuiGraphics, TEXTURE, x * (1 / scale), y * (1 / scale), 71, 0, HIT_WIDTH, HIT_HEIGHT);
            poseStack.popPose();
        } else {
            // darken screen
            renderBackground(pGuiGraphics);

            RenderUtil.drawWithShake(poseStack, shake, partialTick, status == Status.SUCCESS || status == Status.FAILURE, () -> {
                RenderUtil.drawWithBlend(() -> {
                    // draw fishing gui
                    pGuiGraphics.blit(TEXTURE, leftPos, topPos, 0, 0, GUI_WIDTH, GUI_HEIGHT);

                    // draw bobber
                    RenderUtil.drawWithAlpha(bobberAlpha.getInterpolated(partialTick), () -> {
                        float bobberY = 4 - 12 + (142 - bobberPos.getInterpolated(partialTick)) - fishBehavior.getBobberHeight();
                        RenderUtil.blitF(pGuiGraphics, TEXTURE, leftPos + 18, topPos + bobberY,
                                38, 0, 9, 36, 256F, 256F,
                                leftPos + 27, topPos + bobberY + fishBehavior.getBobberHeight() + 12);
                    });
                });

                RenderUtil.drawWithShake(poseStack, shake, partialTick, minigame.isBobberOnFish() && status == Status.MINIGAME, () -> {
                    // draw fish
                    float fishY = 4 - 16 + (142 - fishPos.getInterpolated(partialTick));
                    RenderUtil.blitF(pGuiGraphics, this.fishTexture, leftPos + 14, topPos + fishY, 0, 0, 16, 16, 16, 16);
                });

                // draw progress bar
                float progress = progressBar.getInterpolated(partialTick);
                int color = Mth.hsvToRgb(progress / 3.0F, 1.0F, 1.0F) | 0xFF000000;
                RenderUtil.fillF(pGuiGraphics, leftPos + 33, topPos + 148, leftPos + 37, topPos + 148 - progress * 145, 0, color);

                // draw handle
                RenderUtil.drawRotatedAround(poseStack, handleRot.getInterpolated(partialTick), leftPos + 6.5F, topPos + 130.5F, () -> {
                    pGuiGraphics.blit(TEXTURE, leftPos + 5, topPos + 129, 47, 0, 8, 3);
                });

                // render PERFECT!
                if (status == Status.SUCCESS && accuracy == 1) {
                    float scale = textSize.getInterpolated(partialTick);
                    float x = leftPos + 2 + (PERFECT_WIDTH - PERFECT_WIDTH * scale) / 2;
                    float y = topPos - PERFECT_HEIGHT * scale;

                    poseStack.pushPose();
                    poseStack.scale(scale, scale, 1);
                    RenderUtil.blitF(pGuiGraphics, TEXTURE, x * (1 / scale), y * (1 / scale), 144, 0, PERFECT_WIDTH, PERFECT_HEIGHT);
                    poseStack.popPose();
                }
            });
        }
    }

    @Override
    protected void init() {
        leftPos = (width - GUI_WIDTH) / 2;
        topPos = (height - GUI_HEIGHT) / 2;
    }

    @Override
    public void tick() {
        shake.tick();

        switch (status) {
            case HIT_TEXT -> {
                if (animationTimer < 20) {
                    if (++animationTimer == 20) {
                        status = Status.MINIGAME;
                    } else if (animationTimer <= 5) {
                        textSize.addValue(0.2F);
                    } else if (animationTimer <= 15) {
                        textSize.addValue(-0.013F);
                    } else {
                        textSize.addValue(-0.16F);
                    }
                }
            }
            case MINIGAME -> {
                minigame.tick(mouseDown);

                boolean onFish = minigame.isBobberOnFish();

                progressBar.setValue(minigame.getProgress());
                bobberPos.setValue(minigame.getBobberPos());
                bobberAlpha.addValue(onFish ? ALPHA_PER_TICK : -ALPHA_PER_TICK, 0.4F, 1);
                fishPos.setValue(minigame.getFishPos());
                handleRot.addValue(onFish ? HANDLE_ROT_FAST : HANDLE_ROT_SLOW);

                if (reelSoundTimer == -1 || --reelSoundTimer == 0) {
                    reelSoundTimer = onFish ? REEL_FAST_LENGTH : REEL_SLOW_LENGTH;
                    playSound(onFish ? StardewFishing.REEL_FAST.get() : StardewFishing.REEL_SLOW.get());
                }

                if (creakSoundTimer > 0) {
                    creakSoundTimer--;
                }
                if (mouseDown && creakSoundTimer == 0) {
                    creakSoundTimer = CREAK_LENGTH;
                    playSound(StardewFishing.REEL_CREAK.get());
                }
            }
            case SUCCESS, FAILURE -> {
                if (--animationTimer == 0) {
                    onClose();
                } else if (animationTimer >= 15) {
                    textSize.addValue(0.2F);
                } else if (animationTimer >= 5) {
                    textSize.addValue(-0.013F);
                } else {
                    textSize.addValue(-0.16F);
                }
            }
        }
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (pButton == GLFW.GLFW_MOUSE_BUTTON_1 || pButton == GLFW.GLFW_MOUSE_BUTTON_2) {
            if (!mouseDown) {
                playSound(StardewFishing.REEL_CREAK.get());
                mouseDown = true;
            }
            return true;
        } else {
            return super.mouseClicked(pMouseX, pMouseY, pButton);
        }
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        if (pButton == GLFW.GLFW_MOUSE_BUTTON_1 || pButton == GLFW.GLFW_MOUSE_BUTTON_2) {
            if (mouseDown) {
                mouseDown = false;
            }
            return true;
        } else {
            return super.mouseReleased(pMouseX, pMouseY, pButton);
        }
    }

    @Override
    public void onClose() {
        super.onClose();
        SFNetworking.sendToServer(new C2SCompleteMinigamePacket(status == Status.SUCCESS, accuracy));

        stopReelingSounds();
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return status == Status.MINIGAME;
    }

    @Override
    public boolean isPauseScreen() {
        return status != Status.HIT_TEXT;
    }

    public void setResult(boolean success, double accuracy) {
        status = success ? Status.SUCCESS : Status.FAILURE;
        this.accuracy = accuracy;
        animationTimer = 20;
        textSize.reset(0.0F);

        progressBar.freeze();
        bobberPos.freeze();
        bobberAlpha.freeze();
        fishPos.freeze();
        handleRot.freeze();

        playSound(success ? StardewFishing.COMPLETE.get() : StardewFishing.FISH_ESCAPE.get());
        shake.setValues(2.0F, 1);
    }

    public void playSound(SoundEvent soundEvent) {
        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(soundEvent, 1.0F));
    }

    public void stopReelingSounds() {
        minecraft.getSoundManager().stop(StardewFishing.REEL_FAST.getId(), null);
        minecraft.getSoundManager().stop(StardewFishing.REEL_SLOW.getId(), null);
    }

    public enum Status {
        HIT_TEXT, MINIGAME, SUCCESS, FAILURE
    }
}
