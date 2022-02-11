package net.egobars.simpleido;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.*;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.state.property.Properties;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.event.GameEvent;


public class SimpleIDO implements ModInitializer {
    @Override
    public void onInitialize() {
        UseBlockCallback.EVENT.register((player, world, hand, hitresult) -> {
            if (hand == Hand.MAIN_HAND) {
                BlockPos pos = hitresult.getBlockPos();
                BlockState state = world.getBlockState(pos);
                if (state.getBlock() == Blocks.IRON_DOOR) {
                    DoorBlock doorBlock = (DoorBlock) state.getBlock();
                    state = state.cycle(Properties.OPEN);
                    world.setBlockState(pos, state, 10);
                    world.syncWorldEvent(player, state.get(Properties.OPEN) ? 1005 : 1011, pos, 0);
                    world.emitGameEvent(player, doorBlock.isOpen(state) ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pos);
                    player.handSwinging = true;
                    return ActionResult.success(world.isClient);
                } else if (state.getBlock() == Blocks.IRON_TRAPDOOR) {
                    if (!world.isClient()) {
                        state = state.cycle(Properties.OPEN);
                        world.setBlockState(pos, state, Block.NOTIFY_LISTENERS);
                        if (state.get(Properties.WATERLOGGED)) {
                            world.createAndScheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
                        }
                    } else {
                        boolean is_open = state.get(Properties.OPEN);
                        if (is_open) world.syncWorldEvent(player, WorldEvents.IRON_TRAPDOOR_OPENS, pos, 0);
                        else world.syncWorldEvent(player, WorldEvents.IRON_TRAPDOOR_CLOSES, pos, 0);
                        world.emitGameEvent(player, is_open ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pos);
                    }
                    player.handSwinging = true;
                    return ActionResult.success(world.isClient);
                }
            }
            return ActionResult.PASS;
        });
    }
}