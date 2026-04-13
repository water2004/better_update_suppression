package org.edtp.better_update_suppression;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.redstone.Orientation;
import org.jetbrains.annotations.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

public class UpdateSuppressionBlock extends Block {
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
            var active_state = !state.getValue(ACTIVED);
            player.playSound(SoundEvents.RESPAWN_ANCHOR_CHARGE, 1, 1);
            if (active_state) {
                player.sendSystemMessage(Component.literal("update suppression on at: " + pos));
            } else {
                player.sendSystemMessage(Component.literal("update suppression off at: " + pos));
            }
            level.setBlockAndUpdate(pos, state.setValue(ACTIVED, active_state));
        }
        return InteractionResult.SUCCESS;
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