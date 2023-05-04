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

    private float animationScale;
    //TODO: ExpandAndFadeAnimation then make Effect a functional itnerface

    public ExpandAndFadeEffect(ResourceLocation texture, float effectSize, float animationScale, long animationDuration, long animationDelay, AnimationComponent color, int alpha, int light) {
        super(
                new Animation()
                        // Scale
                        .addComponent(
                                new AnimationComponent(
                                        new Eases.Ease(
                                                Eases.EXPONENTIAL_EASE_OUT,
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
                        // Color
                        .addComponent(
                                color
                        )
                        .setDuration(animationDuration),
                texture,
                effectSize,
                0,
                alpha,
                light
        );

        this.animationScale = animationScale;

    }

    public ExpandAndFadeEffect(ResourceLocation texture, float effectSize, float animationScale, long animationDuration, long animationDelay, int color, int alpha, int light) {
        this(texture, effectSize, animationScale, animationDuration, animationDelay, new AnimationComponent((dt) -> color, animationDuration), alpha, light);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffers, Vector3f offsetVector) {

        if (!this.isActive()) {
            return;
        }

        float scaleMultiplier = this.animation.getComponentValue(0);
        RenderUtil.renderColoredTexture(poseStack, this.getConsumer(buffers), this.effectSize * scaleMultiplier, offsetVector, Math.round(this.animation.getComponentValue(2)), Math.round(this.animation.getComponentValue(1)), this.light);

        this.active = this.animation.isActive();

    }

    private IRenderMethod method = (self, poseStack, buffers, offsetVector) -> {


        float scaleMultiplier = self.animation.getComponentValue(0);
        float scaledOffset = (self.halfEffectSize * scaleMultiplier) - (8);
        //ProjectAura.LOGGER.debug(String.valueOf(scaledOffset));
        offsetVector.add(-scaledOffset, -scaledOffset, 0F);
        RenderUtil.renderColoredTexture(poseStack, self.getConsumer(buffers), self.effectSize * scaleMultiplier, offsetVector, Math.round(self.animation.getComponentValue(2)), Math.round(self.animation.getComponentValue(1)), self.light);


    };

}