package com.excelsw.infeccion.block;

import com.excelsw.infeccion.Infection;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;

/** Tierra, piedra y hojas infectadas: se propagan y contagian a quien las pisa. */
public class InfectedBlock extends Block {
    public static final MapCodec<InfectedBlock> CODEC = simpleCodec(InfectedBlock::new);
    public static final DustParticleOptions SPORE_DUST = new DustParticleOptions(new Vector3f(0.55F, 0.18F, 0.65F), 1.1F);

    public InfectedBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        Infection.spread(level, pos, random, 1, 0);
        // En la tierra infectada brotan zarcillos brillantes.
        if (state.is(ModBlocks.INFECTED_DIRT) && random.nextInt(10) == 0 && level.getBlockState(pos.above()).isAir()) {
            level.setBlockAndUpdate(pos.above(), ModBlocks.INFECTED_GROWTH.defaultBlockState());
        }
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (!level.isClientSide() && entity instanceof LivingEntity living && level.random.nextInt(40) == 0) {
            Infection.tryInfect(living, 300, 0, false, true);
        }
        super.stepOn(level, pos, state, entity);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!level.getBlockState(pos.above()).isAir()) {
            return;
        }
        if (random.nextInt(5) == 0) {
            level.addParticle(ParticleTypes.MYCELIUM, pos.getX() + random.nextDouble(), pos.getY() + 1.05,
                    pos.getZ() + random.nextDouble(), 0.0, 0.0, 0.0);
        }
        if (random.nextInt(25) == 0) {
            // Esporas flotando en el aire.
            level.addParticle(SPORE_DUST, pos.getX() + random.nextDouble(), pos.getY() + 1.2 + random.nextDouble(),
                    pos.getZ() + random.nextDouble(), 0.0, 0.02, 0.0);
        }
        if (random.nextInt(60) == 0) {
            level.addParticle(ParticleTypes.SPORE_BLOSSOM_AIR, pos.getX() + random.nextDouble(), pos.getY() + 1.5,
                    pos.getZ() + random.nextDouble(), 0.0, 0.0, 0.0);
        }
    }
}
