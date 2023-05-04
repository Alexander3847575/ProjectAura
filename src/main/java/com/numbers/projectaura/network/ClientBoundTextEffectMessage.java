package com.numbers.projectaura.network;

import com.numbers.projectaura.ui.DamageNumberParticle;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class ClientBoundTextEffectMessage {

    private final int entityID;
    private final String effectText;
    private final int effectColor;

    public ClientBoundTextEffectMessage(int entityID, String effectText, int effectColor) {
        this.entityID = entityID;
        this.effectText = effectText;
        this.effectColor = effectColor;
    }

    public static void serialize(final ClientBoundTextEffectMessage message, final FriendlyByteBuf buf) {
        buf.writeVarInt(message.entityID);
        buf.writeUtf(message.effectText);
        buf.writeVarInt(message.effectColor);
    }

    public static ClientBoundTextEffectMessage deserialize(final FriendlyByteBuf buf) {
        return new ClientBoundTextEffectMessage(buf.readVarInt(), buf.readUtf(), buf.readVarInt());
    }


    // TODO: use sendParticle instead of manual packets
    public static class Handler implements BiConsumer<ClientBoundTextEffectMessage, Supplier<NetworkEvent.Context>> {
        @Override
        public void accept(final ClientBoundTextEffectMessage message, final Supplier<NetworkEvent.Context> contextSupplier) {

            final NetworkEvent.Context context = contextSupplier.get();

            context.enqueueWork(() -> {

                if (Minecraft.getInstance().level == null) {
                    return;
                }

                Entity entity = Minecraft.getInstance().level.getEntity(message.entityID);

                if (!(entity instanceof LivingEntity living)) {
                    return;
                }

                entity.getLevel().addParticle(new DamageNumberParticle.DamageParticleOptions(message.effectText, message.effectColor, false), entity.getX(), entity.getY(), entity.getZ(), message.entityID, 1, 1);

            });

            context.setPacketHandled(true);

        }
    }
}
