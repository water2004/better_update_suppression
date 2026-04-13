package org.edtp.better_update_suppression;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

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

    // 对于 1.20.5 以下版本，方法参数应该是“BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit”
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (!world.isClientSide()) {
//            my_state=!my_state;
            var active_state=state.getValue(ACTIVED);
            active_state=!active_state;
            player.playSound(SoundEvents.RESPAWN_ANCHOR_CHARGE, 1, 1);
            world.setBlockAndUpdate(pos, state.setValue(ACTIVED, active_state));
            if(active_state)
            {
                player.displayClientMessage(Component.literal("update suppression on at: "+pos), false);
            }
            else
            {
                player.displayClientMessage(Component.literal("update suppression off at: "+pos), false);
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    protected void neighborChanged(BlockState state, Level world, BlockPos pos, Block sourceBlock, @Nullable Orientation wireOrientation, boolean notify) {
        // 确保逻辑只在服务器端运行
        if (!world.isClientSide()) {

            if (state.getValue(ACTIVED)) {

                // 向附近玩家发送一条消息，告知即将发生抑制
                var players = world.players();
                for (Player player : players) {
                    if (player.blockPosition().closerThan(pos, 16.0)) {
                        player.displayClientMessage(Component.literal("Update suppressed at: "+pos), false);
                    }
                }

                // 抛出我们的自定义异常来中断更新链
//                throw new UpdateSuppressionException("Intentionally suppressing block update chain at " + pos.toString());
                throw new StackOverflowError("Intentionally suppressing block update chain at " + pos.toString());
            }
        }

        // 如果条件不满足，正常执行父类逻辑
        super.neighborChanged(state, world, pos, sourceBlock, wireOrientation, notify);
    }
}