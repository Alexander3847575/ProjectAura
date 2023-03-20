package com.numbers.projectaura.registries;

import com.numbers.projectaura.ProjectAura;
import com.numbers.projectaura.item.AuraApplicatorItem;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraft.world.item.Item;

import static com.numbers.projectaura.ProjectAura.REGISTRATE;

public class ItemRegistry {

    public static final RegistryEntry<Item> BUDDY = REGISTRATE.item("buddy", Item::new)
            .properties(p -> p.stacksTo(1))
            .register();

    public static final RegistryEntry<AuraApplicatorItem> FIRE_STICK = REGISTRATE.item("fire_stick", AuraApplicatorItem::new)
            .properties(p -> p.stacksTo(1))
            .register();


    public static void register() { }

}