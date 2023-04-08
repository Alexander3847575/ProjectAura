package com.numbers.projectaura.animation;

import com.numbers.projectaura.animation.functions.Eases;
import com.numbers.projectaura.animation.functions.IAnimationFunction;
import lombok.Getter;
import lombok.Setter;

/**
 * This class represents a generic animation, that wraps around an interpolation function. It handles delays and states.
 */
public class AnimationComponent {

    private IAnimationFunction function;
    @Getter
    private long componentDuration;
    private long componentDelay;
    @Getter
    private long absoluteComponentDuration;
    @Getter @Setter
    private boolean active;

    private float initialState;
    private float lastState;

    public AnimationComponent(IAnimationFunction function, long componentDuration, long componentDelay) {
        this.function = function;
        this.initialState = function.getAt(0); // Set the starting state of the function to the initial state of the interpolation
        this.lastState = initialState;
        this.componentDuration = componentDuration;
        this.componentDelay = componentDelay;
        this.absoluteComponentDuration = componentDuration + componentDelay;
        this.setActive(true);
    }

    public AnimationComponent(IAnimationFunction function, long componentDuration) {
        this(function, componentDuration, 0L);
    }

    public AnimationComponent(Eases.Ease ease, long componentDelay) {
        this(ease, (long) ease.getDuration(), componentDelay);
    }

    public AnimationComponent(Eases.Ease ease) {

        this(ease,  0L);

    }

    /**
     * Gets the state of the component at a certain time. This will deactivate the component if it detects it is past its specified duration.
     * @param dt Time since the beginning of the animation, in milliseconds
     * @return The state of the component, or the point at which the interpolation is.
     */
    public float getState(long dt) {

        // If this component is not active, return the last known state
        if (!this.isActive()) {
            return this.lastState;
        }

        // If this component has exceeded its specified duration, return
        if (dt > (this.absoluteComponentDuration)) {
            this.setActive(false); // So close to a pure function D:
            return this.lastState; // This should be the final state of the animation
        }

        // If this component hasn't passed its specified delay, return
        if (dt < this.componentDelay) {
            return this.initialState; // This should be the initial state of the animation
        }

        this.lastState = this.function.getAt(dt - componentDelay);
        return this.lastState;

    }

    /**
     * Activates the component.
     */
    public void start() {
        this.setActive(true);
    }


}
