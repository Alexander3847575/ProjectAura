package com.numbers.projectaura.capability;

import com.numbers.projectaura.ProjectAura;
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

    public static ResourceLocation ID = new ResourceLocation(ProjectAura.MOD_ID, "health_bar_capability");

    @Getter
    public float bufferPos = 100; //represents the percentage of health the buffer is at
    @Getter
    public int bufferColor;
    @Getter
    public int bufferAlpha;
    @Getter
    public int blendedBufferAlpha;
    public static final int baseColor = 0xff000000 | 255 << 16 | 224 << 8 | 87;
    public static final int white = 0xff000000 | 255 << 16 | 255 << 8 | 255;
    private float bufferFrom = 100;
    private float bufferTo = 0;
    @Getter
    public float healthPercent;
    private float prevHealth = 0;
    private long animationTimestamp;
    private boolean active = true;
    private final static long decayDelay = 500L;
    private final static long bufferEaseDuration = 750L; // Duration of animation in milliseconds
    private final static long flashDuration = 500L;

    /**
     * Called on LivingEntityTickEvent in {@link com.numbers.projectaura.event.ServerEventHandler}. Client side only.
     * Used for health calculations to avoid doing it on every client frame instead.
     * In addition, it detects changes in health so it can trigger the buffer animation, and eventually damage indicators.
     * @param entity Entity being ticked
     */
    public void tick(LivingEntity entity) {

        // Only need to do these calcs on client
        if (!entity.level.isClientSide()) {
            return;
        }

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
            startBufferAnimation();
        } else { // We know that damageTaken cannot be 0 because of the previous guard statement
            this.bufferPos = this.healthPercent; // Move buffer to match health, mostly in the case of healing
        }

        this.prevHealth = health;

        //TODO: Reference ToroHealthBar to start creating the elemental damage indicators here?
        //  dev mods to reduce or eliminate build time

    }

    // CALLED EVERY CLIENT FRAME
    // Updates buffer position, color, alpha
    public void tickBuffer() {

        // Important to avoid as much processing as possible on idle frames, as well as the dt getting crazy big
        if (!this.active) {
            return;
        }

        // Delta time, or how much time has passed since the animation started
        long dt = System.currentTimeMillis() - this.animationTimestamp;

        if (dt < bufferEaseDuration) {

            // swap these to switch between cubic easing and fade out mode
            // TODO: config

            //this.bufferPos = cubicEaseIn(dt, this.bufferFrom, this.bufferTo, bufferEaseDuration);
            this.bufferAlpha = Math.round(cubicEaseIn(dt, 255, -255, bufferEaseDuration));
            this.blendedBufferAlpha = Math.round(cubicEaseIn(dt, this.bufferAlpha, 170 - this.bufferAlpha, bufferEaseDuration));

            if (dt < flashDuration) {
                this.bufferColor = cubicEaseInColor(dt, white, baseColor, flashDuration);
            }
        } else {
            // Reset all variables to the starting values for the next buffer update, and unset the active flag
            this.bufferPos = this.healthPercent;
            this.bufferColor = white;
            this.bufferAlpha = 0;
            this.active = false;
        }

    }

    /**
     * Initiates buffer animation by setting the timestamp to track delta T, the goals (params) for easing functions, and the active flag.
     */
    public void startBufferAnimation() {
        this.bufferFrom = this.bufferPos;
        this.bufferTo = this.healthPercent - this.bufferPos;
        this.animationTimestamp = System.currentTimeMillis();
        this.active = true;
    }

    // A ton of math stuff below here

    /**
     * Penner cubic easing.
     * @param t Delta time (ms)
     * @param b Begin, or the y-offset
     * @param c Change (delta of function, NOT the function result @ d)
     * @param d Duration of change (ms)
     * @return
     */
    public static float cubicEaseIn(float t, float b , float c, float d) {
        return c*(t/=d)*t*t + b;
    }

    public static float  expoEaseIn(float t,float b , float c, float d) {
        return (t==0) ? b : c * (float)Math.pow(2, 10 * (t/d - 1)) + b;
    }
    // lerp stnads for linear interpolation :D
    public static float lerp (float t, float b , float c, float d) {
        return c*t/d + b;
    }

    // Lazy RGB space color interpolation
    public static int lerpColor(float t, int b , int c, float d) {

        final int ar = (b >> 16) & 0xFF;
        final int ag = (b >> 8) & 0xFF;
        final int ab = b & 0xFF;

        final int br = (c >> 16) & 0xFF;
        final int bg = (c >> 8) & 0xFF;
        final int bb = c & 0xFF;

        return 0xff000000 | Math.round(ar + (br - ar) * t/d) << 16 | Math.round(ag + (bg - ag) * t/d) << 8 | Math.round(ab + (bb - ab) * t/d);

    }

    // Lazy RGB space color interpolation but cubic
    public static int cubicEaseInColor(float t, int b , int c, float d) {

        final int ar = (b >> 16) & 0xFF;
        final int ag = (b >> 8) & 0xFF;
        final int ab = b & 0xFF;

        final int br = (c >> 16) & 0xFF;
        final int bg = (c >> 8) & 0xFF;
        final int bb = c & 0xFF;

        return 0xff000000 | Math.round(cubicEaseIn(t, ar, br - ar, d)) << 16 | Math.round(cubicEaseIn(t, ag, bg - ag, d)) << 8 | Math.round(cubicEaseIn(t, ab, bb - ab, d));

    }


    /**
     * Blends the current color of the buffer with the background color to allow for smooth fading of the buffer into the background in conjuction with the alpha blend carried out in {@code tickBuffer()}.
     * @param bgColor The color to blend with, usually the background of the health bar.
     * @return the blended color
     */
    public int getBlendedBufferColor(int bgColor) {

        int ar = (this.bufferColor >> 16) & 0xFF;
        int ag = (this.bufferColor >> 8) & 0xFF;
        int ab = this.bufferColor & 0xFF;

        int br = (bgColor >> 16) & 0xFF;
        int bg = (bgColor >> 8) & 0xFF;
        int bb = bgColor & 0xFF;

        // C implementation >> java one lmao
        /*int cr = (ar * ar + br * (255 - this.bufferAlpha)) / 255;
        int cg = (ag * ag + bg * (255 - this.bufferAlpha)) / 255;
        int cb = (ab * ab + bb * (255 - this.bufferAlpha)) / 255;*/

        int alpha = this.bufferAlpha + 1;
        int inv_alpha = 256 - this.bufferAlpha;

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
