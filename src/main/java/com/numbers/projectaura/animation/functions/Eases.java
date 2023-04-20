package com.numbers.projectaura.animation.functions;

import lombok.Getter;

import java.util.function.Supplier;

/**
 * This class contains a variety of Penner easing methods wrapped with a functional interface for effective abstraction.
 * I have awakened to the wonders of functional programming
 */
public final class Eases {

    /**
     * A functional interface that represents a interpolation method.
     */
    public interface EaseMethod {

        /**
         * Returns an interpolation between two values.
         * @param t Time since the beginning of the interpolation, in milliseconds. This should be the changing variable.
         * @param b The beginning (from) point.
         * @param c The CHANGE in value from the beginning.
         * @param d The duration of the interpolation, i.e. the input where the function will return b + c.
         * @return The interpolated value.
         */
        float ease(float t, float b, float c, float d);

    }

    /**
     * A wrapper class for EaseMethods that stores its constants in an animation context.
     */
    public static class Ease implements IAnimationFunction {
        private float beginning;
        private float change;
        @Getter
        private float duration;
        private EaseMethod easeMethod;

        public Ease(EaseMethod method, float beginning, float change, float duration) {
            this.easeMethod = method;
            this.beginning = beginning;
            this.change = change;
            this.duration = duration;
        }

        /**
         * Gets the state of the interpolation at a certain point in time.
         * @param deltaTime Relative time of the interpolation
         * @return The state of the interpolation at the specified time.
         */
        public float getAt(long deltaTime) {
            return easeMethod.ease(deltaTime, this.beginning, this.change, this.duration);
        }

    }

    /**
     * Untested, but might be able to update its goals after initialization for compatibility with ease wrappers
     * Supposedly used in the case where instantiating a new animation is undesirable but the interpolation points might change.
     */
    public static class DynamicEase extends Ease {
        private Supplier<Float> beginning;
        private Supplier<Float> change;
        private Supplier<Float> duration;
        private Supplier<EaseMethod> easeMethod;
        public DynamicEase(Supplier<EaseMethod> method, Supplier<Float> beginning, Supplier<Float> change, Supplier<Float> duration) {
            super(LINEAR_EASE, 0, 0, 0);
            this.easeMethod = method;
            this.beginning = beginning;
            this.change = change;
            this.duration = duration;
        }

        @Override
        public float getAt(long deltaTime) {
            return easeMethod.get().ease(deltaTime, beginning.get(), change.get(), duration.get());
        }
    }

    // Linear interpolation
    public static final EaseMethod LINEAR_EASE = (t, b, c, d) -> c*t/d + b;

    // Cubic interpolations
    public static final EaseMethod CUBIC_EASE_IN = (t, b, c, d) -> c*(t/=d)*t*t + b;
    public static final EaseMethod CUBIC_EASE_OUT = (t, b, c, d) -> c*((t=t/d-1)*t*t + 1) + b;
    public static final EaseMethod CUBIC_EASE_IN_OUT = (t, b, c, d) -> {
        if ((t/=d/2) < 1) return c/2*t*t*t + b;
        return c/2*((t-=2)*t*t + 2) + b;
    };

    // Exponential interpolations
    public static final EaseMethod EXPONENTIAL_EASE = (t, b, c, d) -> (t==0) ? b : c * (float)Math.pow(2, 10 * (t/d - 1)) + b;

    // Lazy RGB color interpolations
    public static final EaseMethod COLOR_LINEAR_EASE = (t, b, c, d) -> ColorEase.ease(t, (int) b, (int) c, d, LINEAR_EASE);
    public static final EaseMethod COLOR_CUBIC_EASE_IN = (t, b, c, d) -> ColorEase.ease(t, (int) b, (int) c, d, CUBIC_EASE_IN);

    // Elastic interpolation
    public static EaseMethod ELASTIC_EASE_IN = (t, b, c, d) -> {
        if (t==0) return b;  if ((t/=d)==1) return b+c;
        float p=d*.3f;
        float a=c;
        float s=p/4;
        return -(a*(float)Math.pow(2,10*(t-=1)) * (float)Math.sin( (t*d-s)*(2*(float)Math.PI)/p )) + b;
    };

