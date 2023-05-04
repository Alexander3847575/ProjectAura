package com.numbers.projectaura.animation.effects;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.numbers.projectaura.ProjectAura;
import com.numbers.projectaura.animation.Animation;
import com.numbers.projectaura.render.ProjectAuraRenderType;
import com.numbers.projectaura.render.RenderUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;

/**
 * This class effectively wraps an {@link Animation}, texture, and render method to allow repeatable render code to be offloaded from the main render body to neat little objects.
 */
public class Effect {
    // TODO: convert to a functional interface?
    @Getter
    protected Animation animation;
    @Getter
    protected ResourceLocation texture = ProjectAura.DEFAULT_RESOURCE_LOCATION;
    protected float effectSize = 16;
    protected float halfEffectSize;
    protected int color;
    protected int alpha = 255;
    protected int light;
    @Getter @Setter(AccessLevel.PRIVATE)
    protected boolean active;
    private IRenderMethod method;

    public Effect(Animation animation, ResourceLocation texture, float effectSize, int color, int alpha, int light) {
        this.animation = animation;
        this.texture = texture;
        this.effectSize = effectSize;
        this.halfEffectSize = effectSize / 2;
        this.color = color;
        this.alpha = alpha;
        this.light = light;
        this.method = (self, poseStack, buffers, offsetVector) -> {
            RenderUtil.renderColoredTexture(poseStack, self.getConsumer(buffers), self.effectSize, offsetVector, self.color, self.alpha, light);
        };

        this.start(); // Auto start effect on initialization

    }
    public Effect(IRenderMethod method, Animation animation, ResourceLocation texture, float effectSize, int color, int alpha, int light) {
        this(animation, texture, effectSize, color, alpha, light);
        this.method = method;
    }

    /**
     * Renders the effect on the poseStack.
     * @param poseStack
     * @param buffers
     * @param offsetVector
     */
    public void render(PoseStack poseStack, MultiBufferSource buffers, Vector3f offsetVector) {

        if (!this.isActive()) {
            return;
        }

        this.method.render(this, poseStack, buffers, offsetVector);

        this.setActive(this.animation.isActive());

    }
    
    public void render(PoseStack poseStack, MultiBufferSource buffers) {
        this.render(poseStack, buffers, new Vector3f(0, 0, 0));
    }

    /**
     * Returns a VertexConsumer with the effect's texture.
     * @param bufferSource
     * @return
     */
    public VertexConsumer getConsumer(MultiBufferSource bufferSource) {
        return bufferSource.getBuffer(ProjectAuraRenderType.coloredTexType(this.texture));
    }

    public void start() {
        this.animation.start();
        this.setActive(true);
    }

    public void cancel() {
        this.animation.cancel();
        this.setActive(false);
    }


}





