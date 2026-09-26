package com.excelsw.infeccion.entity;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** Escupidor: mantiene la distancia y lanza bolas de esporas que infectan todo lo que tocan. */
public class InfectedSpitterEntity extends InfectedEntity implements RangedAttackMob {
    public InfectedSpitterEntity(EntityType<? extends Zombie> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createSpitterAttributes() {
        return InfectedEntity.createInfectedAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25);
    }

    @Override
    protected void addBehaviourGoals() {
        this.goalSelector.addGoal(2, new RangedAttackGoal(this, 1.0, 30, 50, 14.0F));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Animal.class, true));
    }

    @Override
    public void performRangedAttack(LivingEntity target, float power) {
        SporeProjectile spore = new SporeProjectile(level(), this);
        double dx = target.getX() - getX();
        double dy = target.getY(0.33) - spore.getY();
        double dz = target.getZ() - getZ();
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        spore.shoot(dx, dy + horizontal * 0.15, dz, 1.3F, 5.0F);
        playSound(SoundEvents.LLAMA_SPIT, 1.0F, 0.5F);
        level().addFreshEntity(spore);
    }
}
