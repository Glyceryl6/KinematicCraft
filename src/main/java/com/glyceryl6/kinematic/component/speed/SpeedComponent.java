package com.glyceryl6.kinematic.component.speed;

import com.glyceryl6.kinematic.component.speed.modifier.ConditionalSpeedModifier;
import com.glyceryl6.kinematic.component.speed.modifier.ModifierType;
import com.glyceryl6.kinematic.component.speed.modifier.SpeedModifier;
import com.glyceryl6.kinematic.core.architecture.AbstractComponent;
import com.glyceryl6.kinematic.core.architecture.ComponentType;
import com.glyceryl6.kinematic.core.dependency.ComponentDependency;
import com.glyceryl6.kinematic.core.event.ComponentEvent;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

/**
 * 速度组件 - 管理生物移动速度的核心组件
 */
public class SpeedComponent extends AbstractComponent {

    public static final String COMPONENT_ID = "speed";

    // 依赖组件
//    @ComponentDependency
//    private StateManagerComponent stateManager;

    // 速度数据
    private float baseSpeed;
    private float currentSpeed;
    private float cachedSpeed;
    private boolean speedDirty = true;

    // 修饰符管理
    private final List<SpeedModifier> modifiers = new CopyOnWriteArrayList<>();
    private final Map<UUID, SpeedModifier> modifierMap = new ConcurrentHashMap<>();
    private final SpeedModifierCache modifierCache = new SpeedModifierCache();

    // 原版兼容
    private AttributeInstance vanillaSpeedAttribute;
    private float lastVanillaSpeed = -1f;

    // 性能监控
    private long lastCalculationTime = 0;
    private int calculationCount = 0;
    private final SpeedMetrics metrics = new SpeedMetrics();

    @Override
    public String getComponentId() {
        return COMPONENT_ID;
    }

    @Override
    public ComponentType getComponentType() {
        return ComponentType.SPEED;
    }

    @Override
    public void onInitialize() {
        Mob mob = context.getMobEntity();

        // 获取原版速度属性
        this.vanillaSpeedAttribute = mob.getAttribute(Attributes.MOVEMENT_SPEED);
        if (this.vanillaSpeedAttribute != null) {
            this.baseSpeed = (float) vanillaSpeedAttribute.getBaseValue();
            this.currentSpeed = baseSpeed;
            this.cachedSpeed = baseSpeed;
        }

        // 注册事件监听
        context.getEventBus().subscribe("environment_changed", this::onEnvironmentChanged);
        context.getEventBus().subscribe("state_changed", this::onStateChanged);

        System.out.println("SpeedComponent initialized for mob: " + mob.getType().getDescription().getString());
    }

    @Override
    public void update(float deltaTime) {
        long startTime = System.nanoTime();

        // 清理过期的修饰符
        this.cleanupExpiredModifiers();

        // 检查速度是否需要重新计算
        if (this.speedDirty || shouldRecalculateSpeed()) {
            recalculateSpeed();
        }

        // 同步到原版系统
        this.syncWithVanillaSystem();

        // 更新性能指标
        this.updateMetrics(System.nanoTime() - startTime);
    }

    /**
     * 添加速度修饰符
     */
    public UUID addModifier(SpeedModifier modifier) {
        // 移除同名的旧修饰符（如果需要）
        if (this.shouldReplaceExisting(modifier)) {
            this.removeModifiersByName(modifier.getName());
        }

        this.modifiers.add(modifier);
        this.modifierMap.put(modifier.getModifierId(), modifier);

        // 按优先级排序
        this.modifiers.sort(Comparator.comparingInt(SpeedModifier::getPriority).reversed());

        this.markSpeedDirty();

        // 发布速度变化事件
        this.publishSpeedChangeEvent(modifier, SpeedChangeEvent.Type.MODIFIER_ADDED);

        return modifier.getModifierId();
    }

    /**
     * 移除速度修饰符
     */
    public boolean removeModifier(UUID modifierId) {
        SpeedModifier modifier = this.modifierMap.remove(modifierId);
        if (modifier != null) {
            this.modifiers.remove(modifier);
            this.markSpeedDirty();
            this.publishSpeedChangeEvent(modifier, SpeedChangeEvent.Type.MODIFIER_REMOVED);
            return true;
        }

        return false;
    }

    /**
     * 添加临时修饰符（在指定时间后自动移除）
     */
    public UUID addTemporaryModifier(SpeedModifier modifier, long durationMs) {
        modifier.setDuration(durationMs);
        return addModifier(modifier);
    }

    /**
     * 添加条件修饰符
     */
    public UUID addConditionalModifier(String name, SpeedModifier baseModifier, Predicate<Mob> condition) {
        ConditionalSpeedModifier conditionalModifier = new ConditionalSpeedModifier(name, baseModifier, condition);
        return addModifier(conditionalModifier);
    }

    /**
     * 重新计算速度
     */
    private void recalculateSpeed() {
        this.calculationCount++;

        float calculatedSpeed = this.baseSpeed;

        // 应用所有激活的修饰符
        for (SpeedModifier modifier : this.modifiers) {
            if (modifier.isActive()) {
                calculatedSpeed = modifier.apply(this.baseSpeed, calculatedSpeed, context.getMobEntity());
            }
        }

        // 确保速度在合理范围内
        calculatedSpeed = Math.max(0, calculatedSpeed);
        calculatedSpeed = Math.min(calculatedSpeed, 10.0f); // 最大速度限制

        this.currentSpeed = calculatedSpeed;
        this.cachedSpeed = calculatedSpeed;
        this.speedDirty = false;

        // 缓存计算结果
        this.modifierCache.cacheCalculation(this.modifiers, calculatedSpeed);
    }

