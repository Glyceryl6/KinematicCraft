package com.glyceryl6.kinematic;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.NeoForge;

@Mod(KinematicCraft.MOD_ID)
public class KinematicCraft {

    public static final String MOD_ID = "kinematic_craft";

    public KinematicCraft(IEventBus modEventBus, ModContainer modContainer) {
        NeoForge.EVENT_BUS.register(this);
    }

}