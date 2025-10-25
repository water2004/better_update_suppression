package org.edtp.better_update_suppression;

import net.fabricmc.api.ModInitializer;

public class Better_update_suppression implements ModInitializer {

    @Override
    public void onInitialize() {
        UpdateSuppressionBlockFinal.init();
    }
}
