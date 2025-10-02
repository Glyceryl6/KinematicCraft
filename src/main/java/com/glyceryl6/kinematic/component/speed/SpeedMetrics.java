package com.glyceryl6.kinematic.component.speed;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 速度计算性能监控
 */
public class SpeedMetrics {

    private final AtomicLong totalCalculationTime = new AtomicLong(0);
    private final AtomicLong calculationCount = new AtomicLong(0);
    private final AtomicLong maxCalculationTime = new AtomicLong(0);
    private final AtomicLong averageModifierCount = new AtomicLong(0);

    public void recordCalculation(long calculationTimeNanos, int modifierCount) {
        this.totalCalculationTime.addAndGet(calculationTimeNanos);
        this.calculationCount.incrementAndGet();
        this.maxCalculationTime.updateAndGet(current -> Math.max(current, calculationTimeNanos));
        // 更新平均修饰符数量（滑动平均）
        long currentCount = this.averageModifierCount.get();
        long newCount = (currentCount * 9 + modifierCount) / 10;
        this.averageModifierCount.set(newCount);
    }

    public double getAverageCalculationTimeMicros() {
        long count = this.calculationCount.get();
        if (count == 0) return 0;
        return this.totalCalculationTime.get() / (count * 1000.0);
    }

    public double getMaxCalculationTimeMicros() {
        return this.maxCalculationTime.get() / 1000.0;
    }

    public long getTotalCalculations() {
        return this.calculationCount.get();
    }

    public double getAverageModifierCount() {
        return this.averageModifierCount.get();
    }

    public SpeedMetrics copy() {
        SpeedMetrics copy = new SpeedMetrics();
        copy.totalCalculationTime.set(this.totalCalculationTime.get());
        copy.calculationCount.set(this.calculationCount.get());
        copy.maxCalculationTime.set(this.maxCalculationTime.get());
        copy.averageModifierCount.set(this.averageModifierCount.get());
        return copy;
    }

}