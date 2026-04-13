package org.edtp.better_update_suppression;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import java.util.function.Function;

public class UpdateSuppressionBlockFinal {
    public static final Block UPDATE_SUPPRESSION_BLOCK = register("update_suppression_block",
            UpdateSuppressionBlock::new, BlockBehaviour.Properties.of().strength(0.1f));

    private static Block register(String path, Function<BlockBehaviour.Properties, Block> factory,
            BlockBehaviour.Properties settings) {
        final Identifier identifier = Identifier.fromNamespaceAndPath("better_update_suppression", path);
        final ResourceKey<Block> registryKey = ResourceKey.create(Registries.BLOCK, identifier);

        // 设置 Block 的 ID
        settings.setId(registryKey);

        // 使用 Registry.register 进行注册
        final Block block = Registry.register(BuiltInRegistries.BLOCK, registryKey, factory.apply(settings));

        final ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, identifier);
        Registry.register(BuiltInRegistries.ITEM, itemKey, new BlockItem(block, new Item.Properties().setId(itemKey)));

        return block;
    }

    public static void init() {
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.REDSTONE_BLOCKS).register(output -> {
            output.accept(UPDATE_SUPPRESSION_BLOCK);
        });
    }
}