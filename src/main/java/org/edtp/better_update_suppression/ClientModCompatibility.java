package org.edtp.better_update_suppression;

import eu.pb4.polymer.networking.api.ContextByteBuf;
import eu.pb4.polymer.networking.api.PolymerNetworking;
import eu.pb4.polymer.networking.api.server.PolymerServerNetworking;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * Detects whether a connection has this mod's client-side code.
 *
 * <p>The payload is never sent. Registering it makes Polymer advertise a protocol
 * version during its optional handshake, which gives the server a capability check
 * without rejecting vanilla clients.</p>
 */
public final class ClientModCompatibility {
    private static final Identifier CLIENT_SUPPORT_ID = Identifier.fromNamespaceAndPath(
            "better_update_suppression", "client_support");
    private static final int CLIENT_SUPPORT_VERSION = 1;
    private static final Component CLIENT_REQUIRED_MESSAGE = Component.literal(
            "需要在客户端安装 Better Update Suppression 才能操作此方块 / Client mod required");

    private ClientModCompatibility() {
    }

    public static void registerProtocol() {
        PolymerNetworking.registerCommonVersioned(
                ClientSupportPayload.TYPE,
                CLIENT_SUPPORT_VERSION,
                ClientSupportPayload.CODEC);
    }

    public static boolean canUseProtectedContent(Player player) {
        return !(player instanceof ServerPlayer serverPlayer)
                || PolymerServerNetworking.getSupportedVersion(serverPlayer.connection, CLIENT_SUPPORT_ID)
                        >= CLIENT_SUPPORT_VERSION;
    }

    public static boolean hasClientSupport(PacketContext context) {
        return context != null
                && PolymerNetworking.getSupportedVersion(context.get(PacketContext.CONNECTION), CLIENT_SUPPORT_ID)
                        >= CLIENT_SUPPORT_VERSION;
    }

    public static void sendClientRequiredMessage(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendOverlayMessage(CLIENT_REQUIRED_MESSAGE);
        }
    }

    private record ClientSupportPayload() implements CustomPacketPayload {
        private static final ClientSupportPayload INSTANCE = new ClientSupportPayload();
        private static final Type<ClientSupportPayload> TYPE = new Type<>(CLIENT_SUPPORT_ID);
        private static final StreamCodec<ContextByteBuf, ClientSupportPayload> CODEC = StreamCodec.unit(INSTANCE);

        @Override
        public Type<ClientSupportPayload> type() {
            return TYPE;
        }
    }
}
