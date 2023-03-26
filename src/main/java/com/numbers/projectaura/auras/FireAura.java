package com.numbers.projectaura.auras;

import com.numbers.projectaura.ProjectAura;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public final class FireAura implements IElementalAura {
    public static final ResourceLocation ICON = new ResourceLocation(ProjectAura.MOD_ID, "textures/auras/fire-blank.png");
    private static final int COLOR = 0xff000000 | 255 << 16 | 125 << 8 | 30;

    @Override
    public void renderAura() {
    }

    @Override
    public void applyEffects(LivingEntity entity) {

    }

    @Override
    public boolean isVisible() {
        return true;
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
