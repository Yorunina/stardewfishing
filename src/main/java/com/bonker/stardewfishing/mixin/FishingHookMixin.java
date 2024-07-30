package com.bonker.stardewfishing.mixin;

import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.common.FishingHookLogic;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@SuppressWarnings("UnreachableCode")
@Mixin(value = FishingHook.class)
public abstract class FishingHookMixin {
    @Inject(method = "catchingFish", at = @At(value = "HEAD"), cancellable = true)
    private void cancel_catchingFish(BlockPos pPos, CallbackInfo ci) {
        FishingHook hook = (FishingHook) (Object) this;

        if (FishingHookLogic.getStoredRewards(hook).isEmpty()) {
            ci.cancel();
        }
    }

    @Inject(method = "retrieve",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraftforge/eventbus/api/IEventBus;post(Lnet/minecraftforge/eventbus/api/Event;)Z")
    )
    public void retrieve(ItemStack pStack, CallbackInfoReturnable<Integer> cir, @Local List<ItemStack> items) {
        FishingHook hook = (FishingHook) (Object) this;
        ServerPlayer player = (ServerPlayer) hook.getPlayerOwner();
        if (player == null) return;

        if (items.stream().anyMatch(stack -> stack.is(StardewFishing.STARTS_MINIGAME))) {
            CriteriaTriggers.FISHING_ROD_HOOKED.trigger(player, pStack, hook, items);
            FishingHookLogic.getStoredRewards(hook).ifPresent(rewards -> rewards.addAll(items));
            FishingHookLogic.startMinigame(player);
            cir.cancel();
        } else {
            FishingHookLogic.modifyRewards(items, 0);
            player.getLevel().playSound(null, player, StardewFishing.PULL_ITEM.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        }
    }
}
