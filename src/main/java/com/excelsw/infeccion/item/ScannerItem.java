package com.excelsw.infeccion.item;

import com.excelsw.infeccion.Infection;
import com.excelsw.infeccion.InfectionData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Escáner de infección: fase global, contaminación local y dirección al núcleo más cercano. */
public class ScannerItem extends Item {
    private static final int SCAN_RADIUS = 12;
    private static final String[] DIRECTIONS = {"E", "SE", "S", "SO", "O", "NO", "N", "NE"};

    public ScannerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level instanceof ServerLevel serverLevel) {
            InfectionData data = InfectionData.get(serverLevel);
            int phase = data.phase();
            player.sendSystemMessage(Component.translatable("message.infeccion.scanner.header")
                    .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
            player.sendSystemMessage(Component.translatable("message.infeccion.scanner.phase",
                    phase + 1, Component.translatable("phase.infeccion." + phase), data.points(),
                    Math.round(data.phaseProgress() * 100.0F)).withStyle(ChatFormatting.LIGHT_PURPLE));

            int percent = localContamination(serverLevel, player.blockPosition());
            ChatFormatting color = percent >= 40 ? ChatFormatting.RED : percent >= 10 ? ChatFormatting.GOLD : ChatFormatting.GREEN;
            player.sendSystemMessage(Component.translatable("message.infeccion.scanner.local", percent).withStyle(color));

            BlockPos hive = level.dimension() == Level.OVERWORLD ? data.nearestHive(player.blockPosition()) : null;
            if (hive == null) {
                player.sendSystemMessage(Component.translatable("message.infeccion.scanner.no_hive")
                        .withStyle(ChatFormatting.GRAY));
            } else {
                int dx = hive.getX() - player.getBlockX();
                int dz = hive.getZ() - player.getBlockZ();
                int distance = (int) Math.sqrt((double) dx * dx + (double) dz * dz);
                double angle = (Math.toDegrees(Math.atan2(dz, dx)) + 360.0) % 360.0;
                String dir = DIRECTIONS[(int) Math.round(angle / 45.0) % 8];
                player.sendSystemMessage(Component.translatable("message.infeccion.scanner.hive",
                        distance, dir, data.hiveCount()).withStyle(ChatFormatting.YELLOW));
            }
            serverLevel.playSound(null, player.blockPosition(), SoundEvents.SCULK_CLICKING, SoundSource.PLAYERS, 1.0F, 1.5F);
            player.getCooldowns().addCooldown(this, 60);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    /** Porcentaje de bloques sólidos infectados alrededor del jugador. */
    private static int localContamination(ServerLevel level, BlockPos center) {
        int solid = 0;
        int infected = 0;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-SCAN_RADIUS, -6, -SCAN_RADIUS),
                center.offset(SCAN_RADIUS, 6, SCAN_RADIUS))) {
            var state = level.getBlockState(pos);
            if (state.isAir()) {
                continue;
            }
            solid++;
            if (Infection.isInfected(state)) {
                infected++;
            }
        }
        return solid == 0 ? 0 : Math.round(infected * 100.0F / solid);
    }
}
