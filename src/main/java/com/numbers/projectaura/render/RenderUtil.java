package com.numbers.projectaura.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class RenderUtil {

    /**
     * Renders a texture CENTERED on the PoseStack.
     * @param poseStack
     * @param builder
     * @param textureSize
     * @param offsetVector
     * @param color
     * @param alpha
     * @param light
     */
    public static void renderColoredTexture(PoseStack poseStack, VertexConsumer builder, float textureSize, Vector3f offsetVector, int color, int alpha, int light) {

        float halfTextureSize = textureSize / 2;
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        Matrix4f lastPose = poseStack.last().pose();
        builder.vertex(lastPose, offsetVector.x - halfTextureSize,  offsetVector.y + halfTextureSize, offsetVector.z).color(r, g, b, alpha).uv(0.0F, 1.0F).uv2(light).endVertex();
        builder.vertex(lastPose, offsetVector.x + halfTextureSize,  offsetVector.y + halfTextureSize, offsetVector.z).color(r, g, b, alpha).uv(1.0F, 1.0F).uv2(light).endVertex();
        builder.vertex(lastPose, offsetVector.x + halfTextureSize, offsetVector.y - halfTextureSize, offsetVector.z).color(r, g, b, alpha).uv(1.0F, 0.0F).uv2(light).endVertex();
        builder.vertex(lastPose, offsetVector.x - halfTextureSize, offsetVector.y - halfTextureSize, offsetVector.z).color(r, g, b, alpha).uv(0.0F, 0.0F).uv2(light).endVertex();
    }

    public static int blendColors(int overlayColor, int overlayAlpha, int bgColor) {

        int ar = (overlayColor >> 16) & 0xFF;
        int ag = (overlayColor >> 8) & 0xFF;
        int ab = overlayColor & 0xFF;

        int br = (bgColor >> 16) & 0xFF;
        int bg = (bgColor >> 8) & 0xFF;
        int bb = bgColor & 0xFF;

        // C implementation >> java one lmao
        /*int cr = (ar * ar + br * (255 - this.bufferAlpha)) / 255;
        int cg = (ag * ag + bg * (255 - this.bufferAlpha)) / 255;
        int cb = (ab * ab + bb * (255 - this.bufferAlpha)) / 255;*/

        int alpha = overlayAlpha + 1;
        int inv_alpha = 256 - overlayAlpha;

        int cr = (alpha * ar + inv_alpha * br) >> 8;
        int cg = (alpha * ag + inv_alpha * bg) >> 8;
        int cb = (alpha * ab + inv_alpha * bb) >> 8;

        return 0xff000000 | cr << 16 | cg << 8 | cb;
    }

}
