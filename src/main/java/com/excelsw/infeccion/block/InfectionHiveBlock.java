package com.excelsw.infeccion.block;

import com.excelsw.infeccion.Infection;
import com.excelsw.infeccion.InfectionData;
import com.excelsw.infeccion.entity.InfectedEntity;
import com.excelsw.infeccion.entity.ModEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Núcleo infeccioso: el corazón de la plaga. Se propaga con fuerza y pare Infectados.
 * No se puede purificar: hay que romperlo, y al hacerlo salen sus defensores.
 */
public class InfectionHiveBlock extends Block {
    public static final MapCodec<InfectionHiveBlock> CODEC = simpleCodec(InfectionHiveBlock::new);

    public InfectionHiveBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (level instanceof ServerLevel serverLevel && level.dimension() == Level.OVERWORLD && !oldState.is(this)) {
            InfectionData.get(serverLevel).addHive(pos);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (level instanceof ServerLevel serverLevel && !newState.is(this)) {
            InfectionData data = InfectionData.get(serverLevel);
            data.removeHive(pos);
            data.addPoints(-Infection.POINTS_PER_HIVE_DESTROYED);
            serverLevel.sendParticles(ParticleTypes.SCULK_SOUL, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    40, 0.6, 0.6, 0.6, 0.08);
            serverLevel.playSound(null, pos, SoundEvents.WARDEN_DEATH, SoundSource.BLOCKS, 1.5F, 1.3F);
            serverLevel.getServer().getPlayerList().broadcastSystemMessage(
                    Component.translatable("message.infeccion.hive_destroyed", pos.getX(), pos.getY(), pos.getZ())
                            .withStyle(ChatFormatting.GREEN), false);
            // Los defensores del núcleo salen a vengarlo.
            if (serverLevel.getDifficulty() != Difficulty.PEACEFUL) {
                for (int i = 0; i < 2; i++) {
                    spawnInfected(serverLevel, pos, ModEntities.RUNNER, serverLevel.random);
                }
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        Infection.spread(level, pos, random, 4, 4);

        if (level.getDifficulty() == Difficulty.PEACEFUL || random.nextInt(3) != 0) {
            return;
        }
        int phase = Infection.phase(level);
        int maxNearby = 3 + phase * 2;
        int nearby = level.getEntitiesOfClass(InfectedEntity.class, new AABB(pos).inflate(24.0)).size();
        if (nearby >= maxNearby) {
            return;
        }
        BlockPos spawnPos = pos.above();
        if (!level.getBlockState(spawnPos).isAir() || !level.getBlockState(spawnPos.above()).isAir()) {
            return;
        }
        if (spawnInfected(level, spawnPos, Infection.pickVariant(phase, random), random)) {
            level.playSound(null, pos, SoundEvents.SCULK_SHRIEKER_SHRIEK, SoundSource.BLOCKS, 0.8F, 0.6F);
        }
    }

    private static boolean spawnInfected(ServerLevel level, BlockPos pos, EntityType<? extends InfectedEntity> type,
                                         RandomSource random) {
        InfectedEntity mob = type.create(level);
        if (mob == null) {
            return false;
        }
        mob.moveTo(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, random.nextFloat() * 360.0F, 0.0F);
        mob.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.SPAWNER, null);
        level.addFreshEntity(mob);
        level.sendParticles(ParticleTypes.SQUID_INK, pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5,
                20, 0.4, 0.6, 0.4, 0.05);
        return true;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        for (int i = 0; i < 2; i++) {
            level.addParticle(ParticleTypes.WITCH, pos.getX() + random.nextDouble(), pos.getY() + 1.1,
                    pos.getZ() + random.nextDouble(), 0.0, 0.05, 0.0);
        }
        // Las esporas son absorbidas hacia el núcleo: late como un corazón.
        for (int i = 0; i < 3; i++) {
            level.addParticle(ParticleTypes.REVERSE_PORTAL, pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 3.0,
                    pos.getY() + 0.5 + random.nextDouble() * 2.0, pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 3.0,
                    0.0, 0.0, 0.0);
        }
        if (random.nextInt(8) == 0) {
            level.addParticle(ParticleTypes.SCULK_SOUL, pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5,
                    0.0, 0.04, 0.0);
        }
        if (random.nextInt(40) == 0) {
            level.playLocalSound(pos, SoundEvents.WARDEN_HEARTBEAT, SoundSource.BLOCKS, 1.0F, 0.7F, false);
        }
    }
}
