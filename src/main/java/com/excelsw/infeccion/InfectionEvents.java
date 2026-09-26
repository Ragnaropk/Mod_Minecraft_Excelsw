package com.excelsw.infeccion;

import com.excelsw.infeccion.block.ModBlocks;
import com.excelsw.infeccion.effect.ModEffects;
import com.excelsw.infeccion.entity.InfectedEntity;
import com.excelsw.infeccion.entity.MeteorEntity;
import com.excelsw.infeccion.entity.ModEntities;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.BossEvent;
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
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class InfectionEvents {
    /** Cada fase acelera los meteoritos: 100 %, 80 %, 60 %, 40 % del intervalo. */
    private static final double[] METEOR_INTERVAL_FACTOR = {1.0, 0.8, 0.6, 0.4};
    private static final BossEvent.BossBarColor[] PHASE_COLORS = {
            BossEvent.BossBarColor.GREEN, BossEvent.BossBarColor.YELLOW,
            BossEvent.BossBarColor.PURPLE, BossEvent.BossBarColor.RED};

    @Nullable
    private static ServerBossEvent bossBar;
    private static int lastPhase = -1;

    private InfectionEvents() {
    }

    public static void register() {
        ServerLivingEntityEvents.AFTER_DEATH.register(InfectionEvents::onDeath);
        ServerTickEvents.END_WORLD_TICK.register(InfectionEvents::onWorldTick);
        ServerTickEvents.END_SERVER_TICK.register(InfectionEvents::onServerTick);
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            bossBar = null;
            lastPhase = -1;
        });
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(Commands.literal("infeccion")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("estado").executes(ctx -> {
                            InfectionData data = InfectionData.get(ctx.getSource().getServer());
                            ctx.getSource().sendSuccess(() -> Component.translatable("message.infeccion.status",
                                    data.phase() + 1, Component.translatable("phase.infeccion." + data.phase()),
                                    data.points(), data.hiveCount()), false);
                            return data.phase();
                        }))
                        .then(Commands.literal("puntos")
                                .then(Commands.argument("cantidad", LongArgumentType.longArg(0)).executes(ctx -> {
                                    long amount = LongArgumentType.getLong(ctx, "cantidad");
                                    InfectionData.get(ctx.getSource().getServer()).setPoints(amount);
                                    ctx.getSource().sendSuccess(() ->
                                            Component.translatable("message.infeccion.points_set", amount), true);
                                    return 1;
                                })))
                        .then(Commands.literal("meteorito").executes(ctx -> launchMeteorCommand(ctx.getSource())))
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

    private static int launchMeteorCommand(CommandSourceStack source) {
        ServerLevel level = source.getLevel();
        BlockPos target = findImpactNear(level, BlockPos.containing(source.getPosition()), level.random, 16, 32);
        if (target == null) {
            source.sendFailure(Component.translatable("message.infeccion.meteor_failed"));
            return 0;
        }
        launchMeteor(level, target);
        return 1;
    }

    /** Quien muere infectado, se levanta como Infectado. Los grandes vuelven como Brutos. */
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
        int phase = Infection.phase(level);
        EntityType<? extends InfectedEntity> type;
        if (entity.getBbWidth() >= 1.2F || entity.getMaxHealth() >= 40.0F) {
            type = ModEntities.BRUTE;
        } else if (entity instanceof Player) {
            type = ModEntities.INFECTED;
        } else {
            type = Infection.pickVariant(phase, level.random);
        }
        InfectedEntity mob = type.create(level);
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
        int phase = Infection.phase(level);
        long interval = Math.max(200L, (long) (level.getGameRules().getInt(ModGameRules.METEOR_INTERVAL)
                * METEOR_INTERVAL_FACTOR[phase]));
        if (level.getGameTime() % interval != 0) {
            return;
        }
        List<ServerPlayer> players = level.players().stream().filter(p -> !p.isSpectator()).toList();
        if (players.isEmpty()) {
            return;
        }
        ServerPlayer target = players.get(level.random.nextInt(players.size()));
        BlockPos impact = findImpactNear(level, target.blockPosition(), level.random, 32, 96);
        if (impact != null) {
            launchMeteor(level, impact);
        }
    }

    /** Barra de amenaza global y avisos de cambio de fase. */
    private static void onServerTick(MinecraftServer server) {
        if (server.getTickCount() % 20 != 0) {
            return;
        }
        InfectionData data = InfectionData.get(server);
        int phase = data.phase();

        if (lastPhase >= 0 && phase != lastPhase) {
            boolean worse = phase > lastPhase;
            server.getPlayerList().broadcastSystemMessage(Component.translatable(
                    worse ? "message.infeccion.phase_up" : "message.infeccion.phase_down",
                    phase + 1, Component.translatable("phase.infeccion." + phase))
                    .withStyle(worse ? ChatFormatting.DARK_RED : ChatFormatting.GREEN, ChatFormatting.BOLD), false);
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                player.playNotifySound(worse ? SoundEvents.WITHER_SPAWN : SoundEvents.BEACON_POWER_SELECT,
                        SoundSource.MASTER, 0.8F, worse ? 0.6F : 1.2F);
            }
        }
        lastPhase = phase;

        boolean show = server.overworld().getGameRules().getBoolean(ModGameRules.BOSS_BAR);
        if (bossBar == null) {
            bossBar = new ServerBossEvent(Component.empty(), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.NOTCHED_10);
        }
        bossBar.setName(Component.translatable("bossbar.infeccion.threat", phase + 1,
                Component.translatable("phase.infeccion." + phase)));
        bossBar.setColor(PHASE_COLORS[phase]);
        bossBar.setProgress(data.phaseProgress());
        bossBar.setDarkenScreen(phase >= 3);
        bossBar.setCreateWorldFog(phase >= 3);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            boolean shouldSee = show && player.level().dimension() == Level.OVERWORLD;
            boolean sees = bossBar.getPlayers().contains(player);
            if (shouldSee && !sees) {
                bossBar.addPlayer(player);
            } else if (!shouldSee && sees) {
                bossBar.removePlayer(player);
            }
        }
    }

    @Nullable
    private static BlockPos findImpactNear(ServerLevel level, BlockPos center, RandomSource random,
                                           int minDistance, int maxDistance) {
        for (int attempt = 0; attempt < 10; attempt++) {
            double angle = random.nextDouble() * Math.PI * 2.0;
            double distance = minDistance + random.nextDouble() * (maxDistance - minDistance);
            int x = center.getX() + (int) (Math.cos(angle) * distance);
            int z = center.getZ() + (int) (Math.sin(angle) * distance);
            BlockPos column = new BlockPos(x, center.getY(), z);
            // Solo en zonas donde las entidades se simulan, para que el meteorito no se congele.
            if (!level.isPositionEntityTicking(column)) {
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

    /** Lanza un meteorito desde el cielo hacia el objetivo. */
    public static void launchMeteor(ServerLevel level, BlockPos target) {
        double angle = level.random.nextDouble() * Math.PI * 2.0;
        double startX = target.getX() + 0.5 + Math.cos(angle) * 45.0;
        double startZ = target.getZ() + 0.5 + Math.sin(angle) * 45.0;
        double startY = Math.min(level.getMaxBuildHeight() - 2, target.getY() + 90);
        if (!level.isPositionEntityTicking(BlockPos.containing(startX, startY, startZ))) {
            startX = target.getX() + 0.5;
            startZ = target.getZ() + 0.5;
        }
        MeteorEntity meteor = new MeteorEntity(level, startX, startY, startZ);
        meteor.shoot(target.getX() + 0.5 - startX, target.getY() - startY, target.getZ() + 0.5 - startZ, 2.2F, 0.0F);
        level.addFreshEntity(meteor);
        level.playSound(null, BlockPos.containing(startX, target.getY() + 20, startZ), SoundEvents.GHAST_SHOOT,
                SoundSource.HOSTILE, 6.0F, 0.4F);
        level.getServer().getPlayerList().broadcastSystemMessage(
                Component.translatable("message.infeccion.meteor_incoming").withStyle(ChatFormatting.GOLD), false);
    }

    /** Impacto: cráter, rayo, zona infectada y un núcleo en el centro. */
    public static void meteorImpact(ServerLevel level, BlockPos hit) {
        int surface = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, hit.getX(), hit.getZ());
        BlockPos impact = new BlockPos(hit.getX(), Math.min(hit.getY(), surface), hit.getZ());

        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
        if (bolt != null) {
            bolt.moveTo(Vec3.atBottomCenterOf(impact));
            bolt.setVisualOnly(true);
            level.addFreshEntity(bolt);
        }
        level.explode(null, impact.getX() + 0.5, impact.getY(), impact.getZ() + 0.5, 4.0F, Level.ExplosionInteraction.TNT);

        BlockPos core = new BlockPos(impact.getX(),
                level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, impact.getX(), impact.getZ()) - 1,
                impact.getZ());
        Infection.infectArea(level, core, 5);
        level.setBlockAndUpdate(core, ModBlocks.INFECTION_HIVE.defaultBlockState());

        level.sendParticles(ParticleTypes.SQUID_INK, core.getX() + 0.5, core.getY() + 1.5, core.getZ() + 0.5,
                120, 2.5, 1.5, 2.5, 0.1);
        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, core.getX() + 0.5, core.getY() + 1.0, core.getZ() + 0.5,
                2, 1.0, 0.5, 1.0, 0.0);
        level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, core.getX() + 0.5, core.getY() + 1.0, core.getZ() + 0.5,
                40, 2.0, 0.5, 2.0, 0.03);
        level.playSound(null, core, SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 3.0F, 0.5F);
        level.getServer().getPlayerList().broadcastSystemMessage(
                Component.translatable("message.infeccion.meteor", core.getX(), core.getY(), core.getZ())
                        .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD), false);
    }
}
