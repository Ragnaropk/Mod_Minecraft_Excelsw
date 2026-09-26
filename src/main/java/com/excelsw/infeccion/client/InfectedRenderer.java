package com.excelsw.infeccion.client;

import com.excelsw.infeccion.InfeccionMod;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ZombieRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Zombie;

public class InfectedRenderer extends ZombieRenderer {
    private static final ResourceLocation TEXTURE = InfeccionMod.id("textures/entity/infected.png");

    public InfectedRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(Zombie entity) {
        return TEXTURE;
    }
}
