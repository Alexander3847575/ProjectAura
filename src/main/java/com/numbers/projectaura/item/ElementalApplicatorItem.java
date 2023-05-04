package com.numbers.projectaura.item;

import com.numbers.projectaura.auras.IElementalAura;
import com.numbers.projectaura.auras.applicator.ApplicationInstance;
import com.numbers.projectaura.auras.applicator.ApplicationSource;
import com.numbers.projectaura.auras.applicator.ApplicationType;
import com.numbers.projectaura.capability.AuraCapability;
import com.numbers.projectaura.capability.CapabilityHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LazyOptional;

import java.util.function.Function;
import java.util.function.Supplier;

public class ElementalApplicatorItem extends Item {
    public LazyOptional<ApplicationType> applicationType;
    private Function<ApplicationSource, ApplicationInstance> applicationInstance;
    private final String applicatorId;

    public ElementalApplicatorItem(Supplier<IElementalAura> applicationType, String applicatorId, Properties p_41383_) {
        super(p_41383_);
        this.applicationType = LazyOptional.of(() -> new ApplicationType(applicatorId, applicationType.get(), 40));
        this.applicationInstance = (applicationSource) -> new ApplicationInstance(this.getApplicationType(), 16f, 1.5f, applicationSource);
        this.applicatorId = applicatorId;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand) {

        AuraCapability capability = CapabilityHandler.getCapability(entity, CapabilityHandler.AURA_CAPABILITY);

        if (capability != null) {

            player.getLevel().playLocalSound(player.getOnPos(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1f, 0.5f, false);
            capability.tryApplyAura(this.applicationInstance.apply(new ApplicationSource(player.getId(), () -> Minecraft.getInstance().level.damageSources().playerAttack(player))));

            return InteractionResult.SUCCESS; // Still interact even if the application failed

        }

        return InteractionResult.FAIL;


    }
    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pUsedHand);

        pPlayer.startUsingItem(pUsedHand);
        pPlayer.getCooldowns().addCooldown(this, 10);
        pPlayer.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResultHolder.consume(itemstack);

    }

    @Override
    public UseAnim getUseAnimation(ItemStack pStack) {
        return UseAnim.TOOT_HORN;
    }


    public ApplicationType getApplicationType() {
        return this.applicationType.orElseThrow(() -> new RuntimeException("LazyOptional must not be empty"));
    }


}
