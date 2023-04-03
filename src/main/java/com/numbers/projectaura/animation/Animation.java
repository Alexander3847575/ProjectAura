package com.numbers.projectaura.animation;

import com.numbers.projectaura.animation.component.AnimationComponent;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

public class Animation {
    private ArrayList<AnimationComponent> components = new ArrayList<>();

    // System time when the animation began, in milliseconds
    private long animationTimestamp = 0;
    @Getter
    private long animationDuration = 0;
    @Setter(AccessLevel.PRIVATE)
    private boolean active = false;
    private Runnable callback = () -> {};

    public void start() {
        this.animationTimestamp = System.currentTimeMillis();
        this.components.forEach((component -> component.start()));
        this.setActive(true);
    }

    public Animation addComponent(AnimationComponent component) {
        this.components.add(component);
        return this;
    }

    public Animation onFinish(Runnable callback) {
        this.callback = callback;
        return this;
    }

    public Animation setDuration(long duration) {
        this.animationDuration = duration;
        return this;
    }

    public AnimationComponent getComponent(int index) {
        return this.components.get(index);
    }

    public float getValueOfComponent(int index) {
        return this.getComponent(index).getLastState(this.getDeltaTime());
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
