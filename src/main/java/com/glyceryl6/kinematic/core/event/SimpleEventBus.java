package com.glyceryl6.kinematic.core.event;

import com.glyceryl6.kinematic.core.architecture.Component;
import com.glyceryl6.kinematic.core.architecture.EventBus;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 简单事件总线实现，支持同步和异步事件处理
 */
public class SimpleEventBus implements EventBus {

    private final Map<String, List<EventListener>> listeners = new ConcurrentHashMap<>();
    private final Map<Class<? extends Component>, Component> componentListeners = new ConcurrentHashMap<>();
    private final List<ComponentEvent> eventQueue = new CopyOnWriteArrayList<>();
    private boolean asyncProcessing = false;

    @Override
    public void registerComponent(Component component) {
        componentListeners.put(component.getClass(), component);
    }

    @Override
    public void unregisterComponent(Component component) {
        componentListeners.remove(component.getClass());
    }

    @Override
    public void post(ComponentEvent event) {
        if (asyncProcessing) {
            eventQueue.add(event);
        } else {
            processEvent(event);
        }
    }

    @Override
    public void subscribe(String eventType, EventListener listener) {
        listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(listener);
    }

    @Override
    public void unsubscribe(String eventType, EventListener listener) {
        List<EventListener> eventListeners = listeners.get(eventType);
        if (eventListeners != null) {
            eventListeners.remove(listener);
        }
    }

    @Override
    public int getListenerCount(String eventType) {
        List<EventListener> eventListeners = listeners.get(eventType);
        return eventListeners != null ? eventListeners.size() : 0;
    }

    /**
     * 处理事件队列
     */
    public void processEventQueue() {
        for (ComponentEvent event : eventQueue) {
            processEvent(event);
        }

        eventQueue.clear();
    }

    /**
     * 设置异步处理模式
     */
    public void setAsyncProcessing(boolean async) {
        this.asyncProcessing = async;
    }

    private void processEvent(ComponentEvent event) {
        String eventType = event.getEventType();
        // 通知特定事件类型的监听器
        List<EventListener> eventListeners = listeners.get(eventType);
        if (eventListeners != null) {
            for (EventListener listener : eventListeners) {
                if (!event.isCancelled()) {
                    listener.onEvent(event);
                }
            }
        }

        // 通知组件监听器
        for (Component component : componentListeners.values()) {
            if (!event.isCancelled() && component.isEnabled()) {
                component.onEvent(event);
            }
        }
    }

    public void clear() {
        listeners.clear();
        componentListeners.clear();
        eventQueue.clear();
    }

}