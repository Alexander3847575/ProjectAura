package com.numbers.projectaura.auras.applicator;

import net.minecraft.world.damagesource.DamageSource;

import java.util.function.Supplier;

/**
 * Represents the source of elemental damage, to keep track of application cooldown per entity and also to provide damage context.
 * @param sourceId The entity id that caused the damage, or -1 for environmental damage.
 * @param damageSource The Minecraft {@link DamageSource} associated with the damage instance.
 */
public record ApplicationSource(int sourceId, Supplier<DamageSource> damageSource) { }
