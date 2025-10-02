package com.glyceryl6.kinematic.core.dependency;

import com.glyceryl6.kinematic.core.architecture.Component;
import com.glyceryl6.kinematic.core.architecture.ComponentContext;
import com.glyceryl6.kinematic.core.architecture.ComponentType;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * 依赖注入器，自动解析和注入组件依赖
 */
public class DependencyInjector {

    public static void injectDependencies(Component component, ComponentContext context) {
        List<DependencyInfo> dependencies = scanDependencies(component);
        for (DependencyInfo dependency : dependencies) {
            try {
                Object dependencyInstance = resolveDependency(dependency, context);
                if (dependencyInstance != null) {
                    dependency.field.set(component, dependencyInstance);
                } else if (dependency.required) {
                    throw new DependencyResolutionException(
                            "Required dependency not found: " + dependency.field.getName());
                }
            } catch (IllegalAccessException e) {
                throw new DependencyResolutionException(
                        "Failed to inject dependency: " + dependency.field.getName(), e);
            }
        }
    }

    private static List<DependencyInfo> scanDependencies(Component component) {
        List<DependencyInfo> dependencies = new ArrayList<>();
        Class<?> clazz = component.getClass();
        while (clazz != Object.class) {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(ComponentDependency.class)) {
                    ComponentDependency annotation = field.getAnnotation(ComponentDependency.class);
                    dependencies.add(new DependencyInfo(field, annotation));
                }
            }

            clazz = clazz.getSuperclass();
        }

        return dependencies;
    }

    private static Object resolveDependency(DependencyInfo dependency, ComponentContext context) {
        Field field = dependency.field;
        Class<?> fieldType = field.getType();
        if (Component.class.isAssignableFrom(fieldType)) {
            @SuppressWarnings("unchecked")
            Class<? extends Component> componentClass = (Class<? extends Component>) fieldType;
            if (dependency.annotation.value() != ComponentType.CUSTOM) {
                // 按类型查找
                return context.getComponent(dependency.annotation.value());
            } else {
                // 按类查找
                return context.getComponent(componentClass);
            }
        }

        return null;
    }

    private static class DependencyInfo {

        final Field field;
        final ComponentDependency annotation;
        final boolean required;

        DependencyInfo(Field field, ComponentDependency annotation) {
            this.field = field;
            this.annotation = annotation;
            this.required = annotation.required();
            this.field.setAccessible(true);
        }

    }

    public static class DependencyResolutionException extends RuntimeException {

        public DependencyResolutionException(String message) {
            super(message);
        }

        public DependencyResolutionException(String message, Throwable cause) {
            super(message, cause);
        }

    }

}