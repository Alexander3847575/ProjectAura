package com.numbers.projectaura.animation.functions;

public final class Periodic {

    public interface PeriodicFunction {
        float get(float t, float a, float p);
    }

    public class PeriodicAnimation implements IAnimationFunction {

        @Override
        public float getAt(long deltaTime) {
            return 0;
        }
    }
}
