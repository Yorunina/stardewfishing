package com.bonker.stardewfishing.compat.kubejs;

import com.bonker.stardewfishing.common.FishingHookLogic;
import dev.latvian.mods.kubejs.level.LevelEventJS;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Optional;

public class MiniGameEndJS extends LevelEventJS {

    public final Level level;
    public final ServerPlayer player;
    public final boolean fishSuccess;
    public final double accuracy;

    public MiniGameEndJS(ServerPlayer player, boolean success, double accuracy, Level level) {
        super();
        this.level = level;
        this.player = player;
        this.fishSuccess = success;
        this.accuracy = accuracy;
    }

    @Override
    public Level getLevel() {return level;}
    public ServerPlayer getPlayer() {return player;}
    public boolean getFishSuccess() {return fishSuccess;}
    public double getAccuracy() {return accuracy;}
    public Optional<ArrayList<ItemStack>> getStoredRewards() {
        if (player.fishing == null) return Optional.empty();
        return FishingHookLogic.getStoredRewards(player.fishing);
    }
    public void setStoreRewards(ArrayList<ItemStack> itemStacks) {
        if (player.fishing == null) return;
        FishingHookLogic.setStoreRewards(player.fishing, itemStacks);
    }
}
