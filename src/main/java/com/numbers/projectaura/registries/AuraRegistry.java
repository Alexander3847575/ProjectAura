package com.numbers.projectaura.registries;

import com.numbers.projectaura.ProjectAura;
import com.numbers.projectaura.auras.FireAura;
import com.numbers.projectaura.auras.ElementalAura;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class AuraRegistry {
    public static final DeferredRegister<ElementalAura> AURA_REGISTRY = DeferredRegister.create(new ResourceLocation(ProjectAura.MOD_ID, "auras"), ProjectAura.MOD_ID);
    public static final Supplier<IForgeRegistry<ElementalAura>> AURAS = AURA_REGISTRY.makeRegistry(RegistryBuilder::new);



    public static final RegistryObject<ElementalAura> FIRE = AURA_REGISTRY.register("fire", FireAura::new);


}
