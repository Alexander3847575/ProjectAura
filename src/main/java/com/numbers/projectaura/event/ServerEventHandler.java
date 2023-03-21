package com.numbers.projectaura.event;

import com.numbers.projectaura.capability.AuraCapability;
import com.numbers.projectaura.capability.HealthBarCapability;
import com.numbers.projectaura.registries.CapabilityRegistry;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ServerEventHandler {

    @SubscribeEvent
    public void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (event.getEntity() instanceof LivingEntity) {

            LivingEntity entity = event.getEntity();

            AuraCapability auraCapability = CapabilityRegistry.getCapability(entity, CapabilityRegistry.AURA_CAPABILITY);

            if (auraCapability != null) {
                auraCapability.tick(entity);
            }

            HealthBarCapability healthBarCapability = CapabilityRegistry.getCapability(entity, CapabilityRegistry.HEALTH_BAR_CAPABILITY);

            if (healthBarCapability != null) {
                healthBarCapability.tick(entity);
            }
        }
    }

}
