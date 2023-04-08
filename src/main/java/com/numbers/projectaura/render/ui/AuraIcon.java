package com.numbers.projectaura.render.ui;

import com.numbers.projectaura.animation.effects.Effect;
import com.numbers.projectaura.auras.IElementalAura;
import com.numbers.projectaura.registries.AuraRegistry;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.PackagePrivate;

import java.util.ArrayList;

@Builder
public class AuraIcon {

    @Builder.Default
    private IElementalAura auraType = AuraRegistry.FIRE.get();
    @Getter @Builder.Default
    private ArrayList<Effect> effects = new ArrayList<>();
    @Getter
    @PackagePrivate
    private boolean shouldRemoveFromQueue = false;

}
