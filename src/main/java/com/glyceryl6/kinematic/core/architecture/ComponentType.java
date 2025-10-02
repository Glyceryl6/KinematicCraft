// ComponentType.java - 组件类型枚举
package com.glyceryl6.kinematic.core.architecture;

public enum ComponentType {

    SPEED("speed"),
    PATHFINDING("pathfinding"),
    PATHFINDING_ALGORITHM("pathfinding_algorithm"),
    MOVEMENT_EXECUTOR("movement_executor"),
    ENVIRONMENT_SENSOR("environment_sensor"),
    STATE_MANAGER("state_manager"),
    GOAL_INTEGRATION("goal_integration"),
    CONFIGURATION("configuration"),
    CUSTOM("custom");

    private final String id;

    ComponentType(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

}