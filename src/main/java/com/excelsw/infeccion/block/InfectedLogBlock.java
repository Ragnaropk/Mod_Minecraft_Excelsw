package com.excelsw.infeccion.block;

import com.excelsw.infeccion.Infection;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Madera infectada: los árboles se pudren desde el tronco hasta las hojas. */
public class InfectedLogBlock extends RotatedPillarBlock {
    public static final MapCodec<InfectedLogBlock> CODEC = simpleCodec(InfectedLogBlock::new);

    public InfectedLogBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<? extends RotatedPillarBlock> codec() {
        return CODEC;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // Los troncos alcanzan más lejos para llegar a las hojas.
        Infection.spread(level, pos, random, 2, 1);
    }
}
