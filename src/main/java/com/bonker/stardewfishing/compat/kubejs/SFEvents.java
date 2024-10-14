package com.bonker.stardewfishing.compat.kubejs;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;

public interface SFEvents {
    EventGroup SFGROUP = EventGroup.of("StardewFishing");
    EventHandler MINIGAME_START = SFGROUP
            .server("miniGameStart", () -> MiniGameStartJS.class);
    EventHandler MINIGAME_END = SFGROUP
            .server("miniGameEnd", () -> MiniGameEndJS.class);

}
