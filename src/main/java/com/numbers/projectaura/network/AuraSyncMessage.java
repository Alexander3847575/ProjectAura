package com.numbers.projectaura.network;

import com.numbers.projectaura.auras.IElementalAura;
import com.numbers.projectaura.capability.AuraCapability;
import com.numbers.projectaura.registries.AuraRegistry;
import com.numbers.projectaura.capability.CapabilityHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class AuraSyncMessage {

    private int entityID;
    private boolean isFrozen;
    private LinkedHashMap<IElementalAura, Double> auras;

    public AuraSyncMessage() {
        this.entityID = 0;
        this.auras = new LinkedHashMap<>();
    }

    public AuraSyncMessage(LivingEntity entity, LinkedHashMap<IElementalAura, Double> auras) {
        this.entityID = entity.getId();
        this.auras = auras;
    }

    public static void serialize(final AuraSyncMessage message, final FriendlyByteBuf buf) {
        buf.writeVarInt(message.entityID);
        String out = "";
        for (Map.Entry<IElementalAura, Double> aura : message.auras.entrySet()) {
            if (AuraRegistry.AURAS.get().getResourceKey(aura.getKey()).isPresent()) {
                out += AuraRegistry.AURAS.get().getResourceKey(aura.getKey()).get().location() + "@" + aura.getValue() + ";";
            }
        }
        buf.writeUtf(out);
    }

    public static AuraSyncMessage deserialize(final FriendlyByteBuf buf) {
        final AuraSyncMessage message = new AuraSyncMessage();
        message.entityID = buf.readVarInt();
        String raw = buf.readUtf();

        // Do not parse empty auras; that causes an error
        // Everything should still be initialized in the constructor
        if (raw == "") {
            return message;
        }

        String[] auras = raw.split(";");
        Arrays.stream(auras).forEach((rawAura) -> {
            String[] values = rawAura.split("@");
            message.auras.put(AuraRegistry.AURAS.get().getValue(new ResourceLocation(values[0])), Double.valueOf(values[1]));
        });
        //message.isFrozen = buf.readBoolean();
        return message;
    }

    public static class Handler implements BiConsumer<AuraSyncMessage, Supplier<NetworkEvent.Context>> {
        @Override
        public void accept(final AuraSyncMessage message, final Supplier<NetworkEvent.Context> contextSupplier) {

            final NetworkEvent.Context context = contextSupplier.get();

            context.enqueueWork(() -> {

                if (Minecraft.getInstance().level == null) {
                    return;
                }

                Entity entity = Minecraft.getInstance().level.getEntity(message.entityID);

                if (!(entity instanceof LivingEntity living)) {
                    return;
                }

                AuraCapability auraCapability = CapabilityHandler.getCapability(living, CapabilityHandler.AURA_CAPABILITY);

                assert auraCapability != null;
                auraCapability.setAuras(message.auras);

            });

            context.setPacketHandled(true);

        }
    }

}
