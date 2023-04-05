package com.numbers.projectaura.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.numbers.projectaura.ProjectAura;
import com.numbers.projectaura.animation.Animation;
import com.numbers.projectaura.capability.HealthBarCapability;
import com.numbers.projectaura.registries.CapabilityRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static com.numbers.projectaura.render.ProjectAuraRenderType.BAR_TEXTURE_TYPE;

/**
 * Code based off Vaskii's mod Neat. Thanks!
 */
public class HealthBarRenderer {

    private static final int MAX_DIST = 24;
    private static final DecimalFormat HEALTH_FORMAT = new DecimalFormat("#.##");

    @Nullable
    private static Entity getEntityLookedAt(Entity e) {
        Entity foundEntity = null;
        final double finalDistance = 32;
        HitResult pos = raycast(e, finalDistance);
        Vec3 positionVector = e.getEyePosition();
        double distance = pos.getLocation().distanceTo(positionVector);
        Vec3 lookVector = e.getLookAngle();
        Vec3 reachVector = positionVector.add(lookVector.x * finalDistance, lookVector.y * finalDistance, lookVector.z * finalDistance);
        List<Entity> entitiesInBoundingBox = e.getLevel().getEntities(e,
                e.getBoundingBox().inflate(lookVector.x * finalDistance, lookVector.y * finalDistance, lookVector.z * finalDistance)
                        .expandTowards(1F, 1F, 1F));
        double minDistance = distance;
        for (Entity entity : entitiesInBoundingBox) {
            Entity lookedEntity = null;
            if (entity.isPickable()) {
                AABB collisionBox = entity.getBoundingBoxForCulling();
                Optional<Vec3> interceptPosition = collisionBox.clip(positionVector, reachVector);
                if (collisionBox.contains(positionVector)) {
                    if (0.0D < minDistance || minDistance == 0.0D) {
                        lookedEntity = entity;
                        minDistance = 0.0D;
                    }
                } else if (interceptPosition.isPresent()) {
                    double distanceToEntity = positionVector.distanceTo(interceptPosition.get());
                    if (distanceToEntity < minDistance || minDistance == 0.0D) {
                        lookedEntity = entity;
                        minDistance = distanceToEntity;
                    }
                }
            }
            if (lookedEntity != null && minDistance < distance) {
                foundEntity = lookedEntity;
            }
        }
        return foundEntity;
    }
    private static HitResult raycast(Entity e, double len) {
        return raycast(e.getEyePosition(), e.getLookAngle(), e, len);
    }
    private static HitResult raycast(Vec3 origin, Vec3 ray, Entity e, double len) {
        Vec3 next = origin.add(ray.normalize().scale(len));
        return e.level.clip(new ClipContext(origin, next, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, e));
    }
    @Nonnull
    private static ItemStack getIcon(LivingEntity entity, boolean boss) {
        if (boss) {
            return new ItemStack(Items.NETHER_STAR);
        }
        MobType type = entity.getMobType();
        if (type == MobType.ARTHROPOD) {
            return new ItemStack(Items.SPIDER_EYE);
        } else if (type == MobType.UNDEAD) {
            return new ItemStack(Items.ROTTEN_FLESH);
        } else {
            return ItemStack.EMPTY;
        }
    }
    private static int getColor(LivingEntity entity, boolean colorByType, boolean boss) {
        if (colorByType) {
            int r = 0;
            int g = 255;
            int b = 0;
            if (boss) {
                r = 128;
                g = 0;
                b = 128;
            }
            if (entity instanceof Monster) {
                r = 255;
                g = 0;
                b = 0;
            }
            return 0xff000000 | r << 16 | g << 8 | b;
        } else {
            float health = Mth.clamp(entity.getHealth(), 0.0F, entity.getMaxHealth());
            float hue = Math.max(0.0F, (health / entity.getMaxHealth()) / 3.0F - 0.07F);
            return Mth.hsvToRgb(hue, 1.0F, 1.0F);
        }
    }
    private static boolean isBoss(Entity entity) {
        // TODO inaccurate
        return !entity.canChangeDimensions();
    }
    private static boolean shouldShowPlate(Entity entity, Entity cameraEntity) {


        if (!(entity instanceof LivingEntity living)) {
            return false;
        }

        float distance = living.distanceTo(cameraEntity);
        if (distance > MAX_DIST
                || !living.hasLineOfSight(cameraEntity)
                || living.isInvisible()) {
            return false;
        }
        if (false && getEntityLookedAt(cameraEntity) != entity) { //CONF
            return false;
        }
        var id = BuiltInRegistries.ENTITY_TYPE.getKey(living.getType());

        return true;
    }

