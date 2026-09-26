package com.excelsw.infeccion.entity;

import com.excelsw.infeccion.Infection;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;

/** Hinchado: se acerca, se infla… y revienta en una nube de esporas. También al morir. */
public class InfectedBloaterEntity extends InfectedEntity {
    public static final int MAX_FUSE = 30;
    private static final EntityDataAccessor<Integer> DATA_FUSE =
            SynchedEntityData.defineId(InfectedBloaterEntity.class, EntityDataSerializers.INT);

    private int oldFuse;
    private boolean burst;

    public InfectedBloaterEntity(EntityType<? extends Zombie> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createBloaterAttributes() {
        return InfectedEntity.createInfectedAttributes()
                .add(Attributes.MAX_HEALTH, 24.0)
                .add(Attributes.MOVEMENT_SPEED, 0.22)
                .add(Attributes.ATTACK_DAMAGE, 2.0)
                .add(Attributes.SCALE, 1.25);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_FUSE, 0);
    }

    public int getFuse() {
        return this.entityData.get(DATA_FUSE);
    }

    /** 0..1: lo hinchado que está (para el renderizado). */
    public float getSwelling(float partialTick) {
        return Mth.lerp(partialTick, (float) oldFuse, (float) getFuse()) / (float) (MAX_FUSE - 2);
    }

    @Override
    public void tick() {
        oldFuse = getFuse();
        super.tick();
        if (level().isClientSide() || !isAlive()) {
            return;
        }
        LivingEntity target = getTarget();
        int fuse = getFuse();
        if (target != null && target.isAlive() && distanceToSqr(target) < 9.0) {
            if (fuse == 0) {
                playSound(SoundEvents.PUFFER_FISH_BLOW_UP, 1.5F, 0.5F);
            }
            fuse++;
        } else {
            fuse = Math.max(0, fuse - 1);
        }
        this.entityData.set(DATA_FUSE, fuse);
        if (fuse >= MAX_FUSE) {
            burst();
        }
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        burst();
    }

    private void burst() {
        if (burst || !(level() instanceof ServerLevel level)) {
            return;
        }
        burst = true;
        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, getX(), getY() + 1.0, getZ(), 1, 0, 0, 0, 0);
        level.sendParticles(ParticleTypes.SQUID_INK, getX(), getY() + 1.0, getZ(), 80, 1.5, 1.0, 1.5, 0.15);
        playSound(SoundEvents.SLIME_DEATH, 2.0F, 0.4F);
        playSound(SoundEvents.GENERIC_EXPLODE.value(), 1.0F, 1.4F);

        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(4.0),
                e -> e != this && e.isAlive())) {
            victim.hurt(damageSources().mobAttack(this), 4.0F);
            Infection.tryInfect(victim, 600, 1, true, true);
        }
        Infection.infectArea(level, blockPosition().below(), 2);

        // Nube de esporas que se queda flotando unos segundos.
        AreaEffectCloud cloud = new AreaEffectCloud(level, getX(), getY(), getZ());
        cloud.setRadius(3.5F);
        cloud.setRadiusPerTick(-0.02F);
        cloud.setDuration(120);
        cloud.setWaitTime(0);
        cloud.setParticle(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, 0xFF7A2A8C));
        level.addFreshEntity(cloud);

        if (isAlive()) {
            discard();
        }
    }
}
