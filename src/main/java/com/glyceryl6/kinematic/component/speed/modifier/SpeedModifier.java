package com.glyceryl6.kinematic.component.speed.modifier;

import net.minecraft.world.entity.Mob;

import java.util.UUID;

/**
 * 速度修饰符基类，支持不同类型的速度调整
 */
public abstract class SpeedModifier {
    
    protected final UUID modifierId;
    protected final String name;
    protected final ModifierType type;
    protected final int priority;
    protected final float value;
    protected long expirationTime = -1; // -1 表示永久
    protected boolean active = true;

    public SpeedModifier(String name, ModifierType type, float value, int priority) {
        this.modifierId = UUID.randomUUID();
        this.name = name;
        this.type = type;
        this.value = value;
        this.priority = priority;
    }

    public abstract float apply(float baseSpeed, float currentSpeed, Mob mob);

    public boolean isExpired() {
        return this.expirationTime > 0 && System.currentTimeMillis() > this.expirationTime;
    }

    public void setDuration(long durationMs) {
        this.expirationTime = System.currentTimeMillis() + durationMs;
    }

    public UUID getModifierId() { 
        return this.modifierId; 
    }
    
    public String getName() { 
        return this.name; 
    }
    
    public ModifierType getType() { 
        return this.type; 
    }
    
    public int getPriority() { 
        return this.priority; 
    }
    
    public float getValue() { 
        return this.value; 
    }
    
    public boolean isActive() { 
        return this.active && !this.isExpired();
    }
    
    public void setActive(boolean active) { 
        this.active = active; 
    }
    
}