// ConditionalSpeedModifier.java - 条件修饰符
package com.glyceryl6.kinematic.component.speed.modifier;

import net.minecraft.world.entity.Mob;
import java.util.function.Predicate;

public class ConditionalSpeedModifier extends SpeedModifier {

    private final Predicate<Mob> condition;
    private final SpeedModifier baseModifier;

    public ConditionalSpeedModifier(String name, SpeedModifier baseModifier, Predicate<Mob> condition) {
        super(name, ModifierType.COMPOUND, baseModifier.getValue(), baseModifier.getPriority());
        this.baseModifier = baseModifier;
        this.condition = condition;
    }

    @Override
    public float apply(float baseSpeed, float currentSpeed, Mob mob) {
        if (this.condition.test(mob)) {
            return this.baseModifier.apply(baseSpeed, currentSpeed, mob);
        }

        return currentSpeed;
    }

    @Override
    public boolean isActive() {
        return super.isActive() && this.baseModifier.isActive();
    }

}