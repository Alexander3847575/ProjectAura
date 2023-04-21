package com.numbers.projectaura.auras.applicator;

import com.numbers.projectaura.auras.IElementalAura;

/**
 *
 * @param applicatorId The internal id of the applicator, used to distinguish it between other sources. However, a single weapon with multiple types of attacks for example, could have different application types.
 * @param applicationType The type of aura the applicator applies.
 * @param applicatorCooldown The internal cooldown of the applicator in ticks. This represents how often it can apply its aura. This is done so that you can't spam aura application with a single item.
 */
public record ApplicationType(String applicatorId, IElementalAura applicationType, int applicatorCooldown) { }
