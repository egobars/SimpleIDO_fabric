package net.egobars.simpleido;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.BlockState;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoorBlock;
import net.minecraft.fluid.Fluids;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.event.GameEvent;
import net.minecraft.state.property.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleIDO implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("simpleido");

	@Override
	public void onInitialize() {
		UseBlockCallback.EVENT.register((player, world, hand, hitresult) -> {
            BlockPos pos = hitresult.getBlockPos();
            BlockState state = world.getBlockState(pos);
            if (!player.isSneaking() || (player.getMainHandStack().isEmpty() && player.getOffHandStack().isEmpty())) {
                if (state.getBlock() == Blocks.IRON_DOOR) {
                    if (hand == Hand.MAIN_HAND) {
                        DoorBlock doorBlock = (DoorBlock) state.getBlock();
                        state = (BlockState)state.cycle(Properties.OPEN);
                        world.setBlockState(pos, state, Block.NOTIFY_LISTENERS | Block.REDRAW_ON_MAIN_THREAD);
						world.playSound(player, pos, state.get(Properties.OPEN) ? SoundEvents.BLOCK_IRON_DOOR_OPEN : SoundEvents.BLOCK_IRON_DOOR_CLOSE, SoundCategory.BLOCKS, 1.0f, world.getRandom().nextFloat() * 0.1f + 0.9f);
                        world.emitGameEvent(player, doorBlock.isOpen(state) ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pos);
                    }
                    return ActionResult.success(world.isClient);
                } else if (state.getBlock() == Blocks.IRON_TRAPDOOR) {
                    if (hand == Hand.MAIN_HAND) {
						state = (BlockState)state.cycle(Properties.OPEN);
						world.setBlockState(pos, state, Block.NOTIFY_LISTENERS);
						if (state.get(Properties.WATERLOGGED).booleanValue()) {
							world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
						}
						world.playSound(player, pos, state.get(Properties.OPEN) ? SoundEvents.BLOCK_IRON_TRAPDOOR_OPEN : SoundEvents.BLOCK_IRON_TRAPDOOR_CLOSE, SoundCategory.BLOCKS, 1.0f, world.getRandom().nextFloat() * 0.1f + 0.9f);
        				world.emitGameEvent(player, state.get(Properties.OPEN) ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pos);
                    }
                    return ActionResult.success(world.isClient);
                }
            }
            return ActionResult.PASS;
        });
	}
}