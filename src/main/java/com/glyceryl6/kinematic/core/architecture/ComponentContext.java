package com.glyceryl6.kinematic.core.architecture;

import com.glyceryl6.kinematic.core.container.ComponentContainer;
import net.minecraft.world.entity.Mob;
import java.util.UUID;

/**
 * 组件执行上下文，提供运行时环境信息
 */
public class ComponentContext {

    private final ComponentContainer container;
    private final Mob mobEntity;
    private final EventBus eventBus;
    private final UUID contextId;
    private final long createTime;

    public ComponentContext(ComponentContainer container, Mob mobEntity, EventBus eventBus) {
        this.container = container;
        this.mobEntity = mobEntity;
        this.eventBus = eventBus;
        this.contextId = UUID.randomUUID();
        this.createTime = System.currentTimeMillis();
    }

    public <T extends Component> T getComponent(Class<T> componentClass) {
        return container.getComponent(componentClass);
    }

    public <T extends Component> T getComponent(ComponentType type) {
        return container.getComponent(type);
    }

    public Mob getMobEntity() {
        return mobEntity;
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public UUID getContextId() {
        return contextId;
    }

    public long getCreateTime() {
        return createTime;
    }

    public boolean isActive() {
        return mobEntity != null && mobEntity.isAlive();
    }

}