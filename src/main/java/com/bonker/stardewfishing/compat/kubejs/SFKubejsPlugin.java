package com.bonker.stardewfishing.compat.kubejs;

import dev.latvian.mods.kubejs.KubeJSPlugin;

public class SFKubejsPlugin extends KubeJSPlugin {
    @Override
    public void registerEvents() {
        SFEvents.SFGROUP.register();
    }
}
