package com.excelsw.infeccion.client;

import com.excelsw.infeccion.block.ModBlocks;
import com.excelsw.infeccion.entity.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.world.entity.monster.Zombie;

public class InfeccionClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.<Zombie>register(ModEntities.INFECTED, ctx -> new InfectedRenderer(ctx, "infected"));
        EntityRendererRegistry.<Zombie>register(ModEntities.RUNNER, ctx -> new InfectedRenderer(ctx, "infected_runner"));
        EntityRendererRegistry.<Zombie>register(ModEntities.BRUTE, ctx -> new InfectedRenderer(ctx, "infected_brute"));
        EntityRendererRegistry.<Zombie>register(ModEntities.SPITTER, ctx -> new InfectedRenderer(ctx, "infected_spitter"));
        EntityRendererRegistry.<Zombie>register(ModEntities.BLOATER, BloaterRenderer::new);
        EntityRendererRegistry.register(ModEntities.SPORE, ThrownItemRenderer::new);
        EntityRendererRegistry.register(ModEntities.METEOR, ctx -> new ThrownItemRenderer<>(ctx, 4.0F, true));

        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.INFECTED_LEAVES, RenderType.cutoutMipped());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.INFECTED_GROWTH, RenderType.cutout());

        HudRenderCallback.EVENT.register(InfectionHud::render);
        ClientTickEvents.END_CLIENT_TICK.register(InfectionHud::clientTick);
    }
}
