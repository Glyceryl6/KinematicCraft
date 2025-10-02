package com.glyceryl6.kinematic.core.event;// EventListener.java - 事件监听器接口

@FunctionalInterface
public interface EventListener {

    void onEvent(ComponentEvent event);

}