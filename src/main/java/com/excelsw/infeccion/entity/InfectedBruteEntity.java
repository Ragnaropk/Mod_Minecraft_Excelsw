package com.excelsw.infeccion.entity;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;

/** Bruto: una mole de carne infectada. Lento, pero te manda por los aires. */
public class InfectedBruteEntity extends InfectedEntity {
    public InfectedBruteEntity(EntityType<? extends Zombie> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createBruteAttributes() {
        return InfectedEntity.createInfectedAttributes()
                .add(Attributes.MAX_HEALTH, 70.0)
                .add(Attributes.MOVEMENT_SPEED, 0.21)
                .add(Attributes.ATTACK_DAMAGE, 10.0)
                .add(Attributes.ATTACK_KNOCKBACK, 1.5)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.ARMOR, 6.0)
                .add(Attributes.STEP_HEIGHT, 1.0)
                .add(Attributes.SCALE, 1.55);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit) {
            target.setDeltaMovement(target.getDeltaMovement().add(0.0, 0.55, 0.0));
            target.hurtMarked = true;
            if (level() instanceof ServerLevel level) {
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, getBlockStateOn()),
                        getX(), getY() + 0.1, getZ(), 40, 1.2, 0.1, 1.2, 0.2);
                playSound(SoundEvents.RAVAGER_ATTACK, 1.2F, 0.7F);
            }
        }
        return hit;
    }

    @Override
    public float getVoicePitch() {
        return 0.55F;
    }
}
