package org.edtp.better_update_suppression;

import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.core.api.utils.PolymerClientDecoded;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.redstone.Orientation;
import org.jetbrains.annotations.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

public class UpdateSuppressionBlock extends Block implements PolymerBlock, PolymerClientDecoded {
    public static final BooleanProperty ACTIVED = BooleanProperty.create("actived");

    public UpdateSuppressionBlock(Properties settings) {
        super(settings);
        registerDefaultState(defaultBlockState().setValue(ACTIVED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ACTIVED);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            if (player instanceof ServerPlayer serverPlayer
                    && !ClientModCompatibility.canUseProtectedContent(serverPlayer)) {
                ClientModCompatibility.sendClientRequiredMessage(serverPlayer);
                return InteractionResult.FAIL;
            }

            var active_state = !state.getValue(ACTIVED);
            player.playSound(SoundEvents.RESPAWN_ANCHOR_CHARGE, 1, 1);
            if (active_state) {
                player.sendSystemMessage(Component.literal("update suppression on at: " + pos));
            } else {
                player.sendSystemMessage(Component.literal("update suppression off at: " + pos));
            }
            // Only synchronize the visual state. Activating the suppressor must not
            // itself notify neighbouring blocks and start an update chain.
            level.setBlock(pos, state.setValue(ACTIVED, active_state), Block.UPDATE_CLIENTS);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide()
                && player instanceof ServerPlayer serverPlayer
                && !ClientModCompatibility.canUseProtectedContent(serverPlayer)) {
            ClientModCompatibility.sendClientRequiredMessage(serverPlayer);
            return InteractionResult.FAIL;
        }

        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, PacketContext context) {
        return ClientModCompatibility.hasClientSupport(context)
                ? state
                : Blocks.BEDROCK.defaultBlockState();
    }

    @Override
    public BlockState getPolymerBreakEventBlockState(BlockState state, PacketContext context) {
        return getPolymerBlockState(state, context);
    }

    @Override
    public boolean canSyncRawToClient(PacketContext context) {
        return ClientModCompatibility.hasClientSupport(context);
    }

    @Override
    public boolean handleMiningOnServer(ItemStack tool, BlockState state, BlockPos pos, ServerPlayer player) {
        return !ClientModCompatibility.canUseProtectedContent(player);
    }

    @Override
    public boolean isPolymerBlockInteraction(BlockState state, ServerPlayer player, InteractionHand hand,
            ItemStack stack, net.minecraft.server.level.ServerLevel level, BlockHitResult hitResult,
            InteractionResult actionResult) {
        return !ClientModCompatibility.canUseProtectedContent(player);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, @Nullable Orientation orientation, boolean isMoving) {
        if (!level.isClientSide()) {
            if (state.getValue(ACTIVED)) {
                // 向附近玩家发送一条消息，告知即将发生抑制
                var players = level.players();
                for (Player player : players) {
                    if (player.blockPosition().closerThan(pos, 16.0)) {
                        player.sendSystemMessage(Component.literal("Update suppressed at: " + pos));
                    }
                }
                // 抛出 StackOverflowError 来中断更新链
                throw new StackOverflowError("Intentionally suppressing block update chain at " + pos.toString());
            }
        }
        // 如果条件不满足，正常执行父类逻辑
        super.neighborChanged(state, level, pos, neighborBlock, orientation, isMoving);
    }
}
