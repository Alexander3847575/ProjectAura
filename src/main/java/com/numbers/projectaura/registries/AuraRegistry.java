package com.numbers.projectaura.registries;

import com.numbers.projectaura.ProjectAura;
import com.numbers.projectaura.auras.FireAura;
import com.numbers.projectaura.auras.IElementalAura;
import com.numbers.projectaura.auras.WaterAura;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.*;

import java.util.function.Supplier;

public class AuraRegistry {
    public static final DeferredRegister<IElementalAura> AURA_REGISTRY = DeferredRegister.create(new ResourceLocation(ProjectAura.MOD_ID, "auras"), ProjectAura.MOD_ID);
    public static final Supplier<IForgeRegistry<IElementalAura>> AURAS = AURA_REGISTRY.makeRegistry(RegistryBuilder::new);

    public static final RegistryObject<FireAura> FIRE = AURA_REGISTRY.register("fire", FireAura::new);
    public static final RegistryObject<WaterAura> WATER = AURA_REGISTRY.register("water", WaterAura::new);


}
