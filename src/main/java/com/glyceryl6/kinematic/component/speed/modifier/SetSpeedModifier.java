// SetSpeedModifier.java - 设置修饰符
package com.glyceryl6.kinematic.component.speed.modifier;

import net.minecraft.world.entity.Mob;

public class SetSpeedModifier extends SpeedModifier {

    public SetSpeedModifier(String name, float value, int priority) {
        super(name, ModifierType.SET, value, priority);
    }

    @Override
    public float apply(float baseSpeed, float currentSpeed, Mob mob) {
        return this.value;
    }

}