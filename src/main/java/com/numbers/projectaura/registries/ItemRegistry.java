package com.numbers.projectaura.registries;

import com.numbers.projectaura.item.ElementalApplicatorItem;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraft.world.item.Item;

import static com.numbers.projectaura.ProjectAura.REGISTRATE;

public class ItemRegistry {

    public static final RegistryEntry<Item> BUDDY = REGISTRATE.item("buddy", Item::new)
            .properties(p -> p.stacksTo(1))
            .register();

    public static final RegistryEntry<ElementalApplicatorItem> FIRE_STICK = REGISTRATE.item("fire_stick", (properties) -> new ElementalApplicatorItem(() -> AuraRegistry.FIRE.get(), "water_stick", properties))
            .properties(p -> p.stacksTo(1))
            .register();

    public static final RegistryEntry<ElementalApplicatorItem> WATER_STICK = REGISTRATE.item("water_stick", (properties) -> new ElementalApplicatorItem(() -> AuraRegistry.WATER.get(), "fire_stick", properties))
            .properties(p -> p.stacksTo(1))
            .register();


    public static void register() { }

}
