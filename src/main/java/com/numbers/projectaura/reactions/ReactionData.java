package com.numbers.projectaura.reactions;

import lombok.Builder;
import lombok.Data;
import net.minecraft.world.entity.LivingEntity;

/**
 * What can a reaction do?
 * modify damage
 * apply a reaction aura
 */
@Data @Builder
public class ReactionData {

    private double inputAppliedStrength;
    private double inputBaseStrength;

    private double outputAppliedStrength;
    private double outputBaseStrength;

    private float damage;

    private boolean failed;

    private LivingEntity target;



}
