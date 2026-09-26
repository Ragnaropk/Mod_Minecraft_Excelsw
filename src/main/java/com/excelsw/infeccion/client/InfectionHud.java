package com.excelsw.infeccion.client;

import com.excelsw.infeccion.InfeccionMod;
import com.excelsw.infeccion.effect.ModEffects;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;

/** Efectos en pantalla de la infección: viñeta morada que late y latidos del corazón. */
public final class InfectionHud {
    private static final ResourceLocation VIGNETTE = InfeccionMod.id("textures/misc/infection_vignette.png");

    private InfectionHud() {
    }

    public static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) {
            return;
        }
        MobEffectInstance infection = mc.player.getEffect(ModEffects.INFECTION);
        if (infection == null) {
            return;
        }
        int amplifier = infection.getAmplifier();
        float time = mc.player.tickCount + deltaTracker.getGameTimeDeltaPartialTick(false);
        float pulse = 0.5F + 0.5F * Mth.sin(time * (0.1F + amplifier * 0.05F));
        float alpha = Mth.clamp(0.3F + amplifier * 0.17F + pulse * 0.2F, 0.0F, 0.95F);
        // Al final del efecto se desvanece.
        if (infection.getDuration() < 60 && !infection.isInfiniteDuration()) {
            alpha *= infection.getDuration() / 60.0F;
        }

        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.setColor(1.0F, 1.0F, 1.0F, alpha);
        graphics.blit(VIGNETTE, 0, 0, 0.0F, 0.0F, width, height, width, height);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }

    public static void clientTick(Minecraft mc) {
        if (mc.player == null || mc.isPaused()) {
            return;
        }
        MobEffectInstance infection = mc.player.getEffect(ModEffects.INFECTION);
        if (infection == null || infection.getAmplifier() < 1) {
            return;
        }
        int interval = 40 - infection.getAmplifier() * 8;
        if (mc.player.tickCount % interval == 0) {
            mc.player.playSound(SoundEvents.WARDEN_HEARTBEAT, 0.5F + infection.getAmplifier() * 0.15F, 1.1F);
        }
    }
}
