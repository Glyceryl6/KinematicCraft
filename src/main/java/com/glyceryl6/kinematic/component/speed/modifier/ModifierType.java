// ModifierType.java - 修饰符类型枚举
package com.glyceryl6.kinematic.component.speed.modifier;

public enum ModifierType {
    ADDITIVE,       // 加法修饰符
    MULTIPLICATIVE, // 乘法修饰符
    SET,           // 设置修饰符（覆盖）
    PERCENTAGE,    // 百分比修饰符
    COMPOUND       // 复合修饰符
}