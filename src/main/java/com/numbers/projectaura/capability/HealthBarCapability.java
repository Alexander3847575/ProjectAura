package com.numbers.projectaura.capability;

import com.numbers.projectaura.ProjectAura;
import com.numbers.projectaura.animation.Animation;
import com.numbers.projectaura.animation.Eases;
import com.numbers.projectaura.animation.component.AnimationComponent;
import com.numbers.projectaura.event.EventHandler;
import com.numbers.projectaura.registries.CapabilityRegistry;
import lombok.Getter;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    private final Animation bufferAnimation = new Animation()
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

    /**
     * Called on LivingEntityTickEvent in {@link EventHandler}. Client side only.
     * Used for health calculations to avoid doing it on every client frame instead.
     * In addition, it detects changes in health, so it can trigger the buffer animation, and eventually damage indicators.
     * @param entity Entity being ticked
     */
    public void tick(LivingEntity entity) {

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
