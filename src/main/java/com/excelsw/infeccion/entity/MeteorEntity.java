package com.excelsw.infeccion.entity;

import com.excelsw.infeccion.InfectionEvents;
import com.excelsw.infeccion.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

/** Meteorito infectado: cae del cielo envuelto en llamas y siembra un núcleo donde impacta. */
public class MeteorEntity extends ThrowableItemProjectile {
    private static final int MAX_LIFETIME = 400;

    public MeteorEntity(EntityType<? extends MeteorEntity> type, Level level) {
        super(type, level);
    }

    public MeteorEntity(Level level, double x, double y, double z) {
        super(ModEntities.METEOR, x, y, z, level);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.INFECTION_HIVE;
    }

    @Override
    protected double getDefaultGravity() {
        return 0.02;
    }

    @Override
    public boolean displayFireAnimation() {
        return true;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 256.0 * 256.0;
    }

    @Override
    public void tick() {
        super.tick();
        if (level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.FLAME, getX(), getY(), getZ(), 8, 0.5, 0.5, 0.5, 0.05);
            level.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY(), getZ(), 4, 0.4, 0.4, 0.4, 0.02);
            level.sendParticles(ParticleTypes.WITCH, getX(), getY(), getZ(), 4, 0.6, 0.6, 0.6, 0.1);
            if (tickCount % 3 == 0) {
                level.sendParticles(ParticleTypes.LAVA, getX(), getY(), getZ(), 2, 0.3, 0.3, 0.3, 0.0);
            }
            if (tickCount > MAX_LIFETIME) {
                impact(level, BlockPos.containing(position()));
            }
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (level() instanceof ServerLevel level) {
            impact(level, BlockPos.containing(result.getLocation()));
        }
    }

    private void impact(ServerLevel level, BlockPos pos) {
        if (isRemoved()) {
            return;
        }
        discard();
        InfectionEvents.meteorImpact(level, pos);
    }
}
