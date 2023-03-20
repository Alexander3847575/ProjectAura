package com.numbers.projectaura.ui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import com.numbers.projectaura.capability.HealthBarCapability;
import com.numbers.projectaura.registries.CapabilityRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.text.DecimalFormat;
import java.util.*;

import static com.numbers.projectaura.ui.NeatRenderType.BAR_TEXTURE_TYPE;


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
        if (true && getEntityLookedAt(cameraEntity) != entity) { //CONF
            return false;
        }
        var id = BuiltInRegistries.ENTITY_TYPE.getKey(living.getType());

        return true;
    }
    public static void hookRender(Entity entity, PoseStack poseStack, MultiBufferSource buffers,
                                  Quaternionf cameraOrientation) {
        final Minecraft mc = Minecraft.getInstance();
        if (!(entity instanceof LivingEntity living) || !entity.getPassengers().isEmpty()) {
            // TODO handle mob stacks properly
            return;
        }
        if (!shouldShowPlate(entity, mc.gameRenderer.getMainCamera().getEntity())) {
            return;
        }
        // Constants
        final float globalScale = 0.0267F;
        final float textScale = 0.5F;
        final int barHeight = 4;//CONF
        final boolean boss = isBoss(entity);
        final String name = entity.hasCustomName()
                ? ChatFormatting.ITALIC + entity.getCustomName().getString()
                : entity.getDisplayName().getString();
        final float nameLen = mc.font.width(name) * textScale;
        //final float halfSize = Math.max(40, nameLen / 2.0F + 10.0F);//25 is CONF; plateSize
        poseStack.pushPose();
        poseStack.translate(0, entity.getBbHeight() + 0.4, 0);// 0.6 is CONF; height Above
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
        {
            BarProperties properties = new BarProperties(
                    40,
                    4,
                    0.125f,
                    255,
                    0xF000F0
            );

            int color = getColor(living, false, boss); //CONF

            // There are scenarios in vanilla where the current health
            // can temporarily exceed the max health.

            VertexConsumer builder = buffers.getBuffer(BAR_TEXTURE_TYPE);

            final float barLength = Math.max(properties.barLength(), nameLen / 2.0F + 10.0F);

            final int bufferColor = 0xff000000 | 255 << 16 | 252 << 8 | 250;
            final HealthBarCapability cap = CapabilityRegistry.getCapability(living, CapabilityRegistry.HEALTH_BAR_CAPABILITY);

            cap.tickBufferPos();
            float bufferbufferPos = cap.getBufferPos();

            float healthPercent = cap.getHealthPercent();


            renderHealthBar(barLength,0, healthPercent, color, properties, poseStack, builder); //Main Health Bar
            renderHealthBar(barLength, healthPercent, bufferbufferPos, bufferColor, properties, poseStack, builder);
            renderHealthBar(barLength, bufferbufferPos, 100, 0x00000000, properties, poseStack, builder); // Empty health bar


        }
        // Text
        /*{
            final int white = 0xFFFFFF;
            final int black = 0;
            // Name
            {
                poseStack.pushPose();
                poseStack.translate(-halfSize, -4.5F, 0F);
                poseStack.scale(textScale, textScale, textScale);
                mc.font.drawInBatch(name, 0, 0, white, false, poseStack.last().pose(), buffers, false, black, light);
                poseStack.popPose();
            }
            // Health values (and debug ID)
            {
                final float healthValueTextScale = 0.75F * textScale;
                poseStack.pushPose();
                poseStack.translate(-halfSize, -4.5F, 0F);
                poseStack.scale(healthValueTextScale, healthValueTextScale, healthValueTextScale);
                int hpTextHeight = 14; //CONF
                if (true) {
                    String hpStr = HEALTH_FORMAT.format(living.getHealth());
                    mc.font.drawInBatch(hpStr, 2, hpTextHeight, white, false, poseStack.last().pose(), buffers, false, black, light);
                }
                if (true) {
                    String maxHpStr = ChatFormatting.BOLD + HEALTH_FORMAT.format(living.getMaxHealth());
                    mc.font.drawInBatch(maxHpStr, (int) (halfSize / healthValueTextScale * 2) - mc.font.width(maxHpStr) - 2, hpTextHeight, white, false, poseStack.last().pose(), buffers, false, black, light);
                }
                if (true) {
                    String percStr = (int) (100 * living.getHealth() / living.getMaxHealth()) + "%";
                    mc.font.drawInBatch(percStr, (int) (halfSize / healthValueTextScale) - mc.font.width(percStr) / 2.0F, hpTextHeight, white, false, poseStack.last().pose(), buffers, false, black, light);
                }
                if (true && mc.options.renderDebug) {
                    var id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
                    mc.font.drawInBatch("ID: \"" + id + "\"", 0, hpTextHeight + 16, white, false, poseStack.last().pose(), buffers, false, black, light);
                }
                poseStack.popPose();
            }
        }*/
        poseStack.popPose(); // Remove globalScale
        // Icons
        {
            /*final float zBump = -0.1F;
            poseStack.pushPose();
            float iconOffset = 2.85F;
            float zShift = 0F;
            if (true) {
                var icon = getIcon(living, boss);
                renderIcon(icon, poseStack, buffers, globalScale, halfSize, iconOffset, zShift);
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
                    renderIcon(iron, poseStack, buffers, globalScale, halfSize, iconOffset, zShift);
                    iconOffset += 1F;
                    zShift += zBump;
                }
                var diamond = new ItemStack(Items.DIAMOND_CHESTPLATE);
                for (int i = 0; i < diamondArmor; i++) {
                    renderIcon(diamond, poseStack, buffers, globalScale, halfSize, iconOffset, zShift);
                    iconOffset += 1F;
                    zShift += zBump;
                }
            }
            poseStack.popPose(); */
        }
        poseStack.popPose();
    }


    private record BarProperties(float barLength, float barHeight, float capPercentage, int alpha, int light) {
        public static float capOffset;
        public static float bodyLength; //actually half
        public static float halfBarHeight;
        public static float slope;

        public BarProperties {
            capOffset = barLength * capPercentage;
            bodyLength = barLength - capOffset;
            halfBarHeight = barHeight / 2f;
            slope = halfBarHeight / capOffset;
        }
    }

    /***
     *
     * @param from Percentage to start at
     * @param to
     * @param color 0xff000000 | r << 16 | g << 8 | b  is the int representation of an rgb color
     */
    private static void renderHealthBar(float length, float from, float to, int color, BarProperties properties, PoseStack poseStack, VertexConsumer builder) {

        if (from == to) {
            return;
        }

        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;


        /***
         *      Vertex breakdown because I can't brain properly
         *     A1                B4
         *   L  /-------------\   R
         *      \_____________/
         *     C2                D3
         *     numbers indicate build order, found through testing
         *     height go up = vertex go down
         *     so A would be 0, 0 and C would be 0, 4
         */


        // Trading computation for memory :D
        // I think making them final means they will only be computed once, hopefully globally?
        Matrix4f lastPose = poseStack.last().pose();

        float start = properties.barLength() * from * 0.02f - properties.barLength();
        float end = properties.barLength() * to * 0.02f - properties.barLength();

        float bodyStart = Math.max(start, -properties.bodyLength);
        float bodyEnd = Math.min(end, properties.bodyLength);


        // Left cap
        // All of this just to display some funny triangles...........
        if (start < -properties.bodyLength) {

            float vOffset = -properties.slope * (start + properties.bodyLength);
            float vOffsetBase = 0;

            // This culling might not be important
            if (end < -properties.bodyLength) {
                vOffsetBase = -properties.slope * (end + properties.bodyLength);
            }

            float capBaseStart = Math.min(end, -properties.bodyLength);

            builder.vertex(lastPose, start, vOffset, 0.001F).color(r, g, b, properties.alpha).uv(1.0F, 1.0F).uv2(properties.light).endVertex();
            builder.vertex(lastPose, start, properties.barHeight - vOffset, 0.001F).color(r, g, b, properties.alpha).uv(0.0F, 1.0F).uv2(properties.light).endVertex();
            builder.vertex(lastPose, capBaseStart, properties.barHeight - vOffsetBase, 0.001F).color(r, g, b, properties.alpha).uv(1.0F, 1.0F).uv2(properties.light).endVertex();
            builder.vertex(lastPose, capBaseStart, vOffsetBase, 0.001F).color(r, g, b, properties.alpha).uv(1.0F, 0.75F).uv2(properties.light).endVertex();

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

            builder.vertex(lastPose, capBaseStart, vOffsetBase, 0.001F).color(r, g, b, properties.alpha).uv(1.0F, 0.75F).uv2(properties.light).endVertex();
            builder.vertex(lastPose, capBaseStart, properties.barHeight - vOffsetBase, 0.001F).color(r, g, b, properties.alpha).uv(1.0F, 1.0F).uv2(properties.light).endVertex();
            builder.vertex(lastPose, end, properties.barHeight - vOffset, 0.001F).color(r, g, b, properties.alpha).uv(0.0F, 1.0F).uv2(properties.light).endVertex();
            builder.vertex(lastPose, end, vOffset, 0.001F).color(r, g, b, properties.alpha).uv(1.0F, 1.0F).uv2(properties.light).endVertex();

        }

        // Body
        builder.vertex(lastPose, bodyStart, 0, 0.001F).color(r, g, b, properties.alpha).uv(0.0F, 0.75F).uv2(properties.light).endVertex();
        builder.vertex(lastPose, bodyStart, properties.barHeight, 0.001F).color(r, g, b, properties.alpha).uv(0.0F, 1.0F).uv2(properties.light).endVertex();
        builder.vertex(lastPose, bodyEnd, properties.barHeight, 0.001F).color(r, g, b, properties.alpha).uv(1.0F, 1.0F).uv2(properties.light).endVertex();
        builder.vertex(lastPose, bodyEnd, 0, 0.001F).color(r, g, b, properties.alpha).uv(1.0F, 0.75F).uv2(properties.light).endVertex();

        //TODO: move all not strictly rendering (i.e. health bar percentages) code to HealthBarCapability; this will allow for the implementation of the buffer indicator
        // actually no that logic ashoul not bwe run every tick only when looking at entity for which there is a guars statement i guess the capability will literally just be for storage of previous health
        //TODO: Reference ToroHealthBar to start creating the elemental damage indicators
        //  dev mods to reduce or eliminatw build time

    }

    private static void renderIcon(ItemStack icon, PoseStack poseStack, MultiBufferSource buffers, float globalScale, float halfSize, float leftShift, float zShift) {
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
                    .renderStatic(icon, ItemTransforms.TransformType.NONE,
                            0xF000F0, OverlayTexture.NO_OVERLAY, poseStack, buffers, 0);
            poseStack.popPose();
        }
    }
}