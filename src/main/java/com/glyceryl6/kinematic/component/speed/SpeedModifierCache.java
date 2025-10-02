package com.glyceryl6.kinematic.component.speed;

import com.glyceryl6.kinematic.component.speed.modifier.SpeedModifier;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 速度计算缓存，避免重复计算
 */
public class SpeedModifierCache {

    private final ConcurrentMap<CacheKey, Float> calculationCache = new ConcurrentHashMap<>();
    private final int maxCacheSize = 1000;

    public void cacheCalculation(List<SpeedModifier> modifiers, float result) {
        if (this.calculationCache.size() >= this.maxCacheSize) {
            // 简单的LRU策略：清理一半缓存
            int targetSize = maxCacheSize / 2;
            while (calculationCache.size() > targetSize) {
                this.calculationCache.remove(this.calculationCache.keySet().iterator().next());
            }
        }

        CacheKey key = new CacheKey(modifiers);
        this.calculationCache.put(key, result);
    }

    public boolean isCalculationCached(List<SpeedModifier> modifiers) {
        CacheKey key = new CacheKey(modifiers);
        return this.calculationCache.containsKey(key);
    }

    public float getCachedSpeed(List<SpeedModifier> modifiers) {
        CacheKey key = new CacheKey(modifiers);
        return this.calculationCache.getOrDefault(key, 0.0f);
    }

    public void clear() {
        this.calculationCache.clear();
    }

    /**
     * 缓存键，基于修饰符列表的哈希
     */
    private static class CacheKey {

        private final int hashCode;
        private final List<SpeedModifier> modifiers;

        public CacheKey(List<SpeedModifier> modifiers) {
            this.modifiers = List.copyOf(modifiers);
            this.hashCode = computeHashCode();
        }

        private int computeHashCode() {
            int result = 1;
            for (SpeedModifier modifier : this.modifiers) {
                if (modifier.isActive()) {
                    result = 31 * result + modifier.getModifierId().hashCode();
                    result = 31 * result + Float.floatToIntBits(modifier.getValue());
                }
            }

            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof CacheKey other)) return false;
            if (this.modifiers.size() != other.modifiers.size()) return false;
            for (int i = 0; i < this.modifiers.size(); i++) {
                SpeedModifier mod1 = this.modifiers.get(i);
                SpeedModifier mod2 = other.modifiers.get(i);
                if (!mod1.getModifierId().equals(mod2.getModifierId()) ||
                        Float.compare(mod1.getValue(), mod2.getValue()) != 0 ||
                        mod1.isActive() != mod2.isActive()) {
                    return false;
                }
            }

            return true;
        }

        @Override
        public int hashCode() {
            return this.hashCode;
        }

    }

}