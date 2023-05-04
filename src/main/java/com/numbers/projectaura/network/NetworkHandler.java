package com.numbers.projectaura.network;

import com.numbers.projectaura.ProjectAura;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = net.minecraftforge.network.NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ProjectAura.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );
    private int nextMessageID = 0;

    // TODO: make interface for packets to simplify registration
    public void registerPackets() {
        this.registerPacket(ClientBoundAuraSyncMessage.class, ClientBoundAuraSyncMessage::serialize, ClientBoundAuraSyncMessage::deserialize, new ClientBoundAuraSyncMessage.Handler());
        this.registerPacket(ClientBoundReactionMessage.class, ClientBoundReactionMessage::serialize, ClientBoundReactionMessage::deserialize, new ClientBoundReactionMessage.Handler());
        this.registerPacket(ClientBoundTextEffectMessage.class, ClientBoundTextEffectMessage::serialize, ClientBoundTextEffectMessage::deserialize, new ClientBoundTextEffectMessage.Handler());
    }

    public <T> void registerPacket(final Class<T> message, final BiConsumer<T, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, T> decoder, BiConsumer<T, Supplier<NetworkEvent.Context>> handler) {
        INSTANCE.registerMessage(this.nextMessageID++, message, encoder, decoder, handler);
    }



}
