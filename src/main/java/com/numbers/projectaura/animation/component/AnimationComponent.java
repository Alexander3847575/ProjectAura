package com.numbers.projectaura.animation.component;

import com.numbers.projectaura.animation.Eases;
import lombok.Getter;
import lombok.Setter;

/**
 * This class represents a generic animation.
 */
public class AnimationComponent {

    private Eases.Ease ease;
    @Getter
    private long componentDuration;
    private long componentDelay;
    @Getter
    private long absoluteComponentDuration;
    @Getter @Setter
    private boolean active;


    private float lastState;

    public AnimationComponent(Eases.Ease ease, long componentDelay) {

        this.ease = ease;
        this.lastState = ease.getAt(0); // Set the starting state of the function to the initial state of the interpolation
        this.componentDuration = (long) ease.getDuration();
        this.componentDelay = componentDelay;
        this.absoluteComponentDuration = componentDuration + componentDelay;
        this.setActive(true);

    }

    public AnimationComponent(Eases.Ease ease) {

        this(ease,  0L);

    }

    public float getLastState(long dt) {

        // If this component is not active, return the last known state
        if (!this.isActive()) {
            return this.lastState;
        }

        // If this component has exceeded its specified duration, return
        if (dt > (this.absoluteComponentDuration)) {
            this.setActive(false);
            return this.lastState; // This should be the final state of the animation
        }

        // If this component hasn't passed its specified delay, return
        if (dt < this.componentDelay) {
            return this.lastState; // This should be the initial state of the animation
        }

        this.lastState = this.ease.getAt(dt - componentDelay);
        return this.lastState;

    }

    public void start() {
        this.setActive(true);
    }


}