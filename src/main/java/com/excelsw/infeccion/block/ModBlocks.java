package com.excelsw.infeccion.block;

import com.excelsw.infeccion.InfeccionMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

public final class ModBlocks {
    public static final Block INFECTED_DIRT = register("infected_dirt", new InfectedBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).strength(0.6F)
                    .sound(SoundType.SCULK).randomTicks()));

    public static final Block INFECTED_STONE = register("infected_stone", new InfectedBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_PURPLE).strength(1.5F, 6.0F)
                    .sound(SoundType.SCULK_CATALYST).requiresCorrectToolForDrops().randomTicks()));

    public static final Block INFECTED_LOG = register("infected_log", new InfectedLogBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_MAGENTA).strength(2.0F)
                    .sound(SoundType.STEM).randomTicks()));

    public static final Block INFECTED_LEAVES = register("infected_leaves", new InfectedBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PINK).strength(0.2F)
                    .sound(SoundType.SLIME_BLOCK).noOcclusion().randomTicks()
                    .isSuffocating((state, level, pos) -> false)
                    .isViewBlocking((state, level, pos) -> false)));

    public static final Block INFECTION_HIVE = register("infection_hive", new InfectionHiveBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).strength(5.0F, 12.0F)
                    .sound(SoundType.SCULK_SHRIEKER).lightLevel(state -> 9)
                    .requiresCorrectToolForDrops().randomTicks()));

    public static final Block INFECTED_GROWTH = register("infected_growth", new InfectedGrowthBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).noCollission().instabreak()
                    .sound(SoundType.SCULK_VEIN).lightLevel(state -> 5).replaceable()
                    .pushReaction(PushReaction.DESTROY)));

    public static final Block PURIFIER = register("purifier", new PurifierBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.DIAMOND).strength(3.0F, 9.0F)
                    .sound(SoundType.AMETHYST).lightLevel(state -> 15).requiresCorrectToolForDrops()));

    private ModBlocks() {
    }

    private static Block register(String name, Block block) {
        return Registry.register(BuiltInRegistries.BLOCK, InfeccionMod.id(name), block);
    }

    public static void register() {
        // Carga la clase para registrar los bloques.
    }
}
