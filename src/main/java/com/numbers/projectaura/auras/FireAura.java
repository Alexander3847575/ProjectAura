package com.numbers.projectaura.auras;

import com.numbers.projectaura.ProjectAura;
import net.minecraft.resources.ResourceLocation;

public class FireAura extends ElementalAura {
    public ResourceLocation icon = new ResourceLocation(ProjectAura.MOD_ID, "textures/auras/fire.png");

    @Override
    public boolean react(ElementalAura appliedAura, double d1, double d2) {
        return false;
    }
}
