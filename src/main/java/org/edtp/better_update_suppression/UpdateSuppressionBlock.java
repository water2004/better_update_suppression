package org.edtp.better_update_suppression;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.block.WireOrientation;
import org.jetbrains.annotations.Nullable;

public class UpdateSuppressionBlock extends Block {
    public static final BooleanProperty ACTIVED = BooleanProperty.of("actived");

    public UpdateSuppressionBlock(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(ACTIVED, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(ACTIVED);
    }

    // 对于 1.20.5 以下版本，方法参数应该是“BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit”
    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient()) {
//            my_state=!my_state;
            var active_state=state.get(ACTIVED);
            active_state=!active_state;
            player.playSound(SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE, 1, 1);
            world.setBlockState(pos, state.with(ACTIVED, active_state));
            if(active_state)
            {
                player.sendMessage(Text.literal("update suppression on at: "+pos), false);
            }
            else
            {
                player.sendMessage(Text.literal("update suppression off at: "+pos), false);
            }
        }

        return ActionResult.SUCCESS;
    }

    @Override
    protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, @Nullable WireOrientation wireOrientation, boolean notify) {
        // 确保逻辑只在服务器端运行
        if (!world.isClient()) {

            if (state.get(ACTIVED)) {

                // 向附近玩家发送一条消息，告知即将发生抑制
                var players = world.getPlayers();
                for (PlayerEntity player : players) {
                    if (player.getBlockPos().isWithinDistance(pos, 16.0)) {
                        player.sendMessage(Text.literal("Update suppressed at: "+pos), false);
                    }
                }

                // 抛出我们的自定义异常来中断更新链
//                throw new UpdateSuppressionException("Intentionally suppressing block update chain at " + pos.toString());
                throw new StackOverflowError("Intentionally suppressing block update chain at " + pos.toString());
            }
        }

        // 如果条件不满足，正常执行父类逻辑
        super.neighborUpdate(state, world, pos, sourceBlock, wireOrientation, notify);
    }
}