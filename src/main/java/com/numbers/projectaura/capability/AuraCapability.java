package com.numbers.projectaura.capability;

import com.numbers.projectaura.ProjectAura;
import com.numbers.projectaura.auras.IElementalAura;
import com.numbers.projectaura.network.AuraSyncMessage;
import com.numbers.projectaura.reactions.IElementalReaction;
import com.numbers.projectaura.reactions.ReactionData;
import com.numbers.projectaura.registries.AuraRegistry;
import com.numbers.projectaura.registries.CapabilityRegistry;
import com.numbers.projectaura.registries.NetworkRegistry;
import com.numbers.projectaura.registries.ReactionRegistry;
import lombok.Getter;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class AuraCapability implements INBTSerializable<CompoundTag> {

    public static ResourceLocation ID = new ResourceLocation(ProjectAura.MOD_ID, "aura_capability");
    @Getter
    public LinkedHashMap<IElementalAura, Double> auras = new LinkedHashMap<>();

    public AuraCapability() {
        // Populate
        //AuraRegistry.AURAS.get().getValues().stream().forEach(aura -> auras.put(aura, 0.0d))
    }

    /**
     * Here is where the reaction logic takes place; upon the application of a new aura
     * This method will then call helper methods that deal with the effects of the reactions themselves
     * <p>
     * TODO: make reactions fully data driven
     *
     * @param applied
     *             The type of {@link IElementalAura} to apply.
     * @param applicationStrength
     *              The strength of the applied aura, in units.
     */
    public void applyAura(LivingEntity target, IElementalAura applied, double applicationStrength) {

        // Server handles all reaction stuff
        if (target.level.isClientSide()) {
            return;
        }

        double remainingAura = applicationStrength;

        // Cycle through all registered auras, avoiding concurrent modification
        Iterator<Map.Entry<IElementalAura, Double>> iterator = auras.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<IElementalAura, Double> aura = iterator.next();

            // Attempt a reaction
            IElementalReaction reaction = ReactionRegistry.getReaction(applied, aura.getKey());
            if (reaction == null){
                continue;
            }
            ReactionData result = reaction.react(ReactionData.builder()
                    .inputAppliedStrength(remainingAura)
                    .inputBaseStrength(aura.getValue())
                    .target(target)
                    .build());

            // No need to update aura values or anything if the reaction failed
            if (result.isFailed()) {
                continue;
            }

            double outputBaseStrength = result.getOutputBaseStrength();
            double outputAppliedStrength = result.getOutputAppliedStrength();

            if (outputBaseStrength < 0.1d) {
                // Immediately remove the base aura if it has run out
                // TODO: make a removeAura method so the renderer can hook into that
                iterator.remove();
            } else {
                // Update the base aura
                aura.setValue(outputBaseStrength);
            }

            if (outputAppliedStrength < 0.1d) {
                // Stop the reaction checks if there is no more aura to react
                return;
            }
            remainingAura = outputAppliedStrength;

        }


        // Add remainder to the entity auras
        auras.put(applied, remainingAura);

    }

    /**
     * Auras are only ticked on the server
     * @param entity
     */
    public void tick(LivingEntity entity) {

        boolean updated = false;

        // Avoic concurrent modification
        Iterator<Map.Entry<IElementalAura, Double>> iterator = auras.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<IElementalAura, Double> aura = iterator.next();
            double value = aura.getValue();

            if (value >= 0.1d) {
                value -= 0.1d;
                aura.setValue(value);
                //ProjectAura.LOGGER.debug(Component.translatable("auras." + ProjectAura.MOD_ID + "." + AuraRegistry.AURAS.get().getKey(aura.getKey()).getPath()).getString() + " @ " + aura.getValue().toString() + " on " + entity.getName().getString());
            } else {
                iterator.remove();
            }

            updated = true;

        }

        // Don't send packets if nothing was updated lol
        if (!updated) {
            return;
        }

        // Update client
        sendAuraUpdatePackets(entity);

    }

    /**
     * Sends an update packet to all clients tracking the entity.
     */
    private void sendAuraUpdatePackets(LivingEntity entity) {
        NetworkRegistry.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), new AuraSyncMessage(entity, this.auras));

    }

    public void removeAura(IElementalAura aura) {
        auras.remove(aura);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        AuraRegistry.AURA_REGISTRY.getEntries().forEach((registeredAura) -> {
            double auraValue = (auras.get(registeredAura.get()) == null) ? 0 : auras.get(registeredAura.get());
            nbt.putDouble(registeredAura.getId().getPath(), auraValue);
        });

        return nbt;
    }

    // TODO: client server value desync issue >:( should be syncing client and server caps anyways
    @Override
    public void deserializeNBT(CompoundTag nbt) {
        AuraRegistry.AURA_REGISTRY.getEntries().forEach((registeredAura) -> {
            double auraValue = nbt.getDouble(registeredAura.getId().getPath());
            if (auraValue != 0) {
                auras.put(registeredAura.get(), auraValue);
            }
        });
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
