package com.excelsw.infeccion.entity;

import com.excelsw.infeccion.InfeccionMod;
import com.excelsw.infeccion.InfectionData;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;

public final class ModEntities {
    public static final EntityType<InfectedEntity> INFECTED = register("infected",
            EntityType.Builder.<InfectedEntity>of(InfectedEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F).eyeHeight(1.74F).clientTrackingRange(8));
    public static final EntityType<InfectedRunnerEntity> RUNNER = register("infected_runner",
            EntityType.Builder.<InfectedRunnerEntity>of(InfectedRunnerEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F).eyeHeight(1.74F).clientTrackingRange(8));
    public static final EntityType<InfectedBruteEntity> BRUTE = register("infected_brute",
            EntityType.Builder.<InfectedBruteEntity>of(InfectedBruteEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F).eyeHeight(1.74F).clientTrackingRange(10));
    public static final EntityType<InfectedSpitterEntity> SPITTER = register("infected_spitter",
            EntityType.Builder.<InfectedSpitterEntity>of(InfectedSpitterEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F).eyeHeight(1.74F).clientTrackingRange(8));
    public static final EntityType<InfectedBloaterEntity> BLOATER = register("infected_bloater",
            EntityType.Builder.<InfectedBloaterEntity>of(InfectedBloaterEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F).eyeHeight(1.74F).clientTrackingRange(8));

    public static final EntityType<SporeProjectile> SPORE = register("spore",
            EntityType.Builder.<SporeProjectile>of(SporeProjectile::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(10));
    public static final EntityType<MeteorEntity> METEOR = register("meteor",
            EntityType.Builder.<MeteorEntity>of(MeteorEntity::new, MobCategory.MISC)
                    .sized(1.0F, 1.0F).clientTrackingRange(16).updateInterval(2).fireImmune());

    private ModEntities() {
    }

    private static <T extends net.minecraft.world.entity.Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, InfeccionMod.id(name), builder.build(name));
    }

    public static void register() {
        FabricDefaultAttributeRegistry.register(INFECTED, InfectedEntity.createInfectedAttributes());
        FabricDefaultAttributeRegistry.register(RUNNER, InfectedRunnerEntity.createRunnerAttributes());
        FabricDefaultAttributeRegistry.register(BRUTE, InfectedBruteEntity.createBruteAttributes());
        FabricDefaultAttributeRegistry.register(SPITTER, InfectedSpitterEntity.createSpitterAttributes());
        FabricDefaultAttributeRegistry.register(BLOATER, InfectedBloaterEntity.createBloaterAttributes());

        // Aparición natural de noche: cada variante solo desde cierta fase de la plaga.
        naturalSpawn(INFECTED, 1, 40, 1, 3);
        naturalSpawn(RUNNER, 2, 20, 1, 3);
        naturalSpawn(SPITTER, 2, 12, 1, 2);
        naturalSpawn(BLOATER, 2, 12, 1, 1);
        naturalSpawn(BRUTE, 3, 6, 1, 1);
    }

    private static <T extends InfectedEntity> void naturalSpawn(EntityType<T> type, int minPhase, int weight,
                                                                int minGroup, int maxGroup) {
        SpawnPlacements.register(type, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (entityType, level, reason, pos, random) -> canSpawnNaturally(entityType, level, reason, pos, random, minPhase));
        BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld(), MobCategory.MONSTER, type, weight, minGroup, maxGroup);
    }

    private static boolean canSpawnNaturally(EntityType<? extends Monster> type, ServerLevelAccessor level,
                                             MobSpawnType reason, BlockPos pos, RandomSource random, int minPhase) {
        return InfectionData.get(level.getLevel()).phase() >= minPhase
                && Monster.checkMonsterSpawnRules(type, level, reason, pos, random);
    }
}
