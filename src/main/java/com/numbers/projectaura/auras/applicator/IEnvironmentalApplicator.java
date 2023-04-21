package com.numbers.projectaura.auras.applicator;

import com.numbers.projectaura.auras.IElementalAura;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

/**
 * A basic predicate-like functional interface that potentially returns an application instance, intended to test for if certain conditions apply to the entity.
 * @param <T> The type of aura it will apply.
 */
public interface IEnvironmentalApplicator<T extends IElementalAura> {
    @Nullable
    ApplicationInstance test(LivingEntity entity);
}
