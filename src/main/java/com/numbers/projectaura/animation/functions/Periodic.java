package com.numbers.projectaura.animation.functions;

import lombok.Getter;

public final class Periodic {

    public interface PeriodicFunction {
        float get(float t, float p, float a);
    }

    public static PeriodicFunction SINE = (t, p, a) -> ((float) Math.sin((t *  Math.PI)/ (p/2)) * a);

    public static class PeriodicAnimation implements IAnimationFunction {
        private float period;
        private float amplitude;
        @Getter
        private float duration;
        private PeriodicFunction periodicFunction;

        public PeriodicAnimation(PeriodicFunction periodicFunction, float period, float amplitude, float duration) {
            this.periodicFunction = periodicFunction;
            this.period = period;
            this.amplitude = amplitude;
            this.duration = duration;
        }

        /**
         * Gets the state of the interpolation at a certain point in time.
         * @param deltaTime Relative time of the interpolation
         * @return The state of the interpolation at the specified time.
         */
        public float getAt(long deltaTime) {
            return this.periodicFunction.get(deltaTime, this.period, this.amplitude);
        }
    }
}
