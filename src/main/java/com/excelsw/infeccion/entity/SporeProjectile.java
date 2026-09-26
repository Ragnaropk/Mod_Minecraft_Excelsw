package com.excelsw.infeccion.entity;

import com.excelsw.infeccion.Infection;
import com.excelsw.infeccion.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/** Bola de esporas: la escupen los Escupidores y la puedes lanzar tú con una Espora infecciosa. */
public class SporeProjectile extends ThrowableItemProjectile {
    public SporeProjectile(EntityType<? extends SporeProjectile> type, Level level) {
        super(type, level);
    }

    public SporeProjectile(Level level, LivingEntity owner) {
        super(ModEntities.SPORE, owner, level);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.INFECTION_SPORE;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) {
            level().addParticle(ParticleTypes.WITCH, getX(), getY(), getZ(), 0.0, 0.0, 0.0);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (result.getEntity() instanceof LivingEntity living) {
            living.hurt(damageSources().thrown(this, getOwner()), 2.0F);
            Infection.tryInfect(living, 500, 0);
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (level() instanceof ServerLevel level) {
            BlockPos pos = result.getBlockPos();
            Infection.infectBlock(level, pos, false);
            if (random.nextInt(3) == 0) {
                Infection.infectBlock(level, pos.relative(result.getDirection().getOpposite()), false);
            }
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (level() instanceof ServerLevel level) {
            level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(ModItems.INFECTION_SPORE)),
                    getX(), getY(), getZ(), 12, 0.2, 0.2, 0.2, 0.1);
            level.sendParticles(ParticleTypes.SQUID_INK, getX(), getY(), getZ(), 6, 0.2, 0.2, 0.2, 0.02);
            playSound(SoundEvents.SLIME_SQUISH, 1.0F, 0.6F);
            discard();
        }
    }
}
