// ComponentLifecycleEvent.java - 组件生命周期事件
package com.glyceryl6.kinematic.core.event;

import com.glyceryl6.kinematic.core.architecture.Component;

public class ComponentLifecycleEvent extends ComponentEvent {

    private final Type lifecycleType;

    public ComponentLifecycleEvent(Type lifecycleType, Component source) {
        super("component_lifecycle", source);
        this.lifecycleType = lifecycleType;
    }

    public Type getLifecycleType() {
        return lifecycleType;
    }

    public enum Type {
        COMPONENT_ADDED,
        COMPONENT_REMOVED,
        COMPONENT_HOT_SWAPPED,
        COMPONENT_ENABLED,
        COMPONENT_DISABLED
    }

}