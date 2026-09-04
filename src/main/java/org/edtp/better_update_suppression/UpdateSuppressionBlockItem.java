package org.edtp.better_update_suppression;

import eu.pb4.polymer.core.api.item.PolymerBlockItem;
import eu.pb4.polymer.core.api.utils.PolymerClientDecoded;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** A block item that is represented by bedrock to clients without this mod. */
public class UpdateSuppressionBlockItem extends PolymerBlockItem implements PolymerClientDecoded {
    public UpdateSuppressionBlockItem(Block block, Item.Properties properties) {
        super(block, properties, Items.BEDROCK);
    }

    @Override
    public Item getPolymerItem(ItemStack stack, PacketContext context) {
        return ClientModCompatibility.hasClientSupport(context) ? this : Items.BEDROCK;
    }

    @Override
    public boolean canSyncRawToClient(PacketContext context) {
        return ClientModCompatibility.hasClientSupport(context);
    }

    @Override
    public boolean isPolymerBlockInteraction(BlockState state, ServerPlayer player, InteractionHand hand,
            ItemStack stack, ServerLevel level, BlockHitResult hitResult, InteractionResult actionResult) {
        return !ClientModCompatibility.canUseProtectedContent(player);
    }

    @Override
    public boolean isIgnoringBlockInteractionPlaySoundExceptedEntity(BlockState state, ServerPlayer player,
            InteractionHand hand, ItemStack stack, ServerLevel level, BlockHitResult hitResult) {
        return !ClientModCompatibility.canUseProtectedContent(player);
    }

    @Override
    public boolean isPolymerItemInteraction(ServerPlayer player, InteractionHand hand, ItemStack stack,
            ServerLevel level, InteractionResult actionResult) {
        return !ClientModCompatibility.canUseProtectedContent(player);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!context.getLevel().isClientSide()
                && context.getPlayer() instanceof ServerPlayer serverPlayer
                && !ClientModCompatibility.canUseProtectedContent(serverPlayer)) {
            ClientModCompatibility.sendClientRequiredMessage(serverPlayer);
            return InteractionResult.FAIL;
        }

        return super.useOn(context);
    }
}
