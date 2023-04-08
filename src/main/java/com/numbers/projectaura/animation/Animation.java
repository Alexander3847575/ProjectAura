package com.numbers.projectaura.animation;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

/**
 * Represents an animation, that can have multiple changing components.
 * The animation handles delta time and managing its components.
 */
public class Animation {
    private ArrayList<AnimationComponent> components = new ArrayList<>();

    // System time when the animation began, in milliseconds
    private long animationTimestamp = 0;
    @Getter
    private long animationDuration = 0;
    @Setter(AccessLevel.PRIVATE)
    private boolean active = false;
    private Runnable callback = () -> {};

    /**
     * Starts or restarts the animation, refreshing all components
     */
    public void start() {
        this.animationTimestamp = System.currentTimeMillis();
        this.components.forEach((component -> component.start()));
        this.setActive(true);
    }

    // Value builders

    /**
     * Adds a component to the animation, that can be called with {@code getComponent()} or its value obtained at the current animation time with {@code getComponentValue()}
     * @param component The component to add
     * @return This {@code Animation}, for builder-ing
     */
    public Animation addComponent(AnimationComponent component) {
        this.components.add(component);
        return this;
    }

    /**
     * Sets the duration of this animation, after which it will become inactive.
     * TODO: maybe implement indexing of component delays animation lengths for the default length
     * @param duration The duration of the animation, in milliseconds
     * @return This {@code Animation}, for builder-ing
     */
    public Animation setDuration(long duration) {
        this.animationDuration = duration;
        return this;
    }

    /**
     * Sets a {@link Runnable} callback that runs whenever the animation finishes.
     * The callback is called in {@code isActive()} when it detects that the animation is done.
     * @param callback The Runnable to run
     * @return This {@code Animation}, for builder-ing
     */
    public Animation onFinish(Runnable callback) {
        this.callback = callback;
        return this;
    }

    // Component related
    public AnimationComponent getComponent(int index) {
        return this.components.get(index);
    }

    public float getComponentValue(int index) {
        return this.getComponent(index).getState(this.getDeltaTime());
    }


    public boolean isActive() {
        if (!this.active) {
            return false;
        }
        if (this.getDeltaTime() > this.animationDuration) {
            this.setActive(false);
            callback.run();
            return false;
        }
        return true;
    }

    /**
     * @return The time in milliseconds since the beginning of the animation.
     */
    public long getDeltaTime() {

        return System.currentTimeMillis() - this.animationTimestamp;

    }
}
