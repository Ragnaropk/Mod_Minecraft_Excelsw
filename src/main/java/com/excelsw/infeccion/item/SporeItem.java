package com.excelsw.infeccion.item;

import com.excelsw.infeccion.Infection;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

/** Espora infecciosa: úsala sobre un bloque para empezar un foco, o sobre una criatura para contagiarla. */
public class SporeItem extends Item {
    public SporeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel() instanceof ServerLevel level)) {
            return Infection.infectedVersion(context.getLevel().getBlockState(context.getClickedPos())) != null
                    ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        if (!Infection.infectBlock(level, context.getClickedPos(), false)) {
            return InteractionResult.PASS;
        }
        level.playSound(null, context.getClickedPos(), SoundEvents.SCULK_BLOCK_SPREAD, SoundSource.BLOCKS, 1.0F, 0.8F);
        level.sendParticles(ParticleTypes.SQUID_INK, context.getClickedPos().getX() + 0.5,
                context.getClickedPos().getY() + 1.0, context.getClickedPos().getZ() + 0.5, 12, 0.3, 0.2, 0.3, 0.02);
        Player player = context.getPlayer();
        if (player == null || !player.getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (player.level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (Infection.tryInfect(target, 600, 0)) {
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }
}
