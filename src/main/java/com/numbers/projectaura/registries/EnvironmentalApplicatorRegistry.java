package com.numbers.projectaura.registries;

import com.numbers.projectaura.ProjectAura;
import com.numbers.projectaura.auras.FireAura;
import com.numbers.projectaura.auras.IElementalAura;
import com.numbers.projectaura.auras.WaterAura;
import com.numbers.projectaura.auras.applicator.ApplicationInstance;
import com.numbers.projectaura.auras.applicator.ApplicationSource;
import com.numbers.projectaura.auras.applicator.ApplicationType;
import com.numbers.projectaura.auras.applicator.IEnvironmentalApplicator;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class EnvironmentalApplicatorRegistry {
    public static final DeferredRegister<IEnvironmentalApplicator<? extends IElementalAura>> ENVIRONMENTAL_APPLICATOR_REGISTRY = DeferredRegister.create(new ResourceLocation(ProjectAura.MOD_ID, "environmental_applicators"), ProjectAura.MOD_ID);
    public static final Supplier<IForgeRegistry<IEnvironmentalApplicator<? extends IElementalAura>>> ENVIRONMENTAL_APPLICATORS = ENVIRONMENTAL_APPLICATOR_REGISTRY.makeRegistry(RegistryBuilder::new);

    public static final RegistryObject<IEnvironmentalApplicator<FireAura>> ON_FIRE = ENVIRONMENTAL_APPLICATOR_REGISTRY.register("on_fire", () -> new IEnvironmentalApplicator<>() {
        private static final ApplicationInstance applicationInstance = new ApplicationInstance(new ApplicationType("on_fire", AuraRegistry.FIRE.get(), 10), 6, 0, new ApplicationSource(-1, () -> Minecraft.getInstance().level.damageSources().onFire()));

        @Override
        public @Nullable ApplicationInstance test(LivingEntity entity) {
            if (entity.isOnFire())
                return applicationInstance;
            return null;
        }
    });

    public static final RegistryObject<IEnvironmentalApplicator<WaterAura>> IN_RAIN = ENVIRONMENTAL_APPLICATOR_REGISTRY.register("wet", () -> new IEnvironmentalApplicator<>() {
        private static final ApplicationInstance applicationInstance = new ApplicationInstance(new ApplicationType("wet", AuraRegistry.WATER.get(), 10), 6, 0, new ApplicationSource(-1, () -> Minecraft.getInstance().level.damageSources().generic()));

        @Override
        public @Nullable ApplicationInstance test(LivingEntity entity) {
            if (entity.isInWaterRainOrBubble())
                return applicationInstance;
            return null;
        }
    });

}
