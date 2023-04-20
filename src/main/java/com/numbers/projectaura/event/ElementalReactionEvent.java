package com.numbers.projectaura.event;

import com.numbers.projectaura.reactions.IElementalReaction;
import com.numbers.projectaura.reactions.ReactionData;
import lombok.Getter;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event;

public class ElementalReactionEvent extends Event {

    @Getter
    private final LivingEntity livingEntity;
    @Getter
    private final IElementalReaction<?, ?> elementalReaction;
    @Getter
    private final ReactionData reactionData;

    public ElementalReactionEvent(LivingEntity livingEntity, IElementalReaction<?, ?> elementalReaction, ReactionData reactionData) {
        this.livingEntity = livingEntity;
        this.elementalReaction = elementalReaction;
        this.reactionData = reactionData;
    }
}
