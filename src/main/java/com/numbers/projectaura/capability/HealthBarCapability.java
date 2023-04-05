package com.numbers.projectaura.capability;

import com.numbers.projectaura.ProjectAura;
import com.numbers.projectaura.animation.Animation;
import com.numbers.projectaura.animation.Eases;
import com.numbers.projectaura.animation.component.AnimationComponent;
import com.numbers.projectaura.auras.IElementalAura;
import com.numbers.projectaura.event.EventHandler;
import com.numbers.projectaura.registries.CapabilityRegistry;
import lombok.Getter;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * This capability essentially acts as an interface between the entity (or rather, what happens to its health) and its health bar.
 * It is primarily used for calculations related to the buffer, which requires data tied to an entity that cannot be done in its renderer.
 *
 * TODO: See if all this can be moved to a client-side only type of thing.
 */
public class HealthBarCapability {

    // Constants
    public static final ResourceLocation ID = new ResourceLocation(ProjectAura.MOD_ID, "health_bar_capability");
    public static final int BASE_COLOR = 0xff000000 | 255 << 16 | 224 << 8 | 87;
    public static final int WHITE = 0xff000000 | 255 << 16 | 255 << 8 | 255;
    private static final long DECAY_DELAY = 500L;
    private static final long EASE_DURATION = 750L; // Duration of animation in milliseconds
    private static final long FLASH_DURATION = 500L;

    @Getter
    public float healthPercent;
    private float prevHealth = 0;
    @Getter
    public float bufferPos = 100; //represents the percentage of health the buffer is at
    @Getter
    public int bufferColor;
    @Getter
    public int bufferAlpha;

    // Aura, aura value, animation id
    public LinkedHashMap<IElementalAura, Tuple<Double, Integer>> auraRenderQueue = new LinkedHashMap<>();


    private final Animation bufferAnimation = new Animation()
            // Alpha
            .addComponent(
                    new AnimationComponent(
                            new Eases.Ease(
                                    Eases.CUBIC_EASE_IN,
                                    255,
                                    -255,
                                    EASE_DURATION
                        )
                    )
            )
            // Color
            .addComponent(
                    new AnimationComponent(
                            new Eases.Ease(
                                    Eases.COLOR_CUBIC_EASE_IN,
                                    WHITE,
                                    BASE_COLOR,
                                    FLASH_DURATION
                            )
                    )
            )
            .setDuration(EASE_DURATION)
            .onFinish(() -> {
                this.bufferPos = this.healthPercent;
            });


    private final long ICON_APPLY_ANIMATION_DURATION = 250L;
    private final Function<Integer, Animation> auraIconApplyAnimation = (animationId) -> new Animation()
            // Scale
            .addComponent(
                    new AnimationComponent(
                            new Eases.Ease(
                                    Eases.CUBIC_EASE_OUT,
                                    1,
                                    2f, // 2.5x in size (scale of 1 -> 2.5 (1+1.5))
                                    ICON_APPLY_ANIMATION_DURATION - 50
                            ),
                            50
                    )
            )
            // Alpha
            .addComponent(
                    new AnimationComponent(
                            new Eases.Ease(
                                    Eases.LINEAR_EASE,
                                    255,
                                    -255,
                                    ICON_APPLY_ANIMATION_DURATION - 50
                            ),
                            50
                    )
            )
            .setDuration(ICON_APPLY_ANIMATION_DURATION);

    public HashMap<Integer, Animation> auraApplyAnimations = new HashMap<>();
    private int animationId = 0;

    /**
     * Called on LivingEntityTickEvent in {@link EventHandler}. Client side only.
     * Used for health calculations to avoid doing it on every client frame instead.
     * In addition, it detects changes in health, so it can trigger the buffer animation, and eventually damage indicators.
     * @param entity Entity being ticked
     */
    public void tick(LivingEntity entity) {
        this.tickHealth(entity);
        this.tickAuras(entity);

    }

    private void tickHealth(LivingEntity entity) {
        float health = entity.getHealth();

        // No need to update healthRatios if it's the same, this way we trade a simple check and some variables for div operation and such?
        if (health == this.prevHealth) {
            return;
        }

        // There are scenarios in vanilla where the current health can temporarily exceed the max health
        float maxHealth = Math.max(entity.getHealth(), entity.getMaxHealth());
        this.healthPercent = (health / maxHealth) * 100; // Update the % health of the entity

        float damageTaken = this.prevHealth - health;

        if (damageTaken > 0) {
            this.bufferAnimation.start();
        } else { // We know that damageTaken cannot be 0 because of the previous guard statement
            this.bufferPos = this.healthPercent; // Move buffer to match health, mostly in the case of healing
        }

        this.prevHealth = health;

        //TODO: Reference ToroHealthBar to start creating the elemental damage indicators here?
        //  dev mods to reduce or eliminate build time
    }

