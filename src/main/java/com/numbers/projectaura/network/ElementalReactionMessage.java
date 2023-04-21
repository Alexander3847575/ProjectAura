package com.numbers.projectaura.network;

import com.numbers.projectaura.event.ElementalReactionEvent;
import com.numbers.projectaura.reactions.IElementalReaction;
import com.numbers.projectaura.reactions.ReactionData;
import com.numbers.projectaura.registries.AuraRegistry;
import com.numbers.projectaura.registries.ReactionRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class ElementalReactionMessage {

    private int entityID;
    private ReactionData reactionData;

    public ElementalReactionMessage() {
        this.entityID = 0;
        this.reactionData = ReactionData.builder().build();
    }

    public ElementalReactionMessage(LivingEntity entity, ReactionData reactionData) {
        this.entityID = entity.getId();
        this.reactionData = reactionData;
    }

    public static void serialize(final ElementalReactionMessage message, final FriendlyByteBuf buf) {
        buf.writeVarInt(message.entityID);
        String out = "";
        out += AuraRegistry.AURAS.get().getResourceKey(message.reactionData.getAppliedAura()).get().location() + ";" + AuraRegistry.AURAS.get().getResourceKey(message.reactionData.getBaseAura()).get().location() + ";" + message.reactionData.getOutputDamage();
        buf.writeUtf(out);
    }

    public static ElementalReactionMessage deserialize(final FriendlyByteBuf buf) {
        final ElementalReactionMessage message = new ElementalReactionMessage();
        message.entityID = buf.readVarInt();
        String raw = buf.readUtf();

        // Do not parse empty auras; that causes an error
        // Everything should still be initialized in the constructor
        if (raw.equals("")) {
            return message;
        }

        String[] data = raw.split(";");
        message.reactionData = ReactionData.builder()
                .appliedAura(AuraRegistry.AURAS.get().getValue(new ResourceLocation(data[0])))
                .baseAura(AuraRegistry.AURAS.get().getValue(new ResourceLocation(data[1])))
                .outputDamage(Float.parseFloat(data[2]))
                .build();

        return message;
    }


    public static class Handler implements BiConsumer<ElementalReactionMessage, Supplier<NetworkEvent.Context>> {
        @Override
        public void accept(final ElementalReactionMessage message, final Supplier<NetworkEvent.Context> contextSupplier) {

            final NetworkEvent.Context context = contextSupplier.get();

            context.enqueueWork(() -> {

                if (Minecraft.getInstance().level == null) {
                    return;
                }

                Entity entity = Minecraft.getInstance().level.getEntity(message.entityID);

                if (!(entity instanceof LivingEntity living)) {
                    return;
                }

                IElementalReaction<?, ?> reaction = ReactionRegistry.getReaction(message.reactionData.getAppliedAura(), message.reactionData.getBaseAura());

                assert reaction != null;

                net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new ElementalReactionEvent(living, reaction, message.reactionData));

            });

            context.setPacketHandled(true);

        }
    }
}
