package com.bonker.stardewfishing.common;

import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.server.FishBehaviorReloadListener;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class CommonEvents {
    @Mod.EventBusSubscriber(modid = StardewFishing.MODID)
    public static class ForgeBus {
        @SubscribeEvent
        public static void onAddReloadListeners(final AddReloadListenerEvent event) {
            event.addListener(FishBehaviorReloadListener.create());
        }

        @SubscribeEvent
        public static void onEntityJoinLevel(final EntityJoinLevelEvent event) {
            if (!event.getLevel().isClientSide && event.getEntity().getType() == EntityType.FISHING_BOBBER) {
                FishingHook hook = (FishingHook) event.getEntity();
                event.setCanceled(true);
                event.getLevel().addFreshEntity(new StardewFishingHook(hook.getPlayerOwner(), event.getLevel(), hook.luck, hook.lureSpeed));
            }
        }
    }

    @Mod.EventBusSubscriber(modid = StardewFishing.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModBus {
        @SubscribeEvent
        public static void onCommonSetup(final FMLCommonSetupEvent event) {
            event.enqueueWork(() ->
                    StardewFishing.QUALITY_FOOD_INSTALLED = ModList.get().isLoaded("quality_food")
            );
        }
    }
}