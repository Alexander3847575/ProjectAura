package com.numbers.projectaura.animation.effects;

import com.mojang.blaze3d.vertex.PoseStack;
import com.numbers.projectaura.animation.Animation;
import com.numbers.projectaura.animation.AnimationComponent;
import com.numbers.projectaura.animation.functions.Eases;
import com.numbers.projectaura.render.RenderUtil;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;

// TODO: I swear there's a functional way to do this but my brain can't take trying to anymore
public class ExpandAndFadeEffect extends Effect {

    public ExpandAndFadeEffect(ResourceLocation texture, float effectSize, float animationScale, long animationDuration, long animationDelay, int color, int alpha, int light) {
        super(
                new Animation()
                        // Scale
                        .addComponent(
                                new AnimationComponent(
                                        new Eases.Ease(
                                                Eases.CUBIC_EASE_OUT,
                                                1,
                                                animationScale,
                                                animationDuration - animationDelay
                                        ),
                                        animationDelay
                                )
                        )
                        // Alpha
                        .addComponent(
                                new AnimationComponent(
                                        new Eases.Ease(
                                                Eases.LINEAR_EASE,
                                                alpha,
                                                -alpha,
                                                animationDuration - animationDelay
                                        ),
                                        animationDelay
                                )
                        )
                        .setDuration(animationDuration),
                texture,
                effectSize,
                color,
                alpha,
                light
        );

    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffers, Vector3f offsetVector) {
        float scaleMultiplier = animation.getComponentValue(0);
        float scaledOffset = (halfEffectSize * (scaleMultiplier - 1));
        offsetVector.add(-scaledOffset, -scaledOffset, 0F);
        RenderUtil.renderColoredTexture(poseStack, this.getConsumer(buffers), 16 * scaleMultiplier, offsetVector, this.color, Math.round(this.animation.getComponentValue(1)), light);
    }
}