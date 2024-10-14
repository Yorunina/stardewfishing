package com.bonker.stardewfishing.compat.kubejs;

import com.bonker.stardewfishing.common.FishBehavior;
import dev.latvian.mods.kubejs.level.LevelEventJS;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public class MiniGameStartJS extends LevelEventJS {

    public final Level level;
    public final ServerPlayer player;
    public final FishBehavior fishBehavior;


    public MiniGameStartJS(ServerPlayer player, FishBehavior fishBehavior, Level level) {
        super();
        this.level = level;
        this.player = player;
        this.fishBehavior = fishBehavior;
    }

    @Override
    public Level getLevel() {return level;}
    public ServerPlayer getPlayer() {return player;}
    public FishBehavior getFishBehavior() {return fishBehavior;}

}
