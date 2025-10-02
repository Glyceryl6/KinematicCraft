// MultiplicativeSpeedModifier.java - 乘法修饰符
package com.glyceryl6.kinematic.component.speed.modifier;

import net.minecraft.world.entity.Mob;

public class MultiplicativeSpeedModifier extends SpeedModifier {

    public MultiplicativeSpeedModifier(String name, float value, int priority) {
        super(name, ModifierType.MULTIPLICATIVE, value, priority);
    }

    @Override
    public float apply(float baseSpeed, float currentSpeed, Mob mob) {
        return currentSpeed * this.value;
    }

}