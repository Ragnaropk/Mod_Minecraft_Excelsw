package com.excelsw.infeccion.entity;

import com.excelsw.infeccion.Infection;
import com.excelsw.infeccion.effect.ModEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;

/**
 * El Infectado: lo que queda de cualquier criatura que muere con la infección.
 * No se quema al sol, persigue también a los animales y contagia con cada golpe.
 */
public class InfectedEntity extends Zombie {
    public InfectedEntity(EntityType<? extends Zombie> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createInfectedAttributes() {
        return Zombie.createAttributes()
                .add(Attributes.MAX_HEALTH, 26.0)
                .add(Attributes.MOVEMENT_SPEED, 0.27)
                .add(Attributes.ATTACK_DAMAGE, 4.0)
                .add(Attributes.FOLLOW_RANGE, 40.0);
    }

    @Override
    protected void addBehaviourGoals() {
        super.addBehaviourGoals();
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Animal.class, true));
    }

    @Override
    protected boolean isSunSensitive() {
        return false;
    }

    @Override
    protected boolean convertsInWater() {
        return false;
    }

    @Override
    public boolean canBeAffected(MobEffectInstance effect) {
        if (effect.is(ModEffects.INFECTION)) {
            return false;
        }
        return super.canBeAffected(effect);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && target instanceof LivingEntity living) {
            Infection.tryInfect(living, 600, 0);
        }
        return hit;
    }

    @Override
    public boolean killedEntity(ServerLevel level, LivingEntity victim) {
        // Sin conversión a aldeano zombi: de eso ya se encarga la infección.
        return true;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        // Va dejando un rastro de infección por donde camina.
        if (this.level() instanceof ServerLevel serverLevel && this.tickCount % 40 == 0
                && this.random.nextInt(4) == 0 && this.onGround()) {
            BlockPos below = this.getBlockPosBelowThatAffectsMyMovement();
            Infection.infectBlock(serverLevel, below, false);
        }
    }
}
