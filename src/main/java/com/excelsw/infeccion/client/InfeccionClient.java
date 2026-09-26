package com.excelsw.infeccion.client;

import com.excelsw.infeccion.block.ModBlocks;
import com.excelsw.infeccion.entity.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.monster.Zombie;

public class InfeccionClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.<Zombie>register(ModEntities.INFECTED, InfectedRenderer::new);
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.INFECTED_LEAVES, RenderType.cutoutMipped());
    }
}