    // TODO: figure out if it's possible to scale the health bar based on distance from camera to be a constant size
    //      make a param override for mobs where the health bar needs to be positioned differently, e.g. elder guardian, wither
    //      render icons for auras - DONE
    //      add element application animation, aura fade out flash (sine based)
    //      maybe override boss bar
    //      config
    //      track entity combat time, fade in/out
    //      move to gui render to render above fire and particles; will depth test still work?
    public static void hookRender(Entity entity, PoseStack poseStack, MultiBufferSource buffers,
                                  Quaternionf cameraOrientation) {
        final Minecraft mc = Minecraft.getInstance();
        if (!(entity instanceof LivingEntity living) || !entity.getPassengers().isEmpty()) {
            // TODO handle mob stacks properly
            return;
        }
        Entity cameraEntity = mc.gameRenderer.getMainCamera().getEntity();
        if (!shouldShowPlate(entity, cameraEntity)) {
            return;
        }
        // Constants
        final float scaleModifier = 0.0267F/2;// 0.0267F;
        double distance = cameraEntity.distanceTo(entity);
        float globalScale = (float) (Math.max((distance/6) * scaleModifier, scaleModifier * 2)); // Adjust size of bar based on distance to entity

        final boolean boss = isBoss(entity);
        final String name = entity.hasCustomName()
                ? ChatFormatting.ITALIC + entity.getCustomName().getString()
                : entity.getDisplayName().getString();
        final double vOffset = Math.min(entity.getBbHeight() * 1.75, entity.getBbHeight() + 0.5);
        //final float halfSize = Math.max(40, nameLen / 2.0F + 10.0F);//25 is CONF; plateSize
        poseStack.pushPose();
        poseStack.translate(0, vOffset, 0);
        poseStack.mulPose(cameraOrientation);
        // Plate background, bars, and text operate with globalScale, but icons don't
        poseStack.pushPose();
        poseStack.scale(-globalScale, -globalScale, globalScale);
        // Background
        /*if (false) { //CONF
            float padding = 2;// CONF
            int bgHeight = 6;//CONF
            VertexConsumer builder = buffers.getBuffer(BAR_TEXTURE_TYPE);
            builder.vertex(poseStack.last().pose(), -halfSize - padding, -bgHeight, 0.01F).color(0, 0, 0, 64).uv(0.0F, 0.0F).uv2(light).endVertex();
            builder.vertex(poseStack.last().pose(), -halfSize - padding, barHeight + padding, 0.01F).color(0, 0, 0, 64).uv(0.0F, 0.5F).uv2(light).endVertex();
            builder.vertex(poseStack.last().pose(), halfSize + padding, barHeight + padding, 0.01F).color(0, 0, 0, 64).uv(1.0F, 0.5F).uv2(light).endVertex();
            builder.vertex(poseStack.last().pose(), halfSize + padding, -bgHeight, 0.01F).color(0, 0, 0, 64).uv(1.0F, 0.0F).uv2(light).endVertex();
        }*/
        // Health Bar
        // 24 is a nice number; not sure what to actually multiply by to match bb dimensions
        final BarProperties properties = new BarProperties(
                entity.getBbWidth() * 24f + 0.4f,
                6,
                3,
                200,
                0xF000F0
        );
        final HealthBarCapability cap = CapabilityRegistry.getCapability(living, CapabilityRegistry.HEALTH_BAR_CAPABILITY);

        assert cap != null;

        {

            int color = getColor(living, false, boss); //CONF
            final VertexConsumer healthBarBuilder = buffers.getBuffer(BAR_TEXTURE_TYPE);


            cap.tickBuffer(); //Unsafe but I think capability should be registered to all LivingEntities
            float bufferPos = cap.getBufferPos();
            float healthPercent = cap.getHealthPercent();

            renderHealthBar(0.003F, 0, healthPercent, color, 255, properties, poseStack, healthBarBuilder); //Main Health Bar
            renderHealthBar(0.003F, bufferPos, 100, 0x00000000, 170, properties, poseStack, healthBarBuilder); // Empty health bar
            renderHealthBar(0.001F, healthPercent, bufferPos, cap.getBlendedBufferColor(0), cap.getBlendedBufferAlpha(170), properties, poseStack, healthBarBuilder); // Buffer

            final VertexConsumer outlineBuilder = buffers.getBuffer(ProjectAuraRenderType.OUTLINE_TYPE);
            final int outlineColor = 0xff000000 | 75 << 16 | 60 << 8 | 20;
            renderHealthBarOutline(properties.barLength, outlineColor, 150, properties, poseStack, outlineBuilder);

        }
        // Aura Icons
        {
            poseStack.pushPose();
            RenderSystem.enableBlend();

            poseStack.translate(-9, -30, 0);
            final int iconSize = 16;
            final int halfIconSize = 16/2;

            int aurasSize = cap.auraRenderQueue.size();
            AtomicInteger i = new AtomicInteger();

            RenderSystem.disableDepthTest();

            // Render an icon for each aura applied to the entity
            cap.auraRenderQueue.entrySet().forEach((entry) -> {

                // Center each icon with padding
                float xOffset = ((iconSize + 0.5f) * (i.get() - ((aurasSize/2f) - 0.5f)));
                final int iconColor = entry.getKey().getColor();

                // REnder slight glow effect behind icon
                final VertexConsumer blurBuilder = buffers.getBuffer(ProjectAuraRenderType.coloredTexType(new ResourceLocation(ProjectAura.MOD_ID, "textures/ui/blur.png")));
                renderColoredTexture(poseStack, blurBuilder, iconSize * 1.25F, xOffset -2, -1,0.01F, iconColor, 130, properties.light);

                // Render icon
                final VertexConsumer iconBuilder = buffers.getBuffer(ProjectAuraRenderType.coloredTexType(entry.getKey().getIcon()));
                renderColoredTexture(poseStack, iconBuilder, iconSize, xOffset, 0, 0F, iconColor, 255, properties.light);

                //renderAuraIcon(poseStack, iconBuilder, 16, xOffset);
                Animation animation = cap.auraApplyAnimations.get(entry.getValue().getB());

                if (animation != null) {
                    if (animation.isActive()) {
                        float scaleMultiplier = animation.getComponentValue(0);
                        float scaledOffset = (halfIconSize * (scaleMultiplier - 1));
                        renderColoredTexture(poseStack, iconBuilder, iconSize * scaleMultiplier, xOffset - scaledOffset, -scaledOffset, 0F, HealthBarCapability.WHITE, Math.round(animation.getComponentValue(1)), properties.light);
                    }
                }

                i.getAndIncrement();
            });

            RenderSystem.enableDepthTest();

            poseStack.popPose();
        }
        // Text
        {
            final float textScale = 1F;
            final float nameLen = mc.font.width(name) * textScale;

            final int white = 0xFFFFFF;
            final int black = 0;

            // Name
            {
                poseStack.pushPose();
                poseStack.translate(-nameLen / 2, -12F, 0F);
                poseStack.scale(textScale, textScale, textScale);
                mc.font.drawInBatch(name, 0, 0, white, false, poseStack.last().pose(), buffers, Font.DisplayMode.NORMAL, black, properties.light());
                poseStack.popPose();


            }
            // Health values (and debug ID)
            {
                final float healthValueTextScale = 0.25F * textScale;
                poseStack.pushPose();
                poseStack.translate(-properties.bodyLength, -4.5F, 0F);
                poseStack.scale(healthValueTextScale, healthValueTextScale, healthValueTextScale);
                int hpTextHeight = 14; //CONF
                if (false) {
                    String hpStr = HEALTH_FORMAT.format(living.getHealth());
                    mc.font.drawInBatch(hpStr, 2, hpTextHeight, white, false, poseStack.last().pose(), buffers, Font.DisplayMode.NORMAL, black, properties.light());
                }
                if (false) {
                    String maxHpStr = ChatFormatting.BOLD + HEALTH_FORMAT.format(living.getMaxHealth());
                    mc.font.drawInBatch(maxHpStr, (int) (properties.bodyLength / healthValueTextScale * 2) - mc.font.width(maxHpStr) - 2, hpTextHeight, white, false, poseStack.last().pose(), buffers, Font.DisplayMode.NORMAL, black, properties.light());
                }
                if (false) {
                    String percStr = (int) (100 * living.getHealth() / living.getMaxHealth()) + "%";
                    mc.font.drawInBatch(percStr, (int) (properties.bodyLength / healthValueTextScale) - mc.font.width(percStr) / 2.0F, hpTextHeight, white, false, poseStack.last().pose(), buffers, Font.DisplayMode.NORMAL, black, properties.light());
                }
                if (true && mc.options.renderDebug) {
                    var id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
                    mc.font.drawInBatch("ID: \"" + id + "\"", 0, hpTextHeight + 16, white, false, poseStack.last().pose(), buffers, Font.DisplayMode.NORMAL, black, properties.light());
                }
                poseStack.popPose();
            }
        }
        poseStack.popPose(); // Remove globalScale
        // Icons
        {
            final float zBump = -0.1F;
            poseStack.pushPose();
            float iconOffset = 2.85F;
            float zShift = 0F;
            if (true) {
                var icon = getIcon(living, boss);
                renderIcon(icon, poseStack, buffers, mc.level, globalScale, properties.barLength(), iconOffset, zShift);
                iconOffset += 5F;
                zShift += zBump;
            }
            int armor = living.getArmorValue();
            if (armor > 0 && true) {
                int ironArmor = armor % 5;
                int diamondArmor = armor / 5;
                if (!true) {
                    ironArmor = armor;
                    diamondArmor = 0;
                }
                var iron = new ItemStack(Items.IRON_CHESTPLATE);
                for (int i = 0; i < ironArmor; i++) {
                    renderIcon(iron, poseStack, buffers, mc.level, globalScale, properties.barLength(), iconOffset, zShift);
                    iconOffset += 1F;
                    zShift += zBump;
                }
                var diamond = new ItemStack(Items.DIAMOND_CHESTPLATE);
                for (int i = 0; i < diamondArmor; i++) {
                    renderIcon(diamond, poseStack, buffers, mc.level, globalScale, properties.barLength(), iconOffset, zShift);
                    iconOffset += 1F;
                    zShift += zBump;
                }
            }
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    private record BarProperties(float barLength, float barHeight, float capOffset, int alpha, int light) {

        public static float bodyLength; //actually half
        public static float halfBarHeight;
        public static float slope;

        public BarProperties {
            bodyLength = barLength - capOffset;
            halfBarHeight = barHeight / 2f;
            slope = halfBarHeight / capOffset; // Having the bar height and cap offset at a 2:1 ratio means that the slope will be one, or an end cap angle of 45 degrees.
        }
    }

    // TODO: this implementation doesn't need texture at all
    /**
     *  Renders a health bar.
     * @param length Length of the health bar (unknown units)
     * @param from Percentage of the health bar to render from
     * @param to Percentage of the health bar to render from
     * @param color int representation of an rgb color
     * @param alpha Alpha channel for the health bar
     * @param properties A {@link BarProperties}.
     * @param poseStack Parent {@link PoseStack}
     * @param builder The {@link VertexConsumer} to use.
     */
    private static void renderHealthBar(float zOffset, float from, float to, int color, int alpha, BarProperties properties, PoseStack poseStack, VertexConsumer builder) {

        if (from == to) {
            return;
        }

        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;


        /**
         *      Vertex breakdown because I can't brain properly
         *     A1                B4
         *   L  /-------------\   R
         *      \_____________/
         *     C2                D3
         *     numbers indicate build order, found through testing
         *     height go up = vertex go down
         *     so A would be (0, 0) and C would be (0, 4), although in actuality the bar is centered on 0 on the x-axis.
         */


        // Trading computation for memory :D
        // I think making them final means they will only be computed once, hopefully globally?
        Matrix4f lastPose = poseStack.last().pose();

        float start = properties.barLength * from * 0.02f - properties.barLength;
        float end = properties.barLength * to * 0.02f - properties.barLength;

        float bodyStart = Math.max(start, -properties.bodyLength);
        float bodyEnd = Math.min(end, properties.bodyLength);


        // Left cap
        // All of this just to display some funny triangles...........
        if (start < -properties.bodyLength) {

            float vOffset = -properties.slope * (start + properties.bodyLength);
            float vOffsetBase = 0;

            // This check might not be important
            if (end < -properties.bodyLength) {
                vOffsetBase = -properties.slope * (end + properties.bodyLength);
            }

            float capBaseStart = Math.min(end, -properties.bodyLength);

            builder.vertex(lastPose, start, properties.barHeight - vOffset, zOffset).color(r, g, b, alpha).uv(0.0F, 1.0F).uv2(properties.light).endVertex();
            builder.vertex(lastPose, capBaseStart, properties.barHeight - vOffsetBase, zOffset).color(r, g, b, alpha).uv(1.0F, 1.0F).uv2(properties.light).endVertex();
            builder.vertex(lastPose, capBaseStart, vOffsetBase, zOffset).color(r, g, b, alpha).uv(1.0F, 0.75F).uv2(properties.light).endVertex();
            builder.vertex(lastPose, start, vOffset, zOffset).color(r, g, b, alpha).uv(1.0F, 1.0F).uv2(properties.light).endVertex();

        }

        // Right cap
        if (end > properties.bodyLength) {

            float vOffset = properties.slope * (end - properties.bodyLength);
            float vOffsetBase = 0;

            // This check might not be important
            if (start > properties.bodyLength) {
                vOffsetBase = properties.slope * (start - properties.bodyLength); // Should range from 0 to 2
            }

            float capBaseStart = Math.max(start, properties.bodyLength);

            builder.vertex(lastPose, capBaseStart, vOffsetBase, zOffset).color(r, g, b, alpha).uv(1.0F, 0.75F).uv2(properties.light).endVertex();
            builder.vertex(lastPose, capBaseStart, properties.barHeight - vOffsetBase, zOffset).color(r, g, b, alpha).uv(1.0F, 1.0F).uv2(properties.light).endVertex();
            builder.vertex(lastPose, end, properties.barHeight - vOffset, zOffset).color(r, g, b, alpha).uv(0.0F, 1.0F).uv2(properties.light).endVertex();
            builder.vertex(lastPose, end, vOffset, zOffset).color(r, g, b, alpha).uv(1.0F, 1.0F).uv2(properties.light).endVertex();

        }

        // Body
        //TODO: consistent vx build order
        builder.vertex(lastPose, bodyStart, 0, zOffset).color(r, g, b, alpha).uv(0.0F, 0.75F).uv2(properties.light).endVertex();
        builder.vertex(lastPose, bodyStart, properties.barHeight, zOffset).color(r, g, b, alpha).uv(0.0F, 1.0F).uv2(properties.light).endVertex();
        builder.vertex(lastPose, bodyEnd, properties.barHeight, zOffset).color(r, g, b, alpha).uv(1.0F, 1.0F).uv2(properties.light).endVertex();
        builder.vertex(lastPose, bodyEnd, 0, zOffset).color(r, g, b, alpha).uv(1.0F, 0.75F).uv2(properties.light).endVertex();

    }

    /**
     * Renders a solid health bar outline.
     * @param length Length of the health bar
     * @param color Color of the outline
     * @param alpha Alpha of the outline
     * @param properties Properties of the health bar
     * @param poseStack {@link PoseStack} to render in
     * @param builder {@link VertexConsumer} to build with (of {@link RenderType} LINES)
     */
    private static void renderHealthBarOutline(float length, int color, int alpha, BarProperties properties, PoseStack poseStack, VertexConsumer builder) {

        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        Matrix4f lastPose = poseStack.last().pose();
        // Slightly increase z-offset ato avoid fighting :)
        final float zOffset = 0.000F;
        builder.vertex(lastPose, -length, properties.halfBarHeight, zOffset).color(r, g, b, alpha).normal(poseStack.last().normal(), 1, 0, 0).endVertex();
        builder.vertex(lastPose, -properties.bodyLength, properties.barHeight, zOffset).color(r, g, b, alpha).normal(poseStack.last().normal(), 1, 0, 0).endVertex();

        builder.vertex(lastPose, -properties.bodyLength, properties.barHeight, zOffset).color(r, g, b, alpha).normal(poseStack.last().normal(), 1, 0, 0).endVertex();
        builder.vertex(lastPose, properties.bodyLength, properties.barHeight, zOffset).color(r, g, b, alpha).normal(poseStack.last().normal(), 1, 0, 0).endVertex();

        builder.vertex(lastPose, properties.bodyLength, properties.barHeight, zOffset).color(r, g, b, alpha).normal(poseStack.last().normal(), 1, 0, 0).endVertex();
        builder.vertex(lastPose, length, properties.halfBarHeight, zOffset).color(r, g, b, alpha).normal(poseStack.last().normal(), 1, 0, 0).endVertex();

        builder.vertex(lastPose, length, properties.halfBarHeight, zOffset).color(r, g, b, alpha).normal(poseStack.last().normal(), 1, 0, 0).endVertex();
        builder.vertex(lastPose, properties.bodyLength, 0, zOffset).color(r, g, b, alpha).normal(poseStack.last().normal(), 1, 0, 0).endVertex();

        builder.vertex(lastPose, properties.bodyLength, 0, zOffset).color(r, g, b, alpha).normal(poseStack.last().normal(), 1, 0, 0).endVertex();
        builder.vertex(lastPose, -properties.bodyLength, 0, zOffset).color(r, g, b, alpha).normal(poseStack.last().normal(), 1, 0, 0).endVertex();

        builder.vertex(lastPose, -properties.bodyLength, 0, zOffset).color(r, g, b, alpha).normal(poseStack.last().normal(), 1, 0, 0).endVertex();
        builder.vertex(lastPose, -length, properties.halfBarHeight, zOffset).color(r, g, b, alpha).normal(poseStack.last().normal(), 1, 0, 0).endVertex();

    }

    private static void renderAuraIcon(PoseStack poseStack, VertexConsumer builder, float iconSize, float xOffset) {
        Matrix4f lastPose = poseStack.last().pose();
        builder.vertex(lastPose, xOffset, iconSize, 0.000F).uv(0.0F, 1.0F).endVertex();
        builder.vertex(lastPose, xOffset + iconSize, iconSize, 0.000F).uv(1.0F, 1.0F).endVertex();
        builder.vertex(lastPose, xOffset + iconSize, 0, 0.000F).uv(1.0F, 0.0F).endVertex();
        builder.vertex(lastPose, xOffset, 0, 0.000F).uv(0.0F, 0.0F).endVertex();
    }

    private static void renderIcon(ItemStack icon, PoseStack poseStack, MultiBufferSource buffers, Level level, float globalScale, float halfSize, float leftShift, float zShift) {
        if (!icon.isEmpty()) {
            final float iconScale = 0.12F;
            poseStack.pushPose();
            // halfSize and co. are units operating under the assumption of globalScale,
            // but in the icon rendering section we don't use globalScale, so we need
            // to manually multiply it in to ensure the units line up.
            float dx = (halfSize - leftShift) * globalScale;
            float dy = 3F * globalScale;
            float dz = zShift * globalScale;
            // Need to negate X due to our rotation below
            poseStack.translate(-dx, dy, dz);
            poseStack.scale(iconScale, iconScale, iconScale);
            poseStack.mulPose(Axis.YP.rotationDegrees(180F));
            Minecraft.getInstance().getItemRenderer()
                    .renderStatic(icon, ItemDisplayContext.NONE,
                            0xF000F0, OverlayTexture.NO_OVERLAY, poseStack, buffers, level, 0);
            poseStack.popPose();
        }
    }

    private static void renderColoredTexture(PoseStack poseStack, VertexConsumer builder, float textureSize, float xOffset, float yOffset, float zOffset, int color, int alpha, int light) {

        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        Matrix4f lastPose = poseStack.last().pose();
        builder.vertex(lastPose, xOffset, textureSize + yOffset, zOffset).color(r, g, b, alpha).uv(0.0F, 1.0F).uv2(light).endVertex();
        builder.vertex(lastPose, xOffset + textureSize, textureSize + yOffset, zOffset).color(r, g, b, alpha).uv(1.0F, 1.0F).uv2(light).endVertex();
        builder.vertex(lastPose, xOffset + textureSize, yOffset, zOffset).color(r, g, b, alpha).uv(1.0F, 0.0F).uv2(light).endVertex();
        builder.vertex(lastPose, xOffset, yOffset, zOffset).color(r, g, b, alpha).uv(0.0F, 0.0F).uv2(light).endVertex();
    }

}