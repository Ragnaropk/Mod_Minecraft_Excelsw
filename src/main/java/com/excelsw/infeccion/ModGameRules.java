package com.excelsw.infeccion;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.world.level.GameRules;

public final class ModGameRules {
    /** Si la infección se propaga por los bloques. */
    public static GameRules.Key<GameRules.BooleanValue> SPREAD;
    /** Intentos de propagación por cada tick aleatorio de un bloque infectado. */
    public static GameRules.Key<GameRules.IntegerValue> SPREAD_SPEED;
    /** Si caen meteoritos infectados cerca de los jugadores. */
    public static GameRules.Key<GameRules.BooleanValue> METEORS;
    /** Ticks entre meteoritos (24000 = un día de Minecraft). */
    public static GameRules.Key<GameRules.IntegerValue> METEOR_INTERVAL;
    /** Si las criaturas que mueren infectadas se levantan como Infectados. */
    public static GameRules.Key<GameRules.BooleanValue> RESURRECTION;

    private ModGameRules() {
    }

    public static void register() {
        SPREAD = GameRuleRegistry.register("infeccionPropagacion",
                GameRules.Category.UPDATES, GameRuleFactory.createBooleanRule(true));
        SPREAD_SPEED = GameRuleRegistry.register("infeccionVelocidad",
                GameRules.Category.UPDATES, GameRuleFactory.createIntRule(1, 0));
        METEORS = GameRuleRegistry.register("infeccionMeteoritos",
                GameRules.Category.MISC, GameRuleFactory.createBooleanRule(true));
        METEOR_INTERVAL = GameRuleRegistry.register("infeccionIntervaloMeteoritos",
                GameRules.Category.MISC, GameRuleFactory.createIntRule(12000, 200));
        RESURRECTION = GameRuleRegistry.register("infeccionResurreccion",
                GameRules.Category.MOBS, GameRuleFactory.createBooleanRule(true));
    }
}
