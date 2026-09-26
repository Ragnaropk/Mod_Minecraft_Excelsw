package com.excelsw.infeccion.effect;

import com.excelsw.infeccion.InfeccionMod;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;

public final class ModEffects {
    public static final Holder<MobEffect> INFECTION = Registry.registerForHolder(
            BuiltInRegistries.MOB_EFFECT, InfeccionMod.id("infection"), new InfectionEffect());
    public static final Holder<MobEffect> IMMUNITY = Registry.registerForHolder(
            BuiltInRegistries.MOB_EFFECT, InfeccionMod.id("immunity"), new ImmunityEffect());

    private ModEffects() {
    }

    public static void register() {
        // Carga la clase para registrar los efectos.
    }
}
