package com.numbers.projectaura;

import com.mojang.logging.LogUtils;
import com.numbers.projectaura.capability.CapabilityHandler;
import com.numbers.projectaura.event.EventHandler;
import com.numbers.projectaura.network.NetworkHandler;
import com.numbers.projectaura.registries.*;
import com.tterrag.registrate.Registrate;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(ProjectAura.MOD_ID)
public final class ProjectAura {
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "projectaura";
    public static final ResourceLocation DEFAULT_RESOURCE_LOCATION = new ResourceLocation(MOD_ID, "textures/ui/oh_no.png");

    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    // Create the primary Registrate
    public static final Registrate REGISTRATE = Registrate.create(MOD_ID)
            .creativeModeTab("main", c -> c.icon(Items.STONE::getDefaultInstance), "Project Aura"); // Create main creative tab

    public ProjectAura()
    {

        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register custom aura and environmental applicator registry
        AuraRegistry.AURA_REGISTRY.register(bus);
        EnvironmentalApplicatorRegistry.ENVIRONMENTAL_APPLICATOR_REGISTRY.register(bus);
        ParticleRegistry.PARTICLE_TYPE_REGISTRY.register(bus);

        // Register all items
        ItemRegistry.register();

        new NetworkHandler().registerPackets();

        bus.addListener(EventPriority.LOWEST, ProjectAura::gatherData);
        bus.addListener(EventPriority.LOW, ProjectAura::registerReactions);
        bus.addListener(CapabilityHandler::registerCapabilities);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new EventHandler());
        MinecraftForge.EVENT_BUS.addGenericListener(Entity.class, CapabilityHandler::attachEntityCapability);
    }

    // idk it just needs to run after aura registration
    public static void registerReactions(FMLLoadCompleteEvent e) {
        ReactionRegistry.register();
    }


    // Placeholder for future data generation; basic registration is done with Registrate
    public static void gatherData(GatherDataEvent event) {
        DataGenerator gen = event.getGenerator();
        PackOutput output = gen.getPackOutput();

    }

}
