// ComponentContainer.java - 组件容器接口
package com.glyceryl6.kinematic.core.container;

import com.glyceryl6.kinematic.core.architecture.Component;
import com.glyceryl6.kinematic.core.architecture.ComponentType;

import java.util.Set;

public interface ComponentContainer {

    <T extends Component> T getComponent(Class<T> componentClass);

    <T extends Component> T getComponent(ComponentType type);

    void addComponent(Component component);

    void removeComponent(Class<? extends Component> componentClass);

    void removeComponent(ComponentType type);

    void updateComponents(float deltaTime);

    boolean hasComponent(Class<? extends Component> componentClass);

    boolean hasComponent(ComponentType type);

    Set<ComponentType> getComponentTypes();

    int getComponentCount();

    void shutdown();

}