package com.numbers.projectaura.capability;

import com.numbers.projectaura.ProjectAura;
import com.numbers.projectaura.registries.CapabilityRegistry;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/***
 * This capability essentially acts as an interface between the entity (or rather, what happens to its health) and its health bar.
 */
public class HealthBarCapability implements INBTSerializable<CompoundTag> {

    public static ResourceLocation ID = new ResourceLocation(ProjectAura.MOD_ID, "health_bar_capability");

    public HealthBarCapability() {

    }

    @Getter
    public float bufferPos = 100; //represents the percentage of health the buffer is at
    @Getter
    public float healthPercent;
    private float prevHealth = 0;
    private int decayDelayTimer = 0;
    private long animationTimer;
    private boolean active = true;
    private float bufferFrom = 100;
    private float bufferTo = 0;
    private long animationDuration = 2000L; // Duration of animation in milliseconds



    public void tick(LivingEntity entity) {
        float health = entity.getHealth();

        // No need to update healthRatios if its the same, this way we trade a simple check and some variables for div operation and such?
        if (health != prevHealth) {
            float maxHealth = Math.max(entity.getHealth(), entity.getMaxHealth());
            this.healthPercent = (health / maxHealth) * 100;
        }

        this.prevHealth = health;

    }

    // CALLED EVERY CLIENT FRAME
    // Updates the buffer position
    public void tickBufferPos() {

        if (!active) {
            return;
        }

        long dt = System.currentTimeMillis() - animationTimer;

        if (dt < animationDuration) {
            this.bufferPos = easeIn(dt, bufferFrom, bufferTo, animationDuration);
        } else {
            //active = false;
        }

        // Legacy linear buffer thing
        /*if (bufferPos > healthPercent) {

            if (decayDelayTimer > 0) {
                decayDelayTimer--;
            } else {

                // avoid health bar stuttering under then back
                if ((bufferPos - 1.5) < healthPercent) {
                    bufferPos = healthPercent;
                } else {
                    bufferPos -= 1.5;
                }

            }

        }*/

    }

    public void addDecayDelay(int ticks) {
        this.decayDelayTimer += ticks;
    }

    public void startBufferAnimation() {
        this.bufferFrom = bufferPos;
        this.bufferTo = healthPercent;
        this.animationTimer = System.currentTimeMillis();
        this.active = true;
    }

    /***
     * Penner cubic easing.
     * @param t Delta time (ms)
     * @param b Begin, or the y-offset
     * @param c Change
     * @param d Duration of change (ms)
     * @return
     */
    public static float easeIn (float t,float b , float c, float d) {
        return c*(t/=d)*t*t + b;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();

        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {

    }

    public static class HealthBarCapabilityProvider implements ICapabilityProvider, ICapabilitySerializable<CompoundTag>
    {
        private final LazyOptional<HealthBarCapability> instance = LazyOptional.of(HealthBarCapability::new);

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
            return CapabilityRegistry.HEALTH_BAR_CAPABILITY.orEmpty(cap, instance.cast());
        }

    }
}
