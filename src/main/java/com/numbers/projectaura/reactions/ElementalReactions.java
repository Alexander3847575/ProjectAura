package com.numbers.projectaura.reactions;

import com.numbers.projectaura.auras.IElementalAura;
import com.numbers.projectaura.registries.AuraRegistry;

import java.util.function.Supplier;

/**
 * Dmg multipliers, AoE dmg, DoT, def/atk dmg reduction, applies a lifesteal aura that grants hp when damage is dealt to the entity with this aura, shield creation, mvmt/ atk spd red., draws enemies in, pushes them away, creates small dot field, stun effect, hp percent damage
 * also could debuff character slightly in exchange for buffs, affecting speed
 */
public class ElementalReactions {

    public static class FireOnWater implements IElementalReaction {

        @Override
        public ReactionData react(ReactionData data) {
            data.setDamage(data.getDamage() * 1.5f);
            return data;
        }

        @Override
        public Supplier<IElementalAura> getApplied() {
            return () -> AuraRegistry.FIRE.get();
        }

        @Override
        public Supplier<IElementalAura> getBase() {
            return () -> AuraRegistry.WATER.get();
        }

    }

    public static class WaterOnFire implements IElementalReaction {

        @Override
        public ReactionData react(ReactionData data) {
            data.setDamage(data.getDamage() * 1.5f);
            return data;
        }

        @Override
        public Supplier<IElementalAura> getApplied() {
            return () -> AuraRegistry.WATER.get();
        }

        @Override
        public Supplier<IElementalAura> getBase() {
            return () -> AuraRegistry.FIRE.get();
        }

    }
}
