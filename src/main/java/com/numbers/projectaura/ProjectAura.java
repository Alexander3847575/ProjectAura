package com.numbers.projectaura;

import com.mojang.logging.LogUtils;
import com.numbers.projectaura.event.ServerEventHandler;
import com.numbers.projectaura.registries.*;
import com.tterrag.registrate.Registrate;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.NewRegistryEvent;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(ProjectAura.MOD_ID)
public class ProjectAura {
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "projectaura";

    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    // Create the primary Registrate
    public static final Registrate REGISTRATE = Registrate.create(MOD_ID)
            .creativeModeTab("main", c -> c.icon(Items.STONE::getDefaultInstance), "Project Aura"); // Create main creative tab

    public ProjectAura()
    {

        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register all items
        ItemRegistry.register();

        // Register custom aura registry
        AuraRegistry.AURA_REGISTRY.register(bus);

        bus.addListener(EventPriority.LOWEST, ProjectAura::gatherData);
        bus.addListener(CapabilityRegistry::registerCapabilities);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new ServerEventHandler());
        MinecraftForge.EVENT_BUS.addGenericListener(Entity.class, CapabilityRegistry::attachEntityCapability);

    }

    @SubscribeEvent
    protected void onCommonSetup(NewRegistryEvent event) {

    }

    // Placeholder for future data generation; basic registration is done with Registrate
    public static void gatherData(GatherDataEvent event) {
        DataGenerator gen = event.getGenerator();
        PackOutput output = gen.getPackOutput();

    }

}
