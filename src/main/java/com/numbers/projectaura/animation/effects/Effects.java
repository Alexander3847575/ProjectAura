package com.numbers.projectaura.animation.effects;

import com.numbers.projectaura.animation.Animation;
import net.minecraft.resources.ResourceLocation;

public class Effects {

    public static Effect EXPAND_AND_FADE_EFFECT(float animationScale, long animationDuration, long animationDelay) {
        return new Effect(new Animation(), new ResourceLocation("d"), 0, 0, 0, 0);
    }
}
