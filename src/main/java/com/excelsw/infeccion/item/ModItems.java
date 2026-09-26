package com.excelsw.infeccion.item;

import com.excelsw.infeccion.InfeccionMod;
import com.excelsw.infeccion.block.ModBlocks;
import com.excelsw.infeccion.entity.ModEntities;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.Block;

public final class ModItems {
    public static final Item INFECTED_DIRT = block(ModBlocks.INFECTED_DIRT);
    public static final Item INFECTED_STONE = block(ModBlocks.INFECTED_STONE);
    public static final Item INFECTED_LOG = block(ModBlocks.INFECTED_LOG);
    public static final Item INFECTED_LEAVES = block(ModBlocks.INFECTED_LEAVES);
    public static final Item INFECTION_HIVE = block(ModBlocks.INFECTION_HIVE);
    public static final Item PURIFIER = block(ModBlocks.PURIFIER);

    public static final Item INFECTION_SPORE = register("infection_spore",
            new SporeItem(new Item.Properties()));
    public static final Item VACCINE = register("vaccine",
            new VaccineItem(new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON)));
    public static final Item PURIFICATION_BOMB = register("purification_bomb",
            new PurificationBombItem(new Item.Properties().stacksTo(16).rarity(Rarity.RARE)));
    public static final Item INFECTED_SPAWN_EGG = register("infected_spawn_egg",
            new SpawnEggItem(ModEntities.INFECTED, 0x3B1446, 0x9BE03C, new Item.Properties()));

    public static final CreativeModeTab TAB = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
            InfeccionMod.id("infeccion"), FabricItemGroup.builder()
                    .icon(() -> new ItemStack(INFECTION_HIVE))
                    .title(Component.translatable("itemGroup.infeccion"))
                    .displayItems((params, output) -> {
                        output.accept(INFECTION_SPORE);
                        output.accept(VACCINE);
                        output.accept(PURIFICATION_BOMB);
                        output.accept(PURIFIER);
                        output.accept(INFECTION_HIVE);
                        output.accept(INFECTED_DIRT);
                        output.accept(INFECTED_STONE);
                        output.accept(INFECTED_LOG);
                        output.accept(INFECTED_LEAVES);
                        output.accept(INFECTED_SPAWN_EGG);
                    })
                    .build());

    private ModItems() {
    }

    private static Item block(Block block) {
        return Registry.register(BuiltInRegistries.ITEM, BuiltInRegistries.BLOCK.getKey(block),
                new BlockItem(block, new Item.Properties()));
    }

    private static Item register(String name, Item item) {
        return Registry.register(BuiltInRegistries.ITEM, InfeccionMod.id(name), item);
    }

    public static void register() {
        // Carga la clase para registrar los objetos y la pestaña creativa.
    }
}
