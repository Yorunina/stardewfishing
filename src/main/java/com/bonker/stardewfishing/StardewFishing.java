package com.bonker.stardewfishing;

import com.bonker.stardewfishing.common.networking.SFNetworking;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(StardewFishing.MODID)
public class StardewFishing {
    /*
        Constants
     */

    public static final String MODID = "stardew_fishing";

    public static boolean QUALITY_FOOD_INSTALLED = false;

    public static final TagKey<Item> STARTS_MINIGAME = TagKey.create(ForgeRegistries.ITEMS.getRegistryKey(), new ResourceLocation(MODID, "starts_minigame"));

    /*
        Sound Events
     */

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MODID);

    public static final RegistryObject<SoundEvent> CAST = registerSound("cast");
    public static final RegistryObject<SoundEvent> COMPLETE = registerSound("complete");
    public static final RegistryObject<SoundEvent> DWOP = registerSound("dwop");
    public static final RegistryObject<SoundEvent> FISH_ESCAPE = registerSound("fish_escape");
    public static final RegistryObject<SoundEvent> FISH_BITE = registerSound("fish_bite");
    public static final RegistryObject<SoundEvent> FISH_HIT = registerSound("fish_hit");
    public static final RegistryObject<SoundEvent> PULL_ITEM = registerSound("pull_item");
    public static final RegistryObject<SoundEvent> REEL_CREAK = registerSound("reel_creak");
    public static final RegistryObject<SoundEvent> REEL_FAST = registerSound("reel_fast");
    public static final RegistryObject<SoundEvent> REEL_SLOW = registerSound("reel_slow");

    public StardewFishing() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        SOUND_EVENTS.register(bus);

        SFNetworking.register();

        MinecraftForge.EVENT_BUS.register(this);

        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, SFConfig.SERVER_SPEC);
    }

    private static RegistryObject<SoundEvent> registerSound(String name) {
        return SOUND_EVENTS.register(name, () -> new SoundEvent(new ResourceLocation(MODID, name)));
    }
}