    // Cursed? Maybe. I'm kinda proud of it though
    public static EaseMethod ELASTIC_EASE_IN(float a, float p) {
        return new EaseMethod() {
            float a = 0;
            float p = 2;
            @Override
            public float ease(float t, float b, float c, float d) {
                float s;
                if (t==0) return b;  if ((t/=d)==1) return b+c;
                if (a < Math.abs(c)) { a=c;  s=this.p/4; }
                else { s = this.p/(2*(float)Math.PI) * (float)Math.asin (c/a);}
                return -(a*(float)Math.pow(2,10*(t-=1)) * (float)Math.sin( (t*d-s)*(2*Math.PI)/this.p )) + b;
            }
            public EaseMethod setAP(float a, float p) {
                this.a = a;
                this.p = p;
                return this;
            }
        }.setAP(a, p);
    }

    public static EaseMethod ELASTIC_EASE_OUT = (t, b, c, d) -> {
        if (t==0) return b;  if ((t/=d)==1) return b+c;
        float p=d*.3f;
        float a=c;
        float s=p/4;
        return (a*(float)Math.pow(2,-10*t) * (float)Math.sin( (t*d-s)*(2*(float)Math.PI)/p ) + c + b);
    };

    public static EaseMethod ELASTIC_EASE_OUT(float a, float p) {
        return new EaseMethod() {
            float a = 0;
            float p = 2;
            @Override
            public float ease(float t, float b, float c, float d) {
                float a = this.a;
                float s;
                if (t==0) return b;  if ((t/=d)==1) return b+c;
                if (a < Math.abs(c)) { a=c;  s=this.p/4; }
                else { s = this.p/(2*(float)Math.PI) * (float)Math.asin (c/a);}
                return (a*(float)Math.pow(2,-10*t) * (float)Math.sin( (t*d-s)*(2*(float)Math.PI)/this.p ) + c + b);
            }
            public EaseMethod setAP(float a, float p) {
                this.a = a;
                this.p = p;
                return this;
            }
        }.setAP(a, p);
    }

    public static EaseMethod ELASTIC_EASE_IN_OUT = (t, b, c, d) -> {
        if (t==0) return b;  if ((t/=d/2)==2) return b+c;
        float p=d*(.3f*1.5f);
        float a=c;
        float s=p/4;
        if (t < 1) return -.5f*(a*(float)Math.pow(2,10*(t-=1)) * (float)Math.sin( (t*d-s)*(2*(float)Math.PI)/p )) + b;
        return a*(float)Math.pow(2,-10*(t-=1)) * (float)Math.sin( (t*d-s)*(2*(float)Math.PI)/p )*.5f + c + b;
    };

    public static EaseMethod ELASTIC_EASE_IN_OUT(float a, float p) {
        return new EaseMethod() {
            float a = 0;
            float p = 2;
            @Override
            public float ease(float t, float b, float c, float d) {
                float s;
                if (t==0) return b;  if ((t/=d/2)==2) return b+c;
                if (a < Math.abs(c)) { a=c; s=p/4; }
                else { s = p/(2*(float)Math.PI) * (float)Math.asin (c/a);}
                if (t < 1) return -.5f*(a*(float)Math.pow(2,10*(t-=1)) * (float)Math.sin( (t*d-s)*(2*(float)Math.PI)/p )) + b;
                return a*(float)Math.pow(2,-10*(t-=1)) * (float)Math.sin( (t*d-s)*(2*(float)Math.PI)/p )*.5f + c + b;
            }
            public EaseMethod setAP(float a, float p) {
                this.a = a;
                this.p = p;
                return this;
            }
        }.setAP(a, p);
    }



    /**
     * A basic implementation of lazy RGB color space interpolation that's abstracted using the {@link EaseMethod} functional interface.
     */
    private static class ColorEase {

        public static int ease(float t, int b , int c, float d, EaseMethod method) {

            final int ar = (b >> 16) & 0xFF;
            final int ag = (b >> 8) & 0xFF;
            final int ab = b & 0xFF;

            final int br = (c >> 16) & 0xFF;
            final int bg = (c >> 8) & 0xFF;
            final int bb = c & 0xFF;

            return 0xff000000 | Math.round(method.ease(t, ar, br - ar, d)) << 16 | Math.round(method.ease(t, ag, bg - ag, d)) << 8 | Math.round(method.ease(t, ab, bb - ab, d));

        }

    }

}
