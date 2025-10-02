// MobComponentContainer.java - 组件容器实现
package com.glyceryl6.kinematic.core.container;

import com.glyceryl6.kinematic.core.architecture.*;
import com.glyceryl6.kinematic.core.dependency.DependencyInjector;
import com.glyceryl6.kinematic.core.event.ComponentLifecycleEvent;
import com.glyceryl6.kinematic.core.event.SimpleEventBus;
import net.minecraft.world.entity.Mob;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 生物组件容器实现，支持动态加载和热替换
 */
public class MobComponentContainer implements ComponentContainer {

    private final Map<Class<? extends Component>, Component> componentsByClass = new ConcurrentHashMap<>();
    private final Map<ComponentType, Component> componentsByType = new ConcurrentHashMap<>();
    private final List<Component> updateOrder = new CopyOnWriteArrayList<>();
    private final Mob mobEntity;
    private final EventBus eventBus;
    private boolean active = true;

    public MobComponentContainer(Mob mobEntity) {
        this.mobEntity = mobEntity;
        this.eventBus = new SimpleEventBus();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Component> T getComponent(Class<T> componentClass) {
        return (T) componentsByClass.get(componentClass);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Component> T getComponent(ComponentType type) {
        return (T) componentsByType.get(type);
    }

    @Override
    public void addComponent(Component component) {
        if (!active) {
            throw new IllegalStateException("Container is shutdown");
        }

        Class<? extends Component> componentClass = component.getClass();
        ComponentType componentType = component.getComponentType();

        // 检查是否已存在同类型组件
        Component existing = componentsByType.get(componentType);
        if (existing != null) {
            removeComponent(existing.getClass());
        }

        // 初始化组件
        ComponentContext context = new ComponentContext(this, mobEntity, eventBus);
        component.initialize(context);
        // 注册组件
        componentsByClass.put(componentClass, component);
        componentsByType.put(componentType, component);
        // 按优先级排序插入更新列表
        insertByPriority(component);
        // 注入依赖
        DependencyInjector.injectDependencies(component, context);
        // 注册事件监听
        eventBus.registerComponent(component);
        // 发布组件添加事件
        eventBus.post(new ComponentLifecycleEvent(ComponentLifecycleEvent.Type.COMPONENT_ADDED, component));
    }

    @Override
    public void removeComponent(Class<? extends Component> componentClass) {
        Component component = componentsByClass.remove(componentClass);
        if (component != null) {
            removeComponentInternal(component);
        }
    }

    @Override
    public void removeComponent(ComponentType type) {
        Component component = componentsByType.remove(type);
        if (component != null) {
            removeComponentInternal(component);
        }
    }

    private void removeComponentInternal(Component component) {
        Class<? extends Component> componentClass = component.getClass();
        ComponentType componentType = component.getComponentType();
        componentsByClass.remove(componentClass);
        componentsByType.remove(componentType);
        updateOrder.remove(component);
        // 取消事件监听
        eventBus.unregisterComponent(component);
        // 发布组件移除事件
        eventBus.post(new ComponentLifecycleEvent(ComponentLifecycleEvent.Type.COMPONENT_REMOVED, component));
        // 关闭组件
        component.shutdown();
    }

    @Override
    public void updateComponents(float deltaTime) {
        if (!active) return;
        for (Component component : updateOrder) {
            if (component.isEnabled()) {
                try {
                    component.update(deltaTime);
                } catch (Exception e) {
                    // 记录错误但不中断其他组件
                    System.err.println("Error updating component: " + component.getComponentId());
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public boolean hasComponent(Class<? extends Component> componentClass) {
        return componentsByClass.containsKey(componentClass);
    }

    @Override
    public boolean hasComponent(ComponentType type) {
        return componentsByType.containsKey(type);
    }

    @Override
    public Set<ComponentType> getComponentTypes() {
        return Collections.unmodifiableSet(componentsByType.keySet());
    }

    @Override
    public int getComponentCount() {
        return componentsByClass.size();
    }

    @Override
    public void shutdown() {
        active = false;
        // 关闭所有组件
        for (Component component : updateOrder) {
            try {
                component.shutdown();
            } catch (Exception e) {
                System.err.println("Error shutting down component: " + component.getComponentId());
                e.printStackTrace();
            }
        }

        componentsByClass.clear();
        componentsByType.clear();
        updateOrder.clear();
    }

    /**
     * 热替换组件
     */
    public void hotSwapComponent(Component newComponent) {
        ComponentType componentType = newComponent.getComponentType();
        Component oldComponent = componentsByType.get(componentType);

        if (oldComponent != null) {
            // 保存旧组件的状态
            ComponentState state = oldComponent instanceof StatefulComponent ?
                    ((StatefulComponent) oldComponent).saveState() : null;

            // 移除旧组件
            removeComponent(componentType);

            // 如果新组件支持状态恢复，则恢复状态
            if (state != null && newComponent instanceof StatefulComponent) {
                ((StatefulComponent) newComponent).restoreState(state);
            }
        }

        // 添加新组件
        addComponent(newComponent);

        eventBus.post(new ComponentLifecycleEvent(
                ComponentLifecycleEvent.Type.COMPONENT_HOT_SWAPPED, newComponent));
    }

    private void insertByPriority(Component component) {
        int index = 0;
        for (; index < updateOrder.size(); index++) {
            if (component.getPriority() > updateOrder.get(index).getPriority()) {
                break;
            }
        }

        updateOrder.add(index, component);
    }

    // 获取内部状态用于调试
    public ContainerDebugInfo getDebugInfo() {
        return new ContainerDebugInfo(mobEntity.getUUID(), componentsByType.keySet(), updateOrder.size(), active);
    }

}