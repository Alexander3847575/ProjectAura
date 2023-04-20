package com.numbers.projectaura.animation.effects;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Vector3f;

public interface IRenderMethod {
    void render(Effect self, PoseStack poseStack, MultiBufferSource buffers, Vector3f offsetVector);
}
