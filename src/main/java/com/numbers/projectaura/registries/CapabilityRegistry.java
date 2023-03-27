package com.numbers.projectaura.registries;

import com.numbers.projectaura.capability.AuraCapability;
import com.numbers.projectaura.capability.HealthBarCapability;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.DistExecutor;

import javax.annotation.Nullable;

public class CapabilityRegistry {

    public static final Capability<AuraCapability> AURA_CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});
    public static final Capability<HealthBarCapability> HEALTH_BAR_CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.register(AuraCapability.class);
        event.register(HealthBarCapability.class);

    }

    public static void attachEntityCapability(AttachCapabilitiesEvent<Entity> e) {
        if (e.getObject() instanceof LivingEntity) {
            e.addCapability(AuraCapability.ID, new AuraCapability.AuraCapabilityProvider());

            // Only add the health bar thing on the client
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                e.addCapability(HealthBarCapability.ID, new HealthBarCapability.HealthBarCapabilityProvider());
            });
        }
    }

    @Nullable
    public static <T> T getCapability(Entity entity, Capability<T> capability) {
        if (entity == null) return null;
        if (!entity.isAlive()) return null;
        return entity.getCapability(capability).isPresent() ? entity.getCapability(capability).orElseThrow(() -> new IllegalArgumentException("Lazy optional must not be empty")) : null;
    }

}
