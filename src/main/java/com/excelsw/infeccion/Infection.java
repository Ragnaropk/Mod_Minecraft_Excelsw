package com.excelsw.infeccion;

import com.excelsw.infeccion.block.ModBlocks;
import com.excelsw.infeccion.effect.ModEffects;
import com.excelsw.infeccion.entity.InfectedEntity;
import com.excelsw.infeccion.entity.ModEntities;
import com.excelsw.infeccion.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** Toda la lógica de la infección: qué se infecta, cómo se contagia y cómo se cura. */
public final class Infection {
    /** Puntos de amenaza que suma cada bloque infectado y que resta cada bloque purificado. */
    public static final int POINTS_PER_BLOCK = 1;
    public static final int POINTS_PER_PURIFIED_BLOCK = 2;
    public static final int POINTS_PER_HIVE_DESTROYED = 400;

    private Infection() {
    }

    public static int phase(ServerLevel level) {
        return InfectionData.get(level).phase();
    }

    public static boolean isInfected(BlockState state) {
        return state.is(ModBlocks.INFECTED_DIRT) || state.is(ModBlocks.INFECTED_STONE)
                || state.is(ModBlocks.INFECTED_LOG) || state.is(ModBlocks.INFECTED_LEAVES)
                || state.is(ModBlocks.INFECTION_HIVE) || state.is(ModBlocks.INFECTED_GROWTH);
    }

    /** Devuelve la versión infectada de un bloque, o null si no se puede infectar. */
    @Nullable
    public static BlockState infectedVersion(BlockState state) {
        if (state.isAir() || isInfected(state) || state.is(ModTags.INFECTION_IMMUNE)) {
            return null;
        }
        Block block = state.getBlock();
        if (state.is(BlockTags.LOGS)) {
            BlockState log = ModBlocks.INFECTED_LOG.defaultBlockState();
            if (state.hasProperty(RotatedPillarBlock.AXIS)) {
                log = log.setValue(RotatedPillarBlock.AXIS, state.getValue(RotatedPillarBlock.AXIS));
            }
            return log;
        }
        if (state.is(BlockTags.LEAVES) || state.is(BlockTags.WART_BLOCKS)) {
            return ModBlocks.INFECTED_LEAVES.defaultBlockState();
        }
        if (state.is(BlockTags.DIRT) || state.is(BlockTags.SAND) || state.is(BlockTags.NYLIUM)
                || block == Blocks.GRAVEL || block == Blocks.FARMLAND || block == Blocks.DIRT_PATH
                || block == Blocks.CLAY || block == Blocks.SNOW_BLOCK || block == Blocks.SOUL_SAND
                || block == Blocks.SOUL_SOIL) {
            return ModBlocks.INFECTED_DIRT.defaultBlockState();
        }
        if (state.is(BlockTags.BASE_STONE_OVERWORLD) || state.is(BlockTags.BASE_STONE_NETHER)
                || block == Blocks.COBBLESTONE || block == Blocks.MOSSY_COBBLESTONE
                || block == Blocks.SANDSTONE || block == Blocks.RED_SANDSTONE
                || block == Blocks.END_STONE || block == Blocks.COBBLED_DEEPSLATE) {
            return ModBlocks.INFECTED_STONE.defaultBlockState();
        }
        if (state.is(BlockTags.FLOWERS) || state.is(BlockTags.SAPLINGS) || block == Blocks.SHORT_GRASS
                || block == Blocks.TALL_GRASS || block == Blocks.FERN || block == Blocks.LARGE_FERN) {
            // Las plantas se marchitan.
            return Blocks.AIR.defaultBlockState();
        }
        return null;
    }

    /**
     * Devuelve la versión purificada de un bloque infectado, o null si no se puede purificar.
     * Los núcleos no se purifican: hay que romperlos a mano.
     */
    @Nullable
    public static BlockState purifiedVersion(BlockState state) {
        if (state.is(ModBlocks.INFECTED_DIRT)) {
            return Blocks.DIRT.defaultBlockState();
        }
        if (state.is(ModBlocks.INFECTED_STONE)) {
            return Blocks.STONE.defaultBlockState();
        }
        if (state.is(ModBlocks.INFECTED_LOG)) {
            return Blocks.OAK_LOG.defaultBlockState()
                    .setValue(RotatedPillarBlock.AXIS, state.getValue(RotatedPillarBlock.AXIS));
        }
        if (state.is(ModBlocks.INFECTED_LEAVES) || state.is(ModBlocks.INFECTED_GROWTH)) {
            return Blocks.AIR.defaultBlockState();
        }
        return null;
    }

    /** Solo se infectan bloques que tocan aire: así la plaga es visible y no destroza el rendimiento. */
    private static boolean isExposed(ServerLevel level, BlockPos pos) {
        for (Direction dir : Direction.values()) {
            if (level.getBlockState(pos.relative(dir)).isAir()) {
                return true;
            }
        }
        return false;
    }

    /** Intenta infectar un bloque concreto. */
    public static boolean infectBlock(ServerLevel level, BlockPos pos, boolean requireExposed) {
        if (!level.isLoaded(pos)) {
            return false;
        }
        BlockState state = level.getBlockState(pos);
        BlockState infected = infectedVersion(state);
        if (infected == null || (requireExposed && !isExposed(level, pos))) {
            return false;
        }
        level.setBlock(pos, infected, Block.UPDATE_ALL);
        InfectionData.get(level).addPoints(POINTS_PER_BLOCK);
        return true;
    }

