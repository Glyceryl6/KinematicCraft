// EventBus.java - 事件总线接口
package com.glyceryl6.kinematic.core.architecture;

import com.glyceryl6.kinematic.core.event.ComponentEvent;
import com.glyceryl6.kinematic.core.event.EventListener;

public interface EventBus {

    void registerComponent(Component component);

    void unregisterComponent(Component component);

    void post(ComponentEvent event);

    void subscribe(String eventType, EventListener listener);

    void unsubscribe(String eventType, EventListener listener);

    int getListenerCount(String eventType);

}