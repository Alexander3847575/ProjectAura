package com.numbers.projectaura.registries;

import com.numbers.projectaura.ProjectAura;
import com.numbers.projectaura.network.AuraSyncMessage;
import com.numbers.projectaura.network.ElementalReactionMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class NetworkRegistry {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = net.minecraftforge.network.NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ProjectAura.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );
    private int nextMessageID = 0;

    public void registerPackets() {
        this.registerPacket(AuraSyncMessage.class, AuraSyncMessage::serialize, AuraSyncMessage::deserialize, new AuraSyncMessage.Handler());
        this.registerPacket(ElementalReactionMessage.class, ElementalReactionMessage::serialize, ElementalReactionMessage::deserialize, new ElementalReactionMessage.Handler());
    }

    public <T> void registerPacket(final Class<T> message, final BiConsumer<T, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, T> decoder, BiConsumer<T, Supplier<NetworkEvent.Context>> handler) {
        INSTANCE.registerMessage(this.nextMessageID++, message, encoder, decoder, handler);
    }



}
