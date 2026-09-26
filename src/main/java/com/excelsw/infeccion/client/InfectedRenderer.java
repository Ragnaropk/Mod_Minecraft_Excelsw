package com.excelsw.infeccion.client;

import com.excelsw.infeccion.InfeccionMod;
import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ZombieRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Zombie;

/** Renderizado de todos los Infectados: modelo de zombi, textura propia y ojos que brillan en la oscuridad. */
public class InfectedRenderer extends ZombieRenderer {
    private static final RenderType EYES = RenderType.eyes(InfeccionMod.id("textures/entity/infected_eyes.png"));

    private final ResourceLocation texture;

    public InfectedRenderer(EntityRendererProvider.Context context, String textureName) {
        super(context);
        this.texture = InfeccionMod.id("textures/entity/" + textureName + ".png");
        this.addLayer(new EyesLayer<Zombie, ZombieModel<Zombie>>(this) {
            @Override
            public RenderType renderType() {
                return EYES;
            }
        });
    }

    @Override
    public ResourceLocation getTextureLocation(Zombie entity) {
        return texture;
    }
}
