package com.numbers.projectaura.auras;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public interface IElementalAura {

    /**
     * Render the particle effects of the aura.
     */
    void renderAura();

    /**
     * Apply effects that occur constantly when an entity has this aura, e.g. the aura gives slowness or reduces defence.
     * Called on every {@link net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent} event.
     * @param entity Entity to apply effects to.
     */
    void applyEffects(LivingEntity entity);

    boolean isVisible();

    String getId();

    ResourceLocation getIcon();

    int getColor();

}
