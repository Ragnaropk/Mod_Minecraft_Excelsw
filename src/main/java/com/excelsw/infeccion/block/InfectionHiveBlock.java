package com.excelsw.infeccion.block;

import com.excelsw.infeccion.Infection;
import com.excelsw.infeccion.ModGameRules;
import com.excelsw.infeccion.entity.InfectedEntity;
import com.excelsw.infeccion.entity.ModEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Núcleo infeccioso: el corazón de la plaga. Se propaga con fuerza y pare Infectados.
 * Destrúyelo para frenar el foco.
 */
public class InfectionHiveBlock extends Block {
    public static final MapCodec<InfectionHiveBlock> CODEC = simpleCodec(InfectionHiveBlock::new);
    private static final int MAX_NEARBY_INFECTED = 5;

    public InfectionHiveBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        Infection.spread(level, pos, random, 4, 4);

        if (level.getDifficulty() == Difficulty.PEACEFUL || random.nextInt(3) != 0) {
            return;
        }
        int nearby = level.getEntitiesOfClass(InfectedEntity.class, new AABB(pos).inflate(24.0)).size();
        if (nearby >= MAX_NEARBY_INFECTED) {
            return;
        }
        BlockPos spawnPos = pos.above();
        if (!level.getBlockState(spawnPos).isAir() || !level.getBlockState(spawnPos.above()).isAir()) {
            return;
        }
        InfectedEntity mob = ModEntities.INFECTED.create(level);
        if (mob == null) {
            return;
        }
        mob.moveTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, random.nextFloat() * 360.0F, 0.0F);
        mob.finalizeSpawn(level, level.getCurrentDifficultyAt(spawnPos), MobSpawnType.SPAWNER, null);
        level.addFreshEntity(mob);
        level.playSound(null, pos, SoundEvents.SCULK_SHRIEKER_SHRIEK, SoundSource.BLOCKS, 0.8F, 0.6F);
        level.sendParticles(ParticleTypes.SQUID_INK, spawnPos.getX() + 0.5, spawnPos.getY() + 0.5,
                spawnPos.getZ() + 0.5, 20, 0.4, 0.6, 0.4, 0.05);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        for (int i = 0; i < 2; i++) {
            level.addParticle(ParticleTypes.WITCH, pos.getX() + random.nextDouble(), pos.getY() + 1.1,
                    pos.getZ() + random.nextDouble(), 0.0, 0.05, 0.0);
        }
        if (random.nextInt(40) == 0) {
            level.playLocalSound(pos, SoundEvents.SCULK_CATALYST_BLOOM, SoundSource.BLOCKS, 0.6F, 0.5F, false);
        }
    }
}
