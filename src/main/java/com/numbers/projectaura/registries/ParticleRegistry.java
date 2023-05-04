package com.numbers.projectaura.registries;

import com.mojang.serialization.Codec;
import com.numbers.projectaura.ProjectAura;
import com.numbers.projectaura.ui.DamageNumberParticle;
import net.minecraft.core.particles.ParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = ProjectAura.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ParticleRegistry {

    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPE_REGISTRY = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, ProjectAura.MOD_ID);
    public static final RegistryObject<ParticleType<DamageNumberParticle.DamageParticleOptions>> DAMAGE_EFFECT = PARTICLE_TYPE_REGISTRY.register("damage_effect", () -> new ParticleType<>(true, DamageNumberParticle.DamageParticleOptions.DESERIALIZER) {
        @Override
        public Codec<DamageNumberParticle.DamageParticleOptions> codec() {
            return DamageNumberParticle.DamageParticleOptions.CODEC;
        }
    });

    @SubscribeEvent
    public static void registerParticles(RegisterParticleProvidersEvent event) {
        event.register(DAMAGE_EFFECT.get(), DamageNumberParticle.Factory::new);
    }

}
