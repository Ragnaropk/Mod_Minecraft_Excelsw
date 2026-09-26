package com.excelsw.infeccion.block;

import com.excelsw.infeccion.Infection;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** Purificador: limpia sin parar la infección en un radio de 8 bloques. */
public class PurifierBlock extends Block {
    public static final MapCodec<PurifierBlock> CODEC = simpleCodec(PurifierBlock::new);
    public static final int RADIUS = 8;
    private static final int TICK_DELAY = 10;
    private static final int CHECKS_PER_TICK = 48;

    public PurifierBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        level.scheduleTick(pos, this, TICK_DELAY);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        for (int i = 0; i < CHECKS_PER_TICK; i++) {
            BlockPos target = pos.offset(
                    random.nextInt(RADIUS * 2 + 1) - RADIUS,
                    random.nextInt(RADIUS * 2 + 1) - RADIUS,
                    random.nextInt(RADIUS * 2 + 1) - RADIUS);
            if (target.distSqr(pos) <= RADIUS * RADIUS) {
                Infection.purifyBlock(level, target);
            }
        }
        level.scheduleTick(pos, this, TICK_DELAY);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        level.addParticle(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5,
                (random.nextDouble() - 0.5) * 0.1, 0.08, (random.nextDouble() - 0.5) * 0.1);
    }
}
