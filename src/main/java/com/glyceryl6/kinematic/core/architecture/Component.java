package com.glyceryl6.kinematic.core.architecture;

import com.glyceryl6.kinematic.core.event.ComponentEvent;

/**
 * 组件基础接口，定义所有组件的生命周期和基本行为
 */
public interface Component {

    /**
     * 获取组件唯一标识
     */
    String getComponentId();

    /**
     * 获取组件类型
     */
    ComponentType getComponentType();

    /**
     * 组件初始化
     */
    void initialize(ComponentContext context);

    /**
     * 每帧更新
     */
    void update(float deltaTime);

    /**
     * 组件销毁
     */
    void shutdown();

    /**
     * 组件是否启用
     */
    boolean isEnabled();

    /**
     * 设置启用状态
     */
    void setEnabled(boolean enabled);

    /**
     * 处理组件事件
     */
    default void onEvent(ComponentEvent event) {}

    /**
     * 获取组件优先级（影响执行顺序）
     */
    default int getPriority() {
        return 0;
    }

}