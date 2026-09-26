package com.excelsw.infeccion.item;

import com.excelsw.infeccion.InfeccionMod;
import com.excelsw.infeccion.block.ModBlocks;
import com.excelsw.infeccion.entity.ModEntities;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

import java.util.EnumMap;
import java.util.List;

public final class ModItems {
    public static final Holder<ArmorMaterial> GAS_MASK_MATERIAL = Registry.registerForHolder(
            BuiltInRegistries.ARMOR_MATERIAL, InfeccionMod.id("gas_mask"),
            new ArmorMaterial(Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                map.put(ArmorItem.Type.BOOTS, 1);
                map.put(ArmorItem.Type.LEGGINGS, 1);
                map.put(ArmorItem.Type.CHESTPLATE, 1);
                map.put(ArmorItem.Type.HELMET, 1);
                map.put(ArmorItem.Type.BODY, 1);
            }), 12, SoundEvents.ARMOR_EQUIP_LEATHER, () -> Ingredient.of(Items.LEATHER),
                    List.of(new ArmorMaterial.Layer(InfeccionMod.id("gas_mask"))), 0.0F, 0.0F));

    public static final Item INFECTED_DIRT = block(ModBlocks.INFECTED_DIRT);
    public static final Item INFECTED_STONE = block(ModBlocks.INFECTED_STONE);
    public static final Item INFECTED_LOG = block(ModBlocks.INFECTED_LOG);
    public static final Item INFECTED_LEAVES = block(ModBlocks.INFECTED_LEAVES);
    public static final Item INFECTED_GROWTH = block(ModBlocks.INFECTED_GROWTH);
    public static final Item INFECTION_HIVE = block(ModBlocks.INFECTION_HIVE);
    public static final Item PURIFIER = block(ModBlocks.PURIFIER);

    public static final Item INFECTION_SPORE = register("infection_spore",
            new SporeItem(new Item.Properties()));
    public static final Item INFECTED_HEART = register("infected_heart",
            new Item(new Item.Properties().rarity(Rarity.RARE)));
    public static final Item VACCINE = register("vaccine",
            new VaccineItem(new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON)));
    public static final Item PURIFICATION_BOMB = register("purification_bomb",
            new PurificationBombItem(new Item.Properties().stacksTo(16).rarity(Rarity.RARE)));
    public static final Item FLAMETHROWER = register("flamethrower",
            new FlamethrowerItem(new Item.Properties().durability(512).rarity(Rarity.UNCOMMON)));
    public static final Item GAS_MASK = register("gas_mask",
            new ArmorItem(GAS_MASK_MATERIAL, ArmorItem.Type.HELMET,
                    new Item.Properties().durability(ArmorItem.Type.HELMET.getDurability(12))));
    public static final Item SCANNER = register("scanner",
            new ScannerItem(new Item.Properties().stacksTo(1)));

    public static final Item INFECTED_SPAWN_EGG = egg("infected_spawn_egg", ModEntities.INFECTED, 0x3B1446, 0x9BE03C);
    public static final Item RUNNER_SPAWN_EGG = egg("infected_runner_spawn_egg", ModEntities.RUNNER, 0x5A1A2E, 0xE0463C);
    public static final Item BRUTE_SPAWN_EGG = egg("infected_brute_spawn_egg", ModEntities.BRUTE, 0x2A0E33, 0x6B1F7A);
    public static final Item SPITTER_SPAWN_EGG = egg("infected_spitter_spawn_egg", ModEntities.SPITTER, 0x2E4A1A, 0xB6F24A);
    public static final Item BLOATER_SPAWN_EGG = egg("infected_bloater_spawn_egg", ModEntities.BLOATER, 0x8C3A7A, 0xF0A0D8);

    public static final CreativeModeTab TAB = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
            InfeccionMod.id("infeccion"), FabricItemGroup.builder()
                    .icon(() -> new ItemStack(INFECTION_HIVE))
                    .title(Component.translatable("itemGroup.infeccion"))
                    .displayItems((params, output) -> {
                        output.accept(SCANNER);
                        output.accept(GAS_MASK);
                        output.accept(FLAMETHROWER);
                        output.accept(VACCINE);
                        output.accept(PURIFICATION_BOMB);
                        output.accept(PURIFIER);
                        output.accept(INFECTION_SPORE);
                        output.accept(INFECTED_HEART);
                        output.accept(INFECTION_HIVE);
                        output.accept(INFECTED_DIRT);
                        output.accept(INFECTED_STONE);
                        output.accept(INFECTED_LOG);
                        output.accept(INFECTED_LEAVES);
                        output.accept(INFECTED_GROWTH);
                        output.accept(INFECTED_SPAWN_EGG);
                        output.accept(RUNNER_SPAWN_EGG);
                        output.accept(BRUTE_SPAWN_EGG);
                        output.accept(SPITTER_SPAWN_EGG);
                        output.accept(BLOATER_SPAWN_EGG);
                    })
                    .build());

    private ModItems() {
    }

    private static Item block(Block block) {
        return Registry.register(BuiltInRegistries.ITEM, BuiltInRegistries.BLOCK.getKey(block),
                new BlockItem(block, new Item.Properties()));
    }

    private static Item egg(String name, EntityType<? extends Mob> type, int base, int spots) {
        return register(name, new SpawnEggItem(type, base, spots, new Item.Properties()));
    }

    private static Item register(String name, Item item) {
        return Registry.register(BuiltInRegistries.ITEM, InfeccionMod.id(name), item);
    }

    public static void register() {
        // Carga la clase para registrar los objetos y la pestaña creativa.
    }
}
