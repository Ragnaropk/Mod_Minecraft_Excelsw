package com.excelsw.infeccion.effect;

import com.excelsw.infeccion.Infection;
import com.excelsw.infeccion.ModTags;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

/**
 * La infección hace daño con el tiempo. En niveles altos provoca náuseas, hambre
 * y se contagia a las criaturas cercanas.
 */
public class InfectionEffect extends MobEffect {
    public InfectionEffect() {
        super(MobEffectCategory.HARMFUL, 0x6B1F7A);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        int interval = 60 >> Math.min(amplifier, 3);
        return interval <= 0 || duration % interval == 0;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (!(entity.level() instanceof ServerLevel level)) {
            return true;
        }
        if (entity.hasEffect(ModEffects.IMMUNITY)) {
            return false;
        }

        DamageSource source = new DamageSource(level.registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(ModTags.INFECTION_DAMAGE));
        entity.hurt(source, 1.0F);

        level.sendParticles(ParticleTypes.WITCH, entity.getX(), entity.getY() + entity.getBbHeight() * 0.6,
                entity.getZ(), 4, 0.3, 0.4, 0.3, 0.02);

        if (amplifier >= 1) {
            entity.addEffect(new MobEffectInstance(MobEffects.HUNGER, 100, 0, true, false));
            // ¡Contagio! Tosiendo esporas a los que estén cerca.
            if (entity.getRandom().nextInt(4) == 0) {
                for (LivingEntity other : level.getEntitiesOfClass(LivingEntity.class,
                        entity.getBoundingBox().inflate(3.0), e -> e != entity && e.isAlive())) {
                    Infection.tryInfect(other, 400, 0);
                }
            }
        }
        if (amplifier >= 2) {
            entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 120, 0, true, false));
            entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 0, true, false));
        }
        return true;
    }
}
