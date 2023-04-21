package com.numbers.projectaura.event;

import com.numbers.projectaura.capability.AuraCapability;
import com.numbers.projectaura.capability.HealthBarCapability;
import com.numbers.projectaura.network.ElementalReactionMessage;
import com.numbers.projectaura.capability.CapabilityHandler;
import com.numbers.projectaura.network.NetworkHandler;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;

public class EventHandler {

    @SubscribeEvent
    public void onLivingTick(LivingEvent.LivingTickEvent event) {

        LivingEntity entity = event.getEntity();

       if (entity.level.isClientSide()) {
           // Health bar rendering only occurs on client
           HealthBarCapability healthBarCapability = CapabilityHandler.getCapability(entity, CapabilityHandler.HEALTH_BAR_CAPABILITY);

           if (healthBarCapability != null) {
               healthBarCapability.tick(entity);
           }

       } else {

           // Server handles all aura stuff, client is just for display
           AuraCapability auraCapability = CapabilityHandler.getCapability(entity, CapabilityHandler.AURA_CAPABILITY);

           if (auraCapability != null) {
               auraCapability.tick();
           }

       }

    }

    @SubscribeEvent
    public void onPlayerAttack(AttackEntityEvent event) {
        // TODO: for implementation of elemental damaging left click attacks?
    }

    @SubscribeEvent
    public void onElementalApplication(ElementalApplicationEvent event) {
        if (!event.getAppliedEntity().level.isClientSide) {

        }
    }

    @SubscribeEvent
    public void onElementalReaction(ElementalReactionEvent event) {

        if (!event.getLivingEntity().level.isClientSide()) {
            // Reroute to client
            NetworkHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(event::getLivingEntity), new ElementalReactionMessage(event.getLivingEntity(), event.getReactionData()));
            return;
        }

        HealthBarCapability healthBarCapability = CapabilityHandler.getCapability(event.getLivingEntity(), CapabilityHandler.HEALTH_BAR_CAPABILITY);

        assert healthBarCapability != null;
        healthBarCapability.onReaction(event);

    }

}
