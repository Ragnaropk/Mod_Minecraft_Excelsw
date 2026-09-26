package com.excelsw.infeccion;

import com.excelsw.infeccion.block.ModBlocks;
import com.excelsw.infeccion.effect.ModEffects;
import com.excelsw.infeccion.entity.InfectedEntity;
import com.excelsw.infeccion.entity.ModEntities;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class InfectionEvents {
    private InfectionEvents() {
    }

    public static void register() {
        ServerLivingEntityEvents.AFTER_DEATH.register(InfectionEvents::onDeath);
        ServerTickEvents.END_WORLD_TICK.register(InfectionEvents::onWorldTick);
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(Commands.literal("infeccion")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("meteorito").executes(ctx -> {
                            ServerLevel level = ctx.getSource().getLevel();
                            BlockPos pos = BlockPos.containing(ctx.getSource().getPosition());
                            BlockPos impact = findImpactNear(level, pos, level.random, 12, 24);
                            if (impact == null) {
                                ctx.getSource().sendFailure(Component.translatable("message.infeccion.meteor_failed"));
                                return 0;
                            }
                            meteorImpact(level, impact);
                            return 1;
                        }))
                        .then(Commands.literal("purificar")
                                .then(Commands.argument("radio", IntegerArgumentType.integer(1, 64)).executes(ctx -> {
                                    int radius = IntegerArgumentType.getInteger(ctx, "radio");
                                    int cleaned = Infection.purifyArea(ctx.getSource().getLevel(),
                                            BlockPos.containing(ctx.getSource().getPosition()), radius);
                                    ctx.getSource().sendSuccess(() ->
                                            Component.translatable("message.infeccion.purified", cleaned), true);
                                    return cleaned;
                                })))));
    }

    /** Quien muere infectado, se levanta como Infectado. */
    private static void onDeath(LivingEntity entity, DamageSource source) {
        if (!(entity.level() instanceof ServerLevel level) || entity instanceof InfectedEntity
                || entity instanceof ArmorStand) {
            return;
        }
        boolean infected = entity.hasEffect(ModEffects.INFECTION)
                || source.is(ModTags.INFECTION_DAMAGE)
                || source.getEntity() instanceof InfectedEntity;
        if (!infected || entity.isInvertedHealAndHarm()) {
            return;
        }

        BlockPos pos = entity.blockPosition();
        Infection.infectArea(level, pos.below(), 2);
        level.sendParticles(ParticleTypes.SQUID_INK, entity.getX(), entity.getY() + 0.5, entity.getZ(),
                30, 0.4, 0.5, 0.4, 0.05);

        if (!level.getGameRules().getBoolean(ModGameRules.RESURRECTION)) {
            return;
        }
        InfectedEntity mob = ModEntities.INFECTED.create(level);
        if (mob == null) {
            return;
        }
        mob.moveTo(entity.getX(), entity.getY(), entity.getZ(), entity.getYRot(), 0.0F);
        mob.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.CONVERSION, null);
        mob.setBaby(entity.isBaby());
        if (entity instanceof Player player) {
            mob.setCustomName(Component.translatable("entity.infeccion.infected_player", player.getName())
                    .withStyle(ChatFormatting.DARK_PURPLE));
            mob.setPersistenceRequired();
        } else if (entity.hasCustomName()) {
            mob.setCustomName(entity.getCustomName());
            mob.setPersistenceRequired();
        }
        level.addFreshEntity(mob);
        level.playSound(null, pos, SoundEvents.ZOMBIE_VILLAGER_CONVERTED, SoundSource.HOSTILE, 1.0F, 0.6F);
    }

    /** Meteoritos infectados que caen cerca de los jugadores. El mundo es infinito: la plaga también. */
    private static void onWorldTick(ServerLevel level) {
        if (level.dimension() != Level.OVERWORLD || !level.getGameRules().getBoolean(ModGameRules.METEORS)) {
            return;
        }
        int interval = level.getGameRules().getInt(ModGameRules.METEOR_INTERVAL);
        if (level.getGameTime() % interval != 0) {
            return;
        }
        List<ServerPlayer> players = level.players().stream().filter(p -> !p.isSpectator()).toList();
        if (players.isEmpty()) {
            return;
        }
        ServerPlayer target = players.get(level.random.nextInt(players.size()));
        BlockPos impact = findImpactNear(level, target.blockPosition(), level.random, 40, 110);
        if (impact != null) {
            meteorImpact(level, impact);
        }
    }

    private static BlockPos findImpactNear(ServerLevel level, BlockPos center, RandomSource random,
                                           int minDistance, int maxDistance) {
        for (int attempt = 0; attempt < 10; attempt++) {
            double angle = random.nextDouble() * Math.PI * 2.0;
            double distance = minDistance + random.nextDouble() * (maxDistance - minDistance);
            int x = center.getX() + (int) (Math.cos(angle) * distance);
            int z = center.getZ() + (int) (Math.sin(angle) * distance);
            BlockPos column = new BlockPos(x, center.getY(), z);
            if (!level.isLoaded(column)) {
                continue;
            }
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            if (y <= level.getMinBuildHeight() + 1) {
                continue;
            }
            return new BlockPos(x, y, z);
        }
        return null;
    }

    public static void meteorImpact(ServerLevel level, BlockPos impact) {
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
        if (bolt != null) {
            bolt.moveTo(Vec3.atBottomCenterOf(impact));
            bolt.setVisualOnly(true);
            level.addFreshEntity(bolt);
        }
        level.explode(null, impact.getX() + 0.5, impact.getY(), impact.getZ() + 0.5, 4.0F, Level.ExplosionInteraction.TNT);

        BlockPos core = impact.below(2);
        if (level.getBlockState(core).isAir()) {
            core = new BlockPos(impact.getX(), level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    impact.getX(), impact.getZ()) - 1, impact.getZ());
        }
        Infection.infectArea(level, core, 5);
        level.setBlockAndUpdate(core, ModBlocks.INFECTION_HIVE.defaultBlockState());

        level.sendParticles(ParticleTypes.SQUID_INK, core.getX() + 0.5, core.getY() + 1.5, core.getZ() + 0.5,
                120, 2.5, 1.5, 2.5, 0.1);
        level.playSound(null, core, SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 3.0F, 0.5F);
        level.getServer().getPlayerList().broadcastSystemMessage(
                Component.translatable("message.infeccion.meteor", core.getX(), core.getY(), core.getZ())
                        .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD), false);
    }
}
