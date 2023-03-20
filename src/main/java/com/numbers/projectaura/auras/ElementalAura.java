package com.numbers.projectaura.auras;

import lombok.Getter;

public class ElementalAura {

    @Getter
    public static final boolean isVisible = true;

    /***
     * Reacts this aura type with the applied one.
     * @param appliedAura
     * @param strength1
     * @param strength2
     * @return Returns a boolean that indicates whether the reaction was successful or not.
     */
    public boolean react(ElementalAura appliedAura, double strength1, double strength2) {
        return false;
    }

    //ResourceLocation icon = new ResourceLocation(ProjectAura.MOD_ID, "textures/auras/empty.png");

}
