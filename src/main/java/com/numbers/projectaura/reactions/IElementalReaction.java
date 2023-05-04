package com.numbers.projectaura.reactions;


import com.numbers.projectaura.auras.IElementalAura;

import java.util.function.Supplier;

public interface IElementalReaction<A extends IElementalAura, B extends IElementalAura> {

    ReactionData react(ReactionData data);

    Supplier<IElementalAura> getApplied();
    Supplier<IElementalAura> getBase();

    String getName();

    int getColor();

}
