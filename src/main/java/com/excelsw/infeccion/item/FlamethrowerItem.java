package com.excelsw.infeccion.item;

import com.excelsw.infeccion.Infection;
import com.excelsw.infeccion.entity.InfectedEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Lanzallamas: mantén el clic derecho para quemar la infección. Purifica los bloques
 * que alcanza, prende fuego a las criaturas y hace el doble de daño a los Infectados.
 * Se recarga (repara) con polvo de blaze en un yunque.
 */
public class FlamethrowerItem extends Item {
    public static final double RANGE = 8.0;

    public FlamethrowerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repair) {
        return repair.is(Items.BLAZE_POWDER);
    }

    @Override
    public void onUseTick(Level level, LivingEntity user, ItemStack stack, int remainingUseDuration) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        Vec3 eye = user.getEyePosition();
        Vec3 look = user.getLookAngle();
        Vec3 nozzle = eye.add(look.scale(0.8)).add(0.0, -0.25, 0.0);

        for (int i = 0; i < 6; i++) {
            Vec3 dir = look.add((user.getRandom().nextDouble() - 0.5) * 0.25,
                    (user.getRandom().nextDouble() - 0.5) * 0.25,
                    (user.getRandom().nextDouble() - 0.5) * 0.25).scale(0.7);
            serverLevel.sendParticles(i % 3 == 0 ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.FLAME,
                    nozzle.x, nozzle.y, nozzle.z, 0, dir.x, dir.y, dir.z, 1.0);
        }
        if (remainingUseDuration % 4 != 0) {
            return;
        }
        serverLevel.playSound(null, user.blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 0.4F, 1.6F);

        // Criaturas dentro del cono de fuego.
        AABB box = user.getBoundingBox().expandTowards(look.scale(RANGE)).inflate(1.5);
        for (LivingEntity target : serverLevel.getEntitiesOfClass(LivingEntity.class, box, e -> e != user && e.isAlive())) {
            Vec3 toTarget = target.position().add(0.0, target.getBbHeight() * 0.5, 0.0).subtract(eye);
            if (toTarget.length() <= RANGE && toTarget.normalize().dot(look) > 0.85) {
                target.igniteForSeconds(4.0F);
                target.hurt(serverLevel.damageSources().inFire(), target instanceof InfectedEntity ? 3.0F : 1.5F);
            }
        }

        // Bloques en la trayectoria: quema la infección hasta chocar con algo sólido y sano.
        for (double d = 1.0; d <= RANGE; d += 0.75) {
            BlockPos pos = BlockPos.containing(eye.add(look.scale(d)));
            boolean infected = Infection.isInfected(serverLevel.getBlockState(pos));
            if (!infected && serverLevel.getBlockState(pos).isAir()) {
                continue;
            }
            if (infected) {
                Infection.purifyArea(serverLevel, pos, 1);
                BlockPos above = pos.above();
                if (serverLevel.getBlockState(above).isAir() && user.getRandom().nextInt(5) == 0) {
                    serverLevel.setBlockAndUpdate(above, BaseFireBlock.getState(serverLevel, above));
                }
            }
            break;
        }

        if (!(user instanceof Player player && player.getAbilities().instabuild)) {
            stack.hurtAndBreak(1, user, LivingEntity.getSlotForHand(user.getUsedItemHand()));
        }
    }
}
