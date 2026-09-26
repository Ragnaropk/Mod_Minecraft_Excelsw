package com.excelsw.infeccion.client;

import com.excelsw.infeccion.entity.InfectedBloaterEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Zombie;

/** El Hinchado se infla y parpadea antes de reventar, como un creeper. */
public class BloaterRenderer extends InfectedRenderer {
    public BloaterRenderer(EntityRendererProvider.Context context) {
        super(context, "infected_bloater");
    }

    @Override
    protected void scale(Zombie entity, PoseStack poseStack, float partialTick) {
        if (!(entity instanceof InfectedBloaterEntity bloater)) {
            return;
        }
        float swell = Mth.clamp(bloater.getSwelling(partialTick), 0.0F, 1.0F);
        float wobble = 1.0F + Mth.sin(swell * 100.0F) * swell * 0.01F;
        swell = swell * swell;
        swell = swell * swell;
        float xz = (1.0F + swell * 0.5F) * wobble;
        float y = (1.0F + swell * 0.15F) / wobble;
        poseStack.scale(xz, y, xz);
    }

    @Override
    protected float getWhiteOverlayProgress(Zombie entity, float partialTick) {
        if (!(entity instanceof InfectedBloaterEntity bloater)) {
            return 0.0F;
        }
        float swell = bloater.getSwelling(partialTick);
        return (int) (swell * 10.0F) % 2 == 0 ? 0.0F : Mth.clamp(swell, 0.5F, 1.0F);
    }
}
