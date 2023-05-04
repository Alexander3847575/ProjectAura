package com.numbers.projectaura.capability;

import com.numbers.projectaura.ProjectAura;
import com.numbers.projectaura.auras.IElementalAura;
import com.numbers.projectaura.auras.applicator.ApplicationInstance;
import com.numbers.projectaura.auras.applicator.ApplicationType;
import com.numbers.projectaura.event.ElementalReactionEvent;
import com.numbers.projectaura.network.ClientBoundAuraSyncMessage;
import com.numbers.projectaura.network.NetworkHandler;
import com.numbers.projectaura.reactions.IElementalReaction;
import com.numbers.projectaura.reactions.ReactionData;
import com.numbers.projectaura.registries.AuraRegistry;
import com.numbers.projectaura.registries.EnvironmentalApplicatorRegistry;
import com.numbers.projectaura.registries.ReactionRegistry;
import lombok.Getter;
import lombok.Setter;
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class AuraCapability implements INBTSerializable<CompoundTag> {

    public static ResourceLocation ID = new ResourceLocation(ProjectAura.MOD_ID, "aura_capability");
    private LivingEntity attachedEntity;
    @Getter @Setter
    private LinkedHashMap<IElementalAura, Double> auras = new LinkedHashMap<>();
    private HashMap<Integer, HashMap<String, Integer>> applicationCooldownRecord = new HashMap<>(); // Stores entity ID's that cor

    public AuraCapability(LivingEntity attachedEntity) {
        this.attachedEntity = attachedEntity;
    }

    /**
     * Here is where the reaction logic takes place; upon the application of a new aura
     * This method will then call helper methods that deal with the effects of the reactions themselves
     * @param applicationInstance
     */
    public boolean tryApplyAura(ApplicationInstance applicationInstance) {
        // TODO: make a version with a bool arg for constant applications so that things like rain or an energy beam don't show apply animation on cooldown, but rather refresh seamlessly. It'll hook into the event.
        // Server handles all reaction stuff
        if (this.attachedEntity.level.isClientSide()) {
            return false;
        }

        ApplicationType applicationType = applicationInstance.applicationType();
        int entityId = applicationInstance.applicationSource().sourceId();

        // Check if this applicator is in the list of applicators on cooldown
        HashMap<String, Integer> applicationTypeList = this.applicationCooldownRecord.get(entityId);
        if (applicationTypeList != null) {

            // Check to see if the application type is in the list of cooldowns for that entity
            Integer timestamp = applicationTypeList.get(applicationType.applicatorId());
            //ProjectAura.LOGGER.debug("Delta time: " + String.valueOf(this.attachedEntity.tickCount - this.applicationCooldownRecord.get(entityId).get(applicationType.applicatorId())));
            if (timestamp != null) {

                // Check to see if the application type is still on cooldown
                if (this.attachedEntity.tickCount - timestamp < applicationType.applicatorCooldown()) {

                    //ProjectAura.LOGGER.debug("Applicator still on cooldown for " + applicationType.applicatorId() + " on " + attachedEntity.getName().getString());

                    // Even if the auras don't change, still deal the damage
                    if (applicationInstance.damage() != 0)
                        this.attachedEntity.hurt(applicationInstance.applicationSource().damageSource().get(), applicationInstance.damage());
                    return false; // Application failed
                }

                //ProjectAura.LOGGER.debug("Applicator past cooldown for " + applicationType.applicatorId() + " on " + attachedEntity.getName().getString());

                // TODO: this is basically a memory leak; garbage collect the stuff before checking here maybe?
                /*// If it has finished its cooldown, remove the entry and continue
                this.applicationCooldownRecord.get(entityId).remove(applicationType.applicatorId());

                // If that was the last cooldown on record, remove the entity altogether
                if (this.applicationCooldownRecord.get(entityId).size() == 0)
                    this.applicationCooldownRecord.remove(entityId);*/

            } else {
                //ProjectAura.LOGGER.debug("No timestamp found for damage type " + applicationType.applicatorId() + " for " + attachedEntity.getName().getString());
            }

        } else {

            //ProjectAura.LOGGER.debug("No prior entry found for " + applicationType.applicatorId() + " on " + attachedEntity.getName().getString());
            // If the entity doesn't have an entry, then put one in
            this.applicationCooldownRecord.put(entityId, new HashMap<>());
        }

        //ProjectAura.LOGGER.debug("Cooldown did not take effect for " + applicationType.applicatorId() + " on " + attachedEntity.getName().getString());
        // Start new cooldown
        this.applicationCooldownRecord.get(entityId).put(applicationType.applicatorId(), this.attachedEntity.tickCount);


        //ProjectAura.LOGGER.debug(this.applicationCooldownRecord.toString());
        //ProjectAura.LOGGER.debug(String.valueOf(System.currentTimeMillis() - this.applicationCooldownRecord.get(entityId).get(applicationType.applicatorId())));

        IElementalAura appliedAuraType = applicationType.applicationType();
        double remainingAura = applicationInstance.applicationStrength();
        float damage = applicationInstance.damage();

        // Cycle through all currently applied auras, avoiding concurrent modification
        Iterator<Map.Entry<IElementalAura, Double>> iterator = auras.entrySet().iterator();
        while (iterator.hasNext()) {

            Map.Entry<IElementalAura, Double> aura = iterator.next();

            // Attempt a reaction
            IElementalReaction<?, ?> reaction = ReactionRegistry.getReaction(appliedAuraType, aura.getKey());
            if (reaction == null){
                continue;
            }

            // React
            ReactionData result = reaction.react(ReactionData.builder()
                            .appliedAura(appliedAuraType)
                            .baseAura(aura.getKey())
                            .inputAppliedStrength(remainingAura)
                            .inputBaseStrength(aura.getValue())
                            .inputDamage(applicationInstance.damage())
                            .target(this.attachedEntity)
                            .reaction(reaction)
                            .build());

            // No need to update aura values or anything if the reaction failed
            if (result.isFailed()) {
                continue;
            }

            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new ElementalReactionEvent(this.attachedEntity, result));

            damage = result.getOutputDamage();
            double outputBaseStrength = result.getOutputBaseStrength();
            double outputAppliedStrength = result.getOutputAppliedStrength();

            if (outputBaseStrength < 0.1d) {
                // Immediately remove the base aura if it has run out
                iterator.remove();
            } else {
                // Update the base aura
                aura.setValue(outputBaseStrength);
            }

            remainingAura = outputAppliedStrength;

            if (outputAppliedStrength < 0.1d) {
                // Stop the reaction checks if there is no more aura to react
                break;
            }

        }

        // TODO: properly handle dealing elemental damage as a new damage type
        if (damage != 0)
            this.attachedEntity.hurt(applicationInstance.applicationSource().damageSource().get(), damage);

        // Add remainder to the entity auras
        auras.put(appliedAuraType, remainingAura);

        return true;

    }

    /**
     * Auras are only ticked on the server
     */
    public void tick() {

        AtomicBoolean updated = new AtomicBoolean(false);

        // Avoid concurrent modification
        Iterator<Map.Entry<IElementalAura, Double>> iterator = auras.entrySet().iterator();

        // Auras decay by 0.1 each tick
        while (iterator.hasNext()) {
            Map.Entry<IElementalAura, Double> aura = iterator.next();
            double value = aura.getValue();

            if (value >= 0.1d) {
                value -= 0.1d;
                aura.setValue(value);
            } else {
                iterator.remove();
            }

            updated.set(true);

        }

        // Check and apply environmental applicators
        EnvironmentalApplicatorRegistry.ENVIRONMENTAL_APPLICATOR_REGISTRY.getEntries().forEach((entry) -> {
            ApplicationInstance applicationInstance = entry.get().test(this.attachedEntity);
            if (applicationInstance != null) {
                if(this.tryApplyAura(applicationInstance))
                    updated.set(true);
            }
        });

        // Don't send packets if nothing was updated lol
        if (!updated.get()) {
            return;
        }

        // Update client
        sendAuraUpdatePackets(this.attachedEntity);

    }

    /**
     * Sends an update packet to all clients tracking the entity.
     */
    private void sendAuraUpdatePackets(LivingEntity entity) {
        NetworkHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), new ClientBoundAuraSyncMessage(entity, this.auras));

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
        private final LazyOptional<AuraCapability> instance;

        public AuraCapabilityProvider(LivingEntity attachedEntity) {
            instance = LazyOptional.of(() -> new AuraCapability(attachedEntity));
        }

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
            return CapabilityHandler.AURA_CAPABILITY.orEmpty(cap, instance.cast());
        }

    }

}
