package com.numbers.projectaura.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.numbers.projectaura.animation.Animation;
import com.numbers.projectaura.animation.AnimationComponent;
import com.numbers.projectaura.animation.functions.Eases;
import com.numbers.projectaura.registries.ParticleRegistry;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Code initially taken from Dummmmmmy by MehVahdJukaar (highly transformative I swear).
 */
public class DamageNumberParticle extends Particle {

    private static final List<Float> POSITIONS = new ArrayList<>(Arrays.asList(0f, -0.25f, 0.12f, -0.12f, 0.25f));
    private final Font fontRenderer = Minecraft.getInstance().font;

    private final String text;
    private float fadeout = 1;
    private float prevFadeout = 1;

    private static final long animationDuration = 400L;
    private static final long animationDelay = 0;
    private Animation particleAnimation = new Animation()
            // Scale
            .addComponent(
                    new AnimationComponent(
                            new Eases.Ease(
                                    Eases.EXPONENTIAL_EASE_OUT,
                                    5,
                                    -4f,
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
            .setDuration(animationDuration);


    public DamageNumberParticle(ClientLevel clientLevel, double x, double y, double z, DamageParticleOptions options) {
        super(clientLevel, x + Mth.randomBetween(Minecraft.getInstance().level.getRandom(), -1f, 1f), y + Mth.randomBetween(Minecraft.getInstance().level.getRandom(), 0f, 1f), z + Mth.randomBetween(Minecraft.getInstance().level.getRandom(), -1f, 1f));
        this.lifetime = 35;

        int color = options.color();
        this.setColor(FastColor.ARGB32.red(color), FastColor.ARGB32.green(color), FastColor.ARGB32.blue(color));
        this.particleAnimation
                // Color
                .addComponent(
                        new AnimationComponent(
                                (dt) -> color,
                                animationDuration
                        )
                )
                .start();

        this.text = options.text();

        //this.yd = 1;
        //this.xd = Mth.randomBetween(Minecraft.getInstance().level.getRandom(), -0.25f, 0.25f);
        //this.xOffset = Mth.randomBetween(Minecraft.getInstance().level.getRandom(), -2f, 2f);
        //this.yOffset = Mth.randomBetween(Minecraft.getInstance().level.getRandom(), -1f, 1f);
        // TODO: randomize positions more, implement crit effect, implement elemental damage color, data drive elemental damage items, api for ealing elemental daamage
        //  get reaction key in registry, translatable component for reaction names
    }

    @Override
    public void render(VertexConsumer consumer, Camera camera, float partialTicks) {

        Vec3 cameraPos = camera.getPosition();
        float particleX = (float) (this.x - cameraPos.x());//(float) (Mth.lerp(partialTicks, this.xo, this.x) - cameraPos.x());
        float particleY = (float) (this.y - cameraPos.y());//(float) (Mth.lerp(partialTicks, this.yo, this.y) - cameraPos.y());
        float particleZ = (float) (this.z - cameraPos.z());//(float) (Mth.lerp(partialTicks, this.zo, this.z) - cameraPos.z());

        int light =  LightTexture.FULL_BRIGHT;

        PoseStack poseStack = new PoseStack();
        poseStack.pushPose();
        poseStack.translate(particleX, particleY, particleZ);

        double distanceFromCam = new Vec3(particleX, particleY, particleZ).length();

        float defScale = 0.024f;

        // Start scaling once more than 2 blocks away based off of the camera distance, and minimum of original scale, in addition to the particle scale
        float scale = (float) (defScale * Math.max(Math.max(distanceFromCam - 2, 0) / 8, 1) * this.particleAnimation.getComponentValue(0));

        // Rotate to face camera
        poseStack.mulPose(camera.rotation());
        poseStack.scale(-scale, -scale, scale);
        // float fadeout = Mth.lerp(partialTicks, this.prevFadeout, this.fadeout);
        //poseStack.translate(0, (4d * (1 - fadeout)), 0);
        poseStack.scale(fadeout, fadeout, fadeout);
        poseStack.translate(-particleX / 10, particleY /10, -particleZ/10);

        var buffer =  Minecraft.getInstance().renderBuffers().bufferSource();

        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(770, 771, 1, 0);

        float x1 = 0.5f - fontRenderer.width(text) / 2f;

        fontRenderer.drawInBatch(text, x1,
                0, Math.round(this.particleAnimation.getComponentValue(2)), false,
                poseStack.last().pose(), buffer, Font.DisplayMode.SEE_THROUGH, 0, light);
        poseStack.translate(1, 1, +0.03);

        buffer.endBatch();

        poseStack.popPose();
    }


    @Override
    public void tick() {
        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            float length = 6;
            this.prevFadeout = this.fadeout;
            this.fadeout = this.age > (lifetime - length) ? ((float) lifetime - this.age) / length : 1;

        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.CUSTOM;
    }


    public static class Factory implements ParticleProvider<DamageParticleOptions> {
        public Factory(SpriteSet spriteSet) { }
        @Override
        public Particle createParticle(DamageParticleOptions pType, ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
            return new DamageNumberParticle(pLevel, pX, pY, pZ, pType);
        }
    }

    public record DamageParticleOptions(String text, int color,
                                        boolean isCrit) implements ParticleOptions {

        public static final Codec<DamageParticleOptions> CODEC = RecordCodecBuilder.create((codec) ->
                    codec.group(
                            Codec.STRING.fieldOf("text").forGetter((particleOptions) -> particleOptions.text),
                            Codec.INT.fieldOf("color").forGetter((particleOptions) -> particleOptions.color),
                            Codec.BOOL.fieldOf("isCrit").forGetter((particleOptions) -> particleOptions.isCrit)
                    ).apply(codec, DamageParticleOptions::new)
            );

        public static final Deserializer<DamageParticleOptions> DESERIALIZER = new Deserializer<>() {
                public DamageParticleOptions fromCommand(ParticleType<DamageParticleOptions> particleType, StringReader stringReader) throws CommandSyntaxException {
                    String text = stringReader.readQuotedString();
                    int color = stringReader.readInt();
                    boolean isCrit = stringReader.readBoolean();
                    return new DamageParticleOptions(text, color, isCrit);
                }

            public DamageParticleOptions fromNetwork(ParticleType<DamageParticleOptions> particleType, FriendlyByteBuf buf) {
                    return new DamageParticleOptions(buf.readUtf(), buf.readInt(), buf.readBoolean());
                }
            };

        @Override
            public ParticleType<?> getType() {
                return ParticleRegistry.DAMAGE_EFFECT.get();
            }

        @Override
            public void writeToNetwork(FriendlyByteBuf pBuffer) {
                pBuffer.writeUtf(this.text);
                pBuffer.writeVarInt(this.color);
                pBuffer.writeBoolean(this.isCrit);
            }

        @Override
            public String writeToString() {
                return "null";
            }

    }
}
