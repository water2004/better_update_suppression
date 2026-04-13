package org.edtp.better_update_suppression;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import java.util.function.Function;

public class UpdateSuppressionBlockFinal {
    public static final Block UPDATE_SUPPRESSION_BLOCK = register("update_suppression_block", UpdateSuppressionBlock::new, BlockBehaviour.Properties.of().strength(0.1f));

    private static Block register(String path, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties settings) {
        final Identifier identifier = Identifier.fromNamespaceAndPath("better_update_suppression", path);
        final ResourceKey<Block> registryKey = ResourceKey.create(Registries.BLOCK, identifier);

        final Block block = Blocks.register(registryKey, factory, settings);
        Items.registerBlock(block);
        return block;
    }

    public static void init() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.REDSTONE_BLOCKS).register(entries -> {
            entries.accept(UPDATE_SUPPRESSION_BLOCK);
        });
    }
}