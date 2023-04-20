package com.numbers.projectaura.reactions;

import com.numbers.projectaura.auras.IElementalAura;
import lombok.Builder;
import lombok.Data;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

/**
 * What can a reaction do?
 * modify damage
 * apply a reaction aura
 */
@Data @Builder
public class ReactionData {

    private IElementalAura appliedAura;
    private IElementalAura baseAura;

    private double inputAppliedStrength;
    private double inputBaseStrength;

    private double outputAppliedStrength;
    private double outputBaseStrength;

    private float damage;

    @Nullable
    private IElementalReaction<?, ?> reaction;
    private boolean failed;

    private LivingEntity target;



}
