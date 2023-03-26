package com.numbers.projectaura.auras;

import com.numbers.projectaura.ProjectAura;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class WaterAura implements IElementalAura {
    public static final ResourceLocation ICON = new ResourceLocation(ProjectAura.MOD_ID, "textures/auras/water-blank.png");
    private static final int COLOR = 0xff000000 | 0 << 16 | 200 << 8 | 255;



    @Override
    public void renderAura() {

    }

    @Override
    public void applyEffects(LivingEntity entity) {

    }

    @Override
    public boolean isVisible() {
        return false;
    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public ResourceLocation getIcon() {
        return ICON;
    }

    @Override
    public int getColor() {
        return COLOR;
    }


}
