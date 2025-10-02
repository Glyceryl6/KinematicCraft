// ContainerDebugInfo.java - 容器调试信息
package com.glyceryl6.kinematic.core.container;

import com.glyceryl6.kinematic.core.architecture.ComponentType;

import java.util.Set;
import java.util.UUID;

public record ContainerDebugInfo(UUID mobId, Set<ComponentType> componentTypes, int updateCount, boolean active) {

    @Override
    public String toString() {
        return String.format("MobContainer[mob=%s, components=%s, updating=%d, active=%s]", mobId, componentTypes, updateCount, active);
    }

}