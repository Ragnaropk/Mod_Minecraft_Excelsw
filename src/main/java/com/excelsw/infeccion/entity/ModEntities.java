package com.excelsw.infeccion.entity;

import com.excelsw.infeccion.InfeccionMod;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class ModEntities {
    public static final EntityType<InfectedEntity> INFECTED = Registry.register(
            BuiltInRegistries.ENTITY_TYPE, InfeccionMod.id("infected"),
            EntityType.Builder.<InfectedEntity>of(InfectedEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F)
                    .eyeHeight(1.74F)
                    .clientTrackingRange(8)
                    .build("infected"));

    private ModEntities() {
    }

    public static void register() {
        FabricDefaultAttributeRegistry.register(INFECTED, InfectedEntity.createInfectedAttributes());
    }
}
