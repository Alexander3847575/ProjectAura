package com.numbers.projectaura.item;

import com.numbers.projectaura.capability.AuraCapability;
import com.numbers.projectaura.registries.AuraRegistry;
import com.numbers.projectaura.registries.CapabilityRegistry;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

public class AuraApplicatorItem extends Item {
    public AuraApplicatorItem(Properties p_41383_) {
        super(p_41383_);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand) {

        AuraCapability capability = CapabilityRegistry.getCapability(entity, CapabilityRegistry.AURA_CAPABILITY);

        if (capability != null) {

            player.getLevel().playLocalSound(player.getOnPos(), SoundEvents.FLINTANDSTEEL_USE, SoundSource.PLAYERS, 0.5f, 0.5f, false);
            capability.applyAura(AuraRegistry.FIRE.get(), 10.0d);
            return InteractionResult.SUCCESS;

        }else {
            return InteractionResult.FAIL;
        }

    }
}