    /**
     * 与原版速度系统同步
     */
    private void syncWithVanillaSystem() {
        if (vanillaSpeedAttribute != null) {
            // 检查原版速度是否被外部修改
            double currentVanillaSpeed = vanillaSpeedAttribute.getValue();
            if (Math.abs(currentVanillaSpeed - lastVanillaSpeed) > 0.001f) {
                // 原版速度被修改，更新我们的基础速度
                if (Math.abs(currentVanillaSpeed - baseSpeed) > 0.001f) {
                    baseSpeed = (float) currentVanillaSpeed;
                    markSpeedDirty();
                }
                lastVanillaSpeed = (float) currentVanillaSpeed;
            }

            // 如果我们的计算速度与原版不同，则更新原版系统
            if (Math.abs(currentSpeed - currentVanillaSpeed) > 0.001f) {
                vanillaSpeedAttribute.setBaseValue(currentSpeed);
                lastVanillaSpeed = currentSpeed;
            }
        }
    }

    /**
     * 清理过期的修饰符
     */
    private void cleanupExpiredModifiers() {
        Iterator<SpeedModifier> iterator = modifiers.iterator();
        boolean removed = false;
        while (iterator.hasNext()) {
            SpeedModifier modifier = iterator.next();
            if (modifier.isExpired()) {
                iterator.remove();
                modifierMap.remove(modifier.getModifierId());
                removed = true;
            }
        }

        if (removed) {
            markSpeedDirty();
        }
    }

    /**
     * 检查是否需要重新计算速度
     */
    private boolean shouldRecalculateSpeed() {
        // 使用缓存检查
        if (modifierCache.isCalculationCached(modifiers)) {
            currentSpeed = modifierCache.getCachedSpeed(modifiers);
            return false;
        }

        // 定期重新计算（每10帧）
        return calculationCount % 10 == 0;
    }

    /**
     * 标记速度需要重新计算
     */
    private void markSpeedDirty() {
        speedDirty = true;
    }

    /**
     * 检查是否应该替换现有修饰符
     */
    private boolean shouldReplaceExisting(SpeedModifier newModifier) {
        // 对于SET类型的修饰符，总是替换同类型的
        return newModifier.getType() == ModifierType.SET;
    }

    /**
     * 按名称移除修饰符
     */
    private void removeModifiersByName(String name) {
        this.modifiers.removeIf(modifier -> {
            if (modifier.getName().equals(name)) {
                this.modifierMap.remove(modifier.getModifierId());
                return true;
            }
            return false;
        });
    }

    // 事件处理
    private void onEnvironmentChanged(ComponentEvent event) {
        // 环境变化可能导致速度需要调整
        this.markSpeedDirty();
    }

    private void onStateChanged(ComponentEvent event) {
        // 状态变化可能影响速度
        this.markSpeedDirty();
    }

    /**
     * 发布速度变化事件
     */
    private void publishSpeedChangeEvent(SpeedModifier modifier, SpeedChangeEvent.Type type) {
        SpeedChangeEvent event = new SpeedChangeEvent(this, currentSpeed, baseSpeed, modifier, type);
        context.getEventBus().post(event);
    }

    /**
     * 更新性能指标
     */
    private void updateMetrics(long calculationTime) {
        metrics.recordCalculation(calculationTime, modifiers.size());
    }

    // ========== 公共API ==========

    /**
     * 获取当前速度
     */
    public float getCurrentSpeed() {
        return this.currentSpeed;
    }

    /**
     * 获取基础速度
     */
    public float getBaseSpeed() {
        return this.baseSpeed;
    }

    /**
     * 设置基础速度
     */
    public void setBaseSpeed(float baseSpeed) {
        if (Math.abs(this.baseSpeed - baseSpeed) > 0.001f) {
            this.baseSpeed = baseSpeed;
            markSpeedDirty();
        }
    }

    /**
     * 获取所有激活的修饰符
     */
    public List<SpeedModifier> getActiveModifiers() {
        return this.modifiers.stream().filter(SpeedModifier::isActive)
                .sorted(Comparator.comparingInt(SpeedModifier::getPriority).reversed())
                .toList();
    }

    /**
     * 获取速度乘数（当前速度/基础速度）
     */
    public float getSpeedMultiplier() {
        return this.baseSpeed > 0 ? this.currentSpeed / this.baseSpeed : 1.0f;
    }

    /**
     * 清除所有临时修饰符
     */
    public void clearTemporaryModifiers() {
        boolean removed = this.modifiers.removeIf(modifier ->
                modifier.isExpired() || modifier.getType() == ModifierType.ADDITIVE
        );

        if (removed) {
            this.modifierMap.clear();
            this.modifiers.forEach(mod -> this.modifierMap.put(mod.getModifierId(), mod));
            markSpeedDirty();
        }
    }

    /**
     * 获取性能指标
     */
    public SpeedMetrics getMetrics() {
        return this.metrics.copy();
    }

    @Override
    public void onShutdown() {
        // 恢复原版速度
        if (vanillaSpeedAttribute != null) {
            vanillaSpeedAttribute.setBaseValue(baseSpeed);
        }

        // 清理资源
        modifiers.clear();
        modifierMap.clear();
        modifierCache.clear();

        System.out.println("SpeedComponent shutdown for mob: " + context.getMobEntity().getType().getDescription().getString());
    }

}