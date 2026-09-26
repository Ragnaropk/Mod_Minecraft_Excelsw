package com.excelsw.infeccion;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.level.block.Block;

public final class ModTags {
    /** Bloques que la infección nunca puede tocar. */
    public static final TagKey<Block> INFECTION_IMMUNE =
            TagKey.create(Registries.BLOCK, InfeccionMod.id("infection_immune"));

    public static final ResourceKey<DamageType> INFECTION_DAMAGE =
            ResourceKey.create(Registries.DAMAGE_TYPE, InfeccionMod.id("infection"));

    private ModTags() {
    }
}
