// ComponentEvent.java - 组件事件基类
package com.glyceryl6.kinematic.core.event;

import com.glyceryl6.kinematic.core.architecture.Component;

public abstract class ComponentEvent {

    private final String eventType;
    private final Component source;
    private boolean cancelled = false;
    private final long timestamp;

    public ComponentEvent(String eventType, Component source) {
        this.eventType = eventType;
        this.source = source;
        this.timestamp = System.currentTimeMillis();
    }

    public String getEventType() {
        return eventType;
    }

    public Component getSource() {
        return source;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public long getTimestamp() {
        return timestamp;
    }

}