    public void tickAuras(LivingEntity entity) {

        AuraCapability auraCapability = CapabilityRegistry.getCapability(entity, CapabilityRegistry.AURA_CAPABILITY);
        assert auraCapability != null; // All living entities should have this capability

        Iterator<Map.Entry<IElementalAura, Double>> iterator = auraCapability.getAuras().entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<IElementalAura, Double> aura = iterator.next();

            // When the entity receives a new aura
            if (!this.auraRenderQueue.containsKey(aura.getKey())) {

                this.auraRenderQueue.put(aura.getKey(), new Tuple<>(aura.getValue(), this.animationId)); // Add the aura to the render queue
                this.auraApplyAnimations.put(this.animationId, auraIconApplyAnimation.apply(this.animationId)); // Add a new animation to the list and increment the animation id
                this.auraApplyAnimations.get(this.animationId).start();
                this.animationId++;

                return;

            }

            // Update existing auras
            Tuple<Double, Integer> tempVal = this.auraRenderQueue.get(aura.getKey());
            // If the aura refreshed restart the animation
            if (aura.getValue() > tempVal.getA()) {
                if (this.auraApplyAnimations.get(tempVal.getB()) != null)
                    this.auraApplyAnimations.get(tempVal.getB()).start();
            }
            this.auraRenderQueue.replace(aura.getKey(), new Tuple<>(aura.getValue(), tempVal.getB()));


        }

        // Remove auras that don't exist anymore
        Iterator<IElementalAura> iterator2 = this.auraRenderQueue.keySet().iterator();
        while (iterator2.hasNext()) {
            IElementalAura aura = iterator2.next();
            if (!auraCapability.getAuras().containsKey(aura)) {
                this.auraApplyAnimations.remove(auraRenderQueue.get(aura).getB()); // Remove this auras animation as well
                iterator2.remove();
            }
        }

    }

    public void tick() {
        this.tickBuffer();
        //this.tickAnimations();
    }

    // CALLED EVERY CLIENT FRAME
    // Updates buffer position, color, alpha
    // Decided to leave this mechanism in place as opposed to directly/indirectly referencing the animation from the outside
    public void tickBuffer() {

        if (!bufferAnimation.isActive()) {
            return;
        }

        this.bufferAlpha = (int) bufferAnimation.getComponentValue(0);
        this.bufferColor = (int) bufferAnimation.getComponentValue(1);

    }


    /**
     * Gets the blended color of the buffer with the background color to allow for smooth fading of the buffer into the background.
     * @param bgColor The color to blend with, usually the background of the health bar.
     * @return the blended color
     */
    public int getBlendedBufferColor(int bgColor) {
        return blendColors(this.bufferColor, this.bufferAlpha, bgColor);
    }
    public int getBlendedBufferAlpha(int bgAlpha) {
        return Math.round(Eases.CUBIC_EASE_IN.ease(bufferAnimation.getDeltaTime(), this.bufferAlpha, 170 - this.bufferAlpha, EASE_DURATION));
    }

    public static int blendColors(int overlayColor, int overlayAlpha, int bgColor) {

        int ar = (overlayColor >> 16) & 0xFF;
        int ag = (overlayColor >> 8) & 0xFF;
        int ab = overlayColor & 0xFF;

        int br = (bgColor >> 16) & 0xFF;
        int bg = (bgColor >> 8) & 0xFF;
        int bb = bgColor & 0xFF;

        // C implementation >> java one lmao
        /*int cr = (ar * ar + br * (255 - this.bufferAlpha)) / 255;
        int cg = (ag * ag + bg * (255 - this.bufferAlpha)) / 255;
        int cb = (ab * ab + bb * (255 - this.bufferAlpha)) / 255;*/

        int alpha = overlayAlpha + 1;
        int inv_alpha = 256 - overlayAlpha;

        int cr = (alpha * ar + inv_alpha * br) >> 8;
        int cg = (alpha * ag + inv_alpha * bg) >> 8;
        int cb = (alpha * ab + inv_alpha * bb) >> 8;

        return 0xff000000 | cr << 16 | cg << 8 | cb;
    }

    public static class HealthBarCapabilityProvider implements ICapabilityProvider {
        private final LazyOptional<HealthBarCapability> instance = LazyOptional.of(HealthBarCapability::new);

        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
            return CapabilityRegistry.HEALTH_BAR_CAPABILITY.orEmpty(cap, instance.cast());
        }

    }
}
