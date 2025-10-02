// AdditiveSpeedModifier.java - 加法修饰符
package com.glyceryl6.kinematic.component.speed.modifier;

import net.minecraft.world.entity.Mob;

public class AdditiveSpeedModifier extends SpeedModifier {

    public AdditiveSpeedModifier(String name, float value, int priority) {
        super(name, ModifierType.ADDITIVE, value, priority);
    }

    @Override
    public float apply(float baseSpeed, float currentSpeed, Mob mob) {
        return currentSpeed + this.value;
    }

}