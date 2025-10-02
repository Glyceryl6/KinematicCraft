// SpeedChangeEvent.java - 速度变化事件
package com.glyceryl6.kinematic.component.speed;

import com.glyceryl6.kinematic.component.speed.SpeedComponent;
import com.glyceryl6.kinematic.component.speed.modifier.SpeedModifier;
import com.glyceryl6.kinematic.core.event.ComponentEvent;

/**
 * 速度变化事件
 */
public class SpeedChangeEvent extends ComponentEvent {

    public enum Type {
        MODIFIER_ADDED,
        MODIFIER_REMOVED,
        MODIFIER_EXPIRED,
        BASE_SPEED_CHANGED,
        RECALCULATION
    }

    private final float newSpeed;
    private final float baseSpeed;
    private final SpeedModifier affectedModifier;
    private final Type changeType;

    public SpeedChangeEvent(
            SpeedComponent source, float newSpeed, float baseSpeed,
            SpeedModifier affectedModifier, Type changeType) {
        super("speed_change", source);
        this.newSpeed = newSpeed;
        this.baseSpeed = baseSpeed;
        this.affectedModifier = affectedModifier;
        this.changeType = changeType;
    }

    public float getNewSpeed() {
        return this.newSpeed;
    }

    public float getBaseSpeed() {
        return this.baseSpeed;
    }

    public SpeedModifier getAffectedModifier() {
        return this.affectedModifier;
    }

    public Type getChangeType() {
        return this.changeType;
    }

    public float getSpeedMultiplier() {
        return this.baseSpeed > 0 ? this.newSpeed / this.baseSpeed : 1.0f;
    }

}