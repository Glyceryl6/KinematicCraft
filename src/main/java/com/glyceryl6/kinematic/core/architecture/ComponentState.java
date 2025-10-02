// ComponentState.java - 组件状态
package com.glyceryl6.kinematic.core.architecture;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ComponentState {

    private final Map<String, Object> stateData = new ConcurrentHashMap<>();

    public void put(String key, Object value) {
        stateData.put(key, value);
    }

    public <T> T get(String key, Class<T> type) {
        Object value = stateData.get(key);
        return type.isInstance(value) ? type.cast(value) : null;
    }

    public Map<String, Object> getAll() {
        return new ConcurrentHashMap<>(stateData);
    }

}