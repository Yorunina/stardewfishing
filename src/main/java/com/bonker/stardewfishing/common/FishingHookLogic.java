package com.bonker.stardewfishing.common;

import com.bonker.stardewfishing.SFConfig;
import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.common.networking.S2CStartMinigamePacket;
import com.bonker.stardewfishing.common.networking.SFNetworking;
import com.bonker.stardewfishing.server.FishBehaviorReloadListener;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.ItemFishedEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FishingHookLogic {
    private final ArrayList<ItemStack> rewards = new ArrayList<>();

    public static void attachCap(AttachCapabilitiesEvent<Entity> event) {
        if (!event.getObject().getCapability(FishingHookLogic.CapProvider.CAP).isPresent()) {
            event.addCapability(CapProvider.NAME, new CapProvider());
        }
    }

    public static Optional<ArrayList<ItemStack>> getStoredRewards(FishingHook entity) {
        return entity.getCapability(CapProvider.CAP).map(cap -> cap.rewards);
    }

    public static void startMinigame(ServerPlayer player) {
        if (player.fishing == null) return;

        SFNetworking.sendToPlayer(player, new S2CStartMinigamePacket(
                FishBehaviorReloadListener.getBehavior(
                        getStoredRewards(player.fishing)
                                .flatMap(rewards -> rewards.stream()
                                        .filter(stack -> stack.is(StardewFishing.STARTS_MINIGAME))
                                        .findFirst()
                                ).orElse(null))
        ));
    }

    public static void endMinigame(Player player, boolean success, double accuracy) {
        if (success && !player.getLevel().isClientSide) {
            modifyRewards((ServerPlayer) player, accuracy);
            giveRewards((ServerPlayer) player, accuracy);
        }

        if (player.fishing != null) {
            player.fishing.discard();
        }
    }

    public static void modifyRewards(ServerPlayer player, double accuracy) {
        if (player.fishing == null) return;
        getStoredRewards(player.fishing).ifPresent(rewards -> modifyRewards(rewards, accuracy));
    }

    public static void modifyRewards(List<ItemStack> rewards, double accuracy) {
        if (StardewFishing.QUALITY_FOOD_INSTALLED) {
            int quality = SFConfig.getQuality(accuracy);
            for (ItemStack reward : rewards) {
                if (reward.is(StardewFishing.STARTS_MINIGAME)) {
                    if (quality == 0 && reward.hasTag() && reward.getOrCreateTag().contains("quality_food")) {
                        if (reward.getOrCreateTag().size() > 1) {
                            reward.getOrCreateTag().remove("quality_food");
                        } else {
                            reward.setTag(null);
                        }
                    } else if (quality > 0) {
                        CompoundTag qualityFood = new CompoundTag();
                        qualityFood.putInt("quality", quality);
                        reward.getOrCreateTag().put("quality_food", qualityFood);
                    }
                }
            }
        }
    }

    public static void giveRewards(ServerPlayer player, double accuracy) {
        if (player.fishing == null) return;

        InteractionHand hand = getRodHand(player);
        if (hand == null) return;

        FishingHook hook = player.fishing;
        getStoredRewards(hook).ifPresent(rewards -> {
            ItemFishedEvent event = new ItemFishedEvent(rewards, hook.isOnGround() ? 2 : 1, hook);
            MinecraftForge.EVENT_BUS.post(event);
            if (event.isCanceled()) {
                hook.discard();
                return;
            }

            player.getItemInHand(hand).hurtAndBreak(event.getRodDamage(), player, p -> p.broadcastBreakEvent(hand));

            ServerLevel level = player.getLevel();
            for (ItemStack reward : rewards) {
                if (reward.is(ItemTags.FISHES)) {
                    player.awardStat(Stats.FISH_CAUGHT);
                }

                ItemEntity itementity = new ItemEntity(level, hook.getX(), hook.getY(), hook.getZ(), reward);
                double dx = player.getX() - hook.getX();
                double dy = player.getY() - hook.getY();
                double dz = player.getZ() - hook.getZ();
                double scale = 0.1;
                itementity.setDeltaMovement(dx * scale, dy * scale + Math.sqrt(Math.sqrt(dx * dx + dy * dy + dz * dz)) * 0.08, dz * scale);
                level.addFreshEntity(itementity);

                int exp = Mth.floor((player.getRandom().nextInt(6) + 1) * SFConfig.getMultiplier(accuracy));
                level.addFreshEntity(new ExperienceOrb(level, player.getX(), player.getY() + 0.5, player.getZ() + 0.5, exp));
            }

            player.getLevel().playSound(null, player, StardewFishing.PULL_ITEM.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        });
    }

    public static InteractionHand getRodHand(Player player) {
        boolean mainHand = player.getItemInHand(InteractionHand.MAIN_HAND).canPerformAction(ToolActions.FISHING_ROD_CAST);
        if (mainHand) return InteractionHand.MAIN_HAND;

        boolean offHand = player.getItemInHand(InteractionHand.OFF_HAND).canPerformAction(ToolActions.FISHING_ROD_CAST);
        if (offHand) return InteractionHand.OFF_HAND;

        return null;
    }

    private static class CapProvider implements ICapabilityProvider {
        private static final Capability<FishingHookLogic> CAP = CapabilityManager.get(new CapabilityToken<>() {});
        private static final ResourceLocation NAME = new ResourceLocation(StardewFishing.MODID, "hook");

        private final LazyOptional<FishingHookLogic> optional = LazyOptional.of(FishingHookLogic::new);

        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
            return getCapability(cap);
        }

        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap) {
            return cap == CAP ? optional.cast() : LazyOptional.empty();
        }
    }
}
