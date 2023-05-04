package com.numbers.projectaura.capability;

import com.numbers.projectaura.ProjectAura;
import com.numbers.projectaura.animation.Animation;
import com.numbers.projectaura.animation.AnimationComponent;
import com.numbers.projectaura.animation.effects.Effect;
import com.numbers.projectaura.animation.effects.ExpandAndFadeEffect;
import com.numbers.projectaura.animation.functions.Eases;
import com.numbers.projectaura.auras.IElementalAura;
import com.numbers.projectaura.event.ElementalReactionEvent;
import com.numbers.projectaura.event.EventHandler;
import com.numbers.projectaura.render.RenderUtil;
import com.numbers.projectaura.ui.AuraIcon;
import com.numbers.projectaura.ui.DamageNumberParticle;
import lombok.Getter;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Slime;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * This capability essentially acts as an interface between the entity (or rather, what happens to its health) and its health bar.
 * It is primarily used for calculations related to the buffer, which requires data tied to an entity that cannot be done in its renderer.*
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


    private static final long ICON_APPLY_ANIMATION_DURATION = 250L;
    private static final Function<ResourceLocation, ExpandAndFadeEffect> IconApplyEffect = (texture) -> new ExpandAndFadeEffect(
             texture,
             16f,
             2f,
             ICON_APPLY_ANIMATION_DURATION,
             50L,
             WHITE,
             255,
             0xF000F0
     );

    private static final long REACTION_ANIMATION_DURATION = 400L;
    private static final BiFunction<ResourceLocation, Integer, ExpandAndFadeEffect> REACTION_ICON_EFFECT = (texture, color) -> new ExpandAndFadeEffect(
            texture,
            32f,
             -0.5f,
            REACTION_ANIMATION_DURATION,
            50L,
            new AnimationComponent(new Eases.Ease(Eases.COLOR_CUBIC_EASE_IN, WHITE, color, ICON_APPLY_ANIMATION_DURATION), 0),
            255,
            0xF000F0
    );

    private static final ResourceLocation REACTION_SHOCK_TEXTURE = new ResourceLocation(ProjectAura.MOD_ID, "textures/ui/ring_effect.png");
    private static final Supplier<ExpandAndFadeEffect> ReactionShockEffect = () -> new ExpandAndFadeEffect(
            REACTION_SHOCK_TEXTURE,
            16f,
            2f,
            REACTION_ANIMATION_DURATION,
            0,
            WHITE,
            255,
            0xF000F0
    );

    private static final DecimalFormat DF = new DecimalFormat("#.##");


    // Aura, aura value, animation id
    public LinkedHashMap<IElementalAura, AuraIcon> iconRenderQueue = new LinkedHashMap<>();
    public LinkedHashMap<IElementalAura, Double> internalAuraBuffer = new LinkedHashMap<>();

    private LivingEntity attachedEntity;
    private boolean showDamageIndicators;


    public HealthBarCapability(LivingEntity attachedEntity) {
        this.attachedEntity = attachedEntity;
        this.prevHealth = Math.min(attachedEntity.getHealth(), attachedEntity.getMaxHealth());
        this.updateHealthValues(); // This is done so that the entity will not display damage particles when initially loaded
        this.showDamageIndicators = !((attachedEntity instanceof Slime) && ((Slime) attachedEntity).getSize() == 1);
    }

    /**
     * Called on LivingEntityTickEvent in {@link EventHandler}. Client side only.
     * Used for health calculations to avoid doing it on every client frame instead.
     * In addition, it detects changes in health, so it can trigger the buffer animation, and eventually damage indicators.
     * @param entity Entity being ticked
     */
    public void tick() {
        this.tickHealth();
        this.tickAuras();

    }

    private void tickHealth() {

        float health = attachedEntity.getHealth();

        // No need to update healthRatios if it's the same, this way we trade a simple check and some variables for div operation and such?
        if (health == this.prevHealth) {
            return;
        }

        float damageTaken = this.updateHealthValues();

        //TODO: Move to mixin to get true damage; use flags and stored data in the capability to determine elemental damage type
        // (because the hurt method is called immediately when elemental damage is applied, you can pretty much guarantee that the flag will be accurate, an then unset it immediately after spawning the particle).
        if (damageTaken != 0 && this.showDamageIndicators)
            attachedEntity.level.addParticle(new DamageNumberParticle.DamageParticleOptions(String.valueOf(DF.format(damageTaken)), WHITE, false), attachedEntity.getX(), attachedEntity.getY() + attachedEntity.getBbHeight()/2, attachedEntity.getZ(), 0, 0, 0);

    }

    private float updateHealthValues() {

        float health = this.attachedEntity.getHealth();

        // There are scenarios in vanilla where the current health can temporarily exceed the max health
        float maxHealth = Math.max(this.attachedEntity.getHealth(), this.attachedEntity.getMaxHealth());
        this.healthPercent = (health / maxHealth) * 100; // Update the % health of the entity

        float damageTaken = this.prevHealth - health;

        if (damageTaken > 0) {
            this.bufferAnimation.start();
        } else { // We know that damageTaken cannot be 0 because of the previous guard statement
            this.bufferPos = this.healthPercent; // Move buffer to match health, mostly in the case of healing
        }

        this.prevHealth = health;

        return damageTaken;

    }

    public void tickAuras() {

        AuraCapability auraCapability = CapabilityHandler.getCapability(this.attachedEntity, CapabilityHandler.AURA_CAPABILITY);
        assert auraCapability != null; // All living entities should have this capability

        Iterator<Map.Entry<IElementalAura, Double>> auraCapabilityIterator = auraCapability.getAuras().entrySet().iterator();
        while (auraCapabilityIterator.hasNext()) {
            Map.Entry<IElementalAura, Double> aura = auraCapabilityIterator.next();

            // When the entity receives a new aura
            if (!this.internalAuraBuffer.containsKey(aura.getKey())) {

                ArrayList<Effect> effects = new ArrayList<>();
                effects.add(IconApplyEffect.apply(aura.getKey().getIcon()));
                effects.get(0).start();

                AuraIcon icon = AuraIcon.builder()
                        .auraType(aura.getKey())
                        .effects(effects)
                        .build();

                this.internalAuraBuffer.put(aura.getKey(), aura.getValue());
                this.iconRenderQueue.put(aura.getKey(), icon); // Add the aura to the render queue

                return;

            }

            // Update existing auras
            double tempVal = this.internalAuraBuffer.get(aura.getKey());
            // If the aura refreshed restart the animation
            if (aura.getValue() > tempVal) {

                if (this.iconRenderQueue.get(aura.getKey()) != null) {

                    AuraIcon icon = this.iconRenderQueue.get(aura.getKey());
                    icon.getEffects().get(0).start(); // Start apply animation
                    icon.cancelFadeAnimation();

                }

            }

            // Start fade animation when the aura has less than 2 seconds remaining
            if (aura.getValue() < 4.0d) {
                this.iconRenderQueue.get(aura.getKey()).startFadeAnimation();
            }

            this.internalAuraBuffer.replace(aura.getKey(), aura.getValue());


        }

        // Remove auras that don't exist anymore
        Iterator<IElementalAura> auraBufferIterator = this.internalAuraBuffer.keySet().iterator();
        while (auraBufferIterator.hasNext()) {
            IElementalAura aura = auraBufferIterator.next();
            if (!auraCapability.getAuras().containsKey(aura)) {
                auraBufferIterator.remove();
                this.iconRenderQueue.get(aura).markForRemoval();
            }
        }

        Iterator<Map.Entry<IElementalAura, AuraIcon>> iconIterator = this.iconRenderQueue.entrySet().iterator();
        while (iconIterator.hasNext()) {
            Map.Entry<IElementalAura, AuraIcon> auraIcon = iconIterator.next();
            if (!this.internalAuraBuffer.containsKey(auraIcon.getKey()) && auraIcon.getValue().shouldRemove()) {
                iconIterator.remove();
            }
        }

    }

    public void onReaction(ElementalReactionEvent event) {

        final IElementalAura baseAura = event.getReactionData().getBaseAura();
        final AuraIcon baseIcon = this.iconRenderQueue.get(baseAura);

        if (baseIcon == null) {
            return;
        }

        // TODO: Ideally these base animations wouldn't be inside the effects array..
        baseIcon.getEffects().get(0).cancel(); // Cancel apply animation
        baseIcon.cancelFadeAnimation();
        baseIcon.getEffects().add(ReactionShockEffect.get());
        baseIcon.getEffects().add(REACTION_ICON_EFFECT.apply(baseAura.getIcon(), baseAura.getColor()));
        baseIcon.markForRemovalWithDelay(500L);

        final IElementalAura appliedAura = event.getReactionData().getAppliedAura();
        final AuraIcon appliedIcon = makeIcon(appliedAura);

        appliedIcon.getEffects().get(0).cancel(); // Cancel apply animation
        appliedIcon.getEffects().add(ReactionShockEffect.get());
        appliedIcon.getEffects().add(REACTION_ICON_EFFECT.apply(appliedAura.getIcon(), appliedAura.getColor()));
        appliedIcon.markForRemovalWithDelay(500L);

        this.iconRenderQueue.put(event.getReactionData().getAppliedAura(), appliedIcon);

    }

    public static AuraIcon makeIcon(IElementalAura aura) {
        ArrayList<Effect> effects = new ArrayList<>();
        effects.add(IconApplyEffect.apply(aura.getIcon()));

        AuraIcon icon = AuraIcon.builder()
                .auraType(aura)
                .effects(effects)
                .build();

        return icon;
    }

    public void removeIcon(AuraIcon aura) {
        this.iconRenderQueue.remove(aura);
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
        return RenderUtil.blendColors(this.bufferColor, this.bufferAlpha, bgColor);
    }
    public int getBlendedBufferAlpha(int bgAlpha) {
        return Math.round(Eases.CUBIC_EASE_IN.ease(bufferAnimation.getDeltaTime(), this.bufferAlpha, bgAlpha - this.bufferAlpha, EASE_DURATION));
    }

    public static class HealthBarCapabilityProvider implements ICapabilityProvider {
        private final LazyOptional<HealthBarCapability> instance;

        public HealthBarCapabilityProvider(LivingEntity attachedEntity) {
            this.instance = LazyOptional.of(() -> new HealthBarCapability(attachedEntity));
        }

        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
            return CapabilityHandler.HEALTH_BAR_CAPABILITY.orEmpty(cap, instance.cast());
        }

    }
}
