package com.numbers.projectaura.render.ui;

import com.numbers.projectaura.animation.Animation;
import com.numbers.projectaura.animation.AnimationComponent;
import com.numbers.projectaura.animation.effects.Effect;
import com.numbers.projectaura.animation.functions.Periodic;
import com.numbers.projectaura.auras.IElementalAura;
import com.numbers.projectaura.registries.AuraRegistry;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.PackagePrivate;

import java.util.ArrayList;

@Builder
public class AuraIcon {

    @Builder.Default
    private IElementalAura auraType = AuraRegistry.FIRE.get();
    @Getter @Builder.Default
    private ArrayList<Effect> effects = new ArrayList<>();
    @Builder.Default
    private Animation fadeAnimation = new Animation(false)
            .addComponent(
                    new AnimationComponent(
                            new Periodic.PeriodicAnimation(
                                    Periodic.SINE,
                                    600,
                                    100,
                                    99999
                            ),
                            99999
                    )
            ).setDuration(99999);

    @PackagePrivate
    private boolean removalFlag = false;
    private long removalTimestamp = 0;
    private long removalDelay = 0;

    public void markForRemoval() {
        this.removalFlag = true;
    }

    public void markForRemovalWithDelay(long delay) {
        this.removalFlag = true;
        this.removalTimestamp = System.currentTimeMillis();
        this.removalDelay = delay;
    }
    public boolean shouldRemove() {

        if (!removalFlag) {
            return false;
        }

        if (System.currentTimeMillis() - this.removalTimestamp > this.removalDelay) {
            return true;
        }

        return false;
    }

    public void startFadeAnimation() {
        if (!this.fadeAnimation.isActive())
            this.fadeAnimation.start();
    }

    public void cancelFadeAnimation() {
        if (this.fadeAnimation.isActive())
            this.fadeAnimation.cancel();
    }
    //TODO: getEffect() that removes effect if its done

    public int getAlpha() {
        if (!this.fadeAnimation.isActive())
            return 255;

        return Math.round(this.fadeAnimation.getComponentValue(0) + 150);
    }

}
