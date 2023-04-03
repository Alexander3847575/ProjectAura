package com.numbers.projectaura.event;

import com.numbers.projectaura.capability.AuraCapability;
import com.numbers.projectaura.capability.HealthBarCapability;
import com.numbers.projectaura.registries.CapabilityRegistry;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class EventHandler {

    @SubscribeEvent
    public void onLivingTick(LivingEvent.LivingTickEvent event) {

        LivingEntity entity = event.getEntity();

       if (entity.level.isClientSide()) {
           // Health bar rendering only occurs on client
           HealthBarCapability healthBarCapability = CapabilityRegistry.getCapability(entity, CapabilityRegistry.HEALTH_BAR_CAPABILITY);

           if (healthBarCapability != null) {
               healthBarCapability.tick(entity);
           }

       } else {

           // Server handles all aura stuff, client is just for display
           AuraCapability auraCapability = CapabilityRegistry.getCapability(entity, CapabilityRegistry.AURA_CAPABILITY);

           if (auraCapability != null) {
               auraCapability.tick(entity);
           }

       }

    }

}
