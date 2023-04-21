package com.numbers.projectaura.event;

import com.numbers.projectaura.auras.applicator.ApplicationInstance;
import lombok.Getter;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event;

// TODO?
public class ElementalApplicationEvent extends Event {
    @Getter
    ApplicationInstance applicationInstance;
    @Getter
    LivingEntity appliedEntity;

}
