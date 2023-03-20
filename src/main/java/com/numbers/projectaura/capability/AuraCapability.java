package com.numbers.projectaura.capability;

import com.numbers.projectaura.ProjectAura;
import com.numbers.projectaura.auras.ElementalAura;
import com.numbers.projectaura.registries.AuraRegistry;
import com.numbers.projectaura.registries.CapabilityRegistry;
import lombok.Getter;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class AuraCapability implements INBTSerializable<CompoundTag> {

    public static ResourceLocation ID = new ResourceLocation(ProjectAura.MOD_ID, "aura_capability");
    @Getter
    public HashMap<ElementalAura, Double> auras = new HashMap<>();

    public AuraCapability() {
        // Populate
        AuraRegistry.AURAS.get().getValues().stream().forEach(aura -> auras.put(aura, 0.0d));
    }

    /**
     * Here is where the reaction logic takes place; upon the application of a new aura
     * This method will then call helper methods that deal with the effects of the reactions themselves
     * <p>
     * TODO: make reactions fully data driven
     *
     * @param auraType
     *             The type of {@link ElementalAura} to apply.
     * @param applicationStrength
     *              The strength of the applied aura, in units.
     */
    public void applyAura(ElementalAura auraType, double applicationStrength) {

        double remainingAura = applicationStrength;

        // Cycle through all registered auras
        for (Map.Entry<ElementalAura, Double> aura : auras.entrySet()) {

            // Make sure the aura exists
            if (aura.getValue() != 0.0d) {

                // Attempt a reaction
                if (aura.getKey().react(auraType, aura.getValue(), remainingAura));

            }
        }

        // Add remainder to the entity auras
        auras.put(auraType, remainingAura);

    }

    public void tick(LivingEntity entity) {

        for (Map.Entry<ElementalAura, Double> aura : auras.entrySet()) {

            double value = aura.getValue();

            if (value >= 0.1d) {
                value -= 0.1d;
                ProjectAura.LOGGER.debug(Component.translatable("auras." + ProjectAura.MOD_ID + "." + AuraRegistry.AURAS.get().getKey(aura.getKey()).getPath()).getString() + " @ " + aura.getValue().toString() + " on " + entity.getName().getString());
            } else {
                value = 0d;
            }

            aura.setValue(value);
        }

    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        for (Map.Entry<ElementalAura, Double> aura : auras.entrySet()) {
            nbt.putDouble(AuraRegistry.AURAS.get().getKey(aura.getKey()).getPath(), aura.getValue());
        }
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        for (Map.Entry<ElementalAura, Double> aura : auras.entrySet()) {
            nbt.getDouble(AuraRegistry.AURAS.get().getKey(aura.getKey()).getPath());
        }
    }

    public static class AuraCapabilityProvider implements ICapabilityProvider, ICapabilitySerializable<CompoundTag>
    {
        private final LazyOptional<AuraCapability> instance = LazyOptional.of(AuraCapability::new);

        @Override
        public CompoundTag serializeNBT() {
            return instance.orElseThrow(NullPointerException::new).serializeNBT();
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            instance.orElseThrow(NullPointerException::new).deserializeNBT(nbt);
        }

        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
            return CapabilityRegistry.AURA_CAPABILITY.orEmpty(cap, instance.cast());
        }

    }

}
