// ComponentDependency.java - 依赖注入注解
package com.glyceryl6.kinematic.core.dependency;

import com.glyceryl6.kinematic.core.architecture.ComponentType;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ComponentDependency {

    boolean required() default true;

    ComponentType value() default ComponentType.CUSTOM;

    String description() default "";

}