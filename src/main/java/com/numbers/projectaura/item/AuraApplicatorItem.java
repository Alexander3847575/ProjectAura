package com.numbers.projectaura.item;

import com.numbers.projectaura.auras.IElementalAura;
import com.numbers.projectaura.capability.AuraCapability;
import com.numbers.projectaura.registries.CapabilityRegistry;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public class AuraApplicatorItem extends Item {
    // TODO: make this get the aura when it is registered? I know theres something else which needs finish registering AFTER something else gets registered
    private Supplier<IElementalAura> aura;
    public AuraApplicatorItem(Supplier<IElementalAura> aura, Properties p_41383_) {
        super(p_41383_);
        this.aura = aura;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand) {

        AuraCapability capability = CapabilityRegistry.getCapability(entity, CapabilityRegistry.AURA_CAPABILITY);

        if (capability != null) {

            player.getLevel().playLocalSound(player.getOnPos(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.5f, 0.5f, false);
            capability.applyAura(entity, this.aura.get(), 16.0d);
            return InteractionResult.SUCCESS;

        }else {
            return InteractionResult.FAIL;
        }

    }
}