    /** Propagación desde un bloque infectado hacia sus vecinos. Cada fase añade intentos extra. */
    public static void spread(ServerLevel level, BlockPos origin, RandomSource random, int radius, int bonusAttempts) {
        if (!level.getGameRules().getBoolean(ModGameRules.SPREAD)) {
            return;
        }
        int attempts = level.getGameRules().getInt(ModGameRules.SPREAD_SPEED) + bonusAttempts + phase(level);
        for (int i = 0; i < attempts; i++) {
            BlockPos target = origin.offset(
                    random.nextInt(radius * 2 + 1) - radius,
                    random.nextInt(radius * 2 + 1) - radius,
                    random.nextInt(radius * 2 + 1) - radius);
            if (target.getY() < level.getMinBuildHeight() || target.getY() >= level.getMaxBuildHeight()) {
                continue;
            }
            infectBlock(level, target, true);
        }
    }

    /** Infecta una esfera de bloques de golpe (impactos de meteorito, muertes, esporas). */
    public static void infectArea(ServerLevel level, BlockPos center, int radius) {
        int r2 = radius * radius;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, -radius, -radius),
                center.offset(radius, radius, radius))) {
            if (pos.distSqr(center) <= r2 && level.random.nextFloat() < 0.85F) {
                infectBlock(level, pos.immutable(), false);
            }
        }
    }

    /** Purifica los bloques infectados en una esfera. Devuelve cuántos se purificaron. */
    public static int purifyArea(ServerLevel level, BlockPos center, int radius) {
        int r2 = radius * radius;
        int count = 0;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, -radius, -radius),
                center.offset(radius, radius, radius))) {
            if (pos.distSqr(center) <= r2 && purifyBlock(level, pos.immutable())) {
                count++;
            }
        }
        return count;
    }

    public static boolean purifyBlock(ServerLevel level, BlockPos pos) {
        BlockState purified = purifiedVersion(level.getBlockState(pos));
        if (purified == null) {
            return false;
        }
        level.setBlock(pos, purified, Block.UPDATE_ALL);
        level.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                2, 0.3, 0.2, 0.3, 0.01);
        InfectionData.get(level).addPoints(-POINTS_PER_PURIFIED_BLOCK);
        return true;
    }

    public static boolean isImmune(LivingEntity entity) {
        if (entity instanceof InfectedEntity || entity.hasEffect(ModEffects.IMMUNITY)) {
            return true;
        }
        if (entity instanceof Player player && (player.isCreative() || player.isSpectator())) {
            return true;
        }
        return entity.isInvertedHealAndHarm(); // Los no-muertos ya están podridos por dentro.
    }

    /** La máscara de gas filtra las esporas del aire, pero se va gastando. */
    private static boolean maskBlocks(LivingEntity entity) {
        ItemStack head = entity.getItemBySlot(EquipmentSlot.HEAD);
        if (!head.is(ModItems.GAS_MASK)) {
            return false;
        }
        head.hurtAndBreak(1, entity, EquipmentSlot.HEAD);
        return true;
    }

    /** Contagio directo (mordisco, espora lanzada): puede empeorar la infección. */
    public static boolean tryInfect(LivingEntity entity, int duration, int amplifier) {
        return tryInfect(entity, duration, amplifier, true, false);
    }

    /**
     * Infecta a una criatura.
     *
     * @param canWorsen si ya estaba infectada, puede subir de nivel (hasta IV)
     * @param airborne  contagio por esporas en el aire: la máscara de gas lo bloquea
     */
    public static boolean tryInfect(LivingEntity entity, int duration, int amplifier, boolean canWorsen, boolean airborne) {
        if (entity.level().isClientSide() || isImmune(entity)) {
            return false;
        }
        MobEffectInstance current = entity.getEffect(ModEffects.INFECTION);
        if (current != null && !canWorsen) {
            return false;
        }
        if (airborne && maskBlocks(entity)) {
            return false;
        }
        int newAmplifier = amplifier;
        int newDuration = duration;
        if (current != null) {
            newAmplifier = Math.min(3, Math.max(current.getAmplifier(), amplifier)
                    + (entity.getRandom().nextInt(3) == 0 ? 1 : 0));
            newDuration = Math.max(current.getDuration(), duration);
        }
        boolean added = entity.addEffect(new MobEffectInstance(ModEffects.INFECTION, newDuration, newAmplifier));
        if (added && current == null) {
            entity.level().playSound(null, entity.blockPosition(), SoundEvents.ZOMBIE_INFECT,
                    SoundSource.HOSTILE, 0.6F, 1.4F);
        }
        return added;
    }

    /** Elige qué tipo de Infectado nace según la fase de la plaga. */
    public static EntityType<? extends InfectedEntity> pickVariant(int phase, RandomSource random) {
        int roll = random.nextInt(100);
        return switch (phase) {
            case 0 -> ModEntities.INFECTED;
            case 1 -> roll < 60 ? ModEntities.INFECTED : roll < 85 ? ModEntities.RUNNER : ModEntities.SPITTER;
            case 2 -> roll < 40 ? ModEntities.INFECTED : roll < 65 ? ModEntities.RUNNER
                    : roll < 80 ? ModEntities.SPITTER : roll < 95 ? ModEntities.BLOATER : ModEntities.BRUTE;
            default -> roll < 30 ? ModEntities.INFECTED : roll < 55 ? ModEntities.RUNNER
                    : roll < 70 ? ModEntities.SPITTER : roll < 85 ? ModEntities.BLOATER : ModEntities.BRUTE;
        };
    }
}
