package com.excelsw.infeccion;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;

/**
 * Estado global de la plaga, guardado con el mundo: los puntos de amenaza (que deciden la fase)
 * y la posición de todos los núcleos infecciosos del Overworld.
 */
public class InfectionData extends SavedData {
    /** Puntos necesarios para entrar en cada fase: Brote, Epidemia, Pandemia, Apocalipsis. */
    public static final long[] PHASE_THRESHOLDS = {0L, 1500L, 6000L, 15000L};
    public static final int MAX_PHASE = PHASE_THRESHOLDS.length - 1;

    private static final SavedData.Factory<InfectionData> FACTORY =
            new SavedData.Factory<>(InfectionData::new, InfectionData::load, null);

    private long points;
    private final LongOpenHashSet hives = new LongOpenHashSet();

    public static InfectionData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(FACTORY, InfeccionMod.MOD_ID);
    }

    public static InfectionData get(ServerLevel level) {
        return get(level.getServer());
    }

    private static InfectionData load(CompoundTag tag, HolderLookup.Provider registries) {
        InfectionData data = new InfectionData();
        data.points = tag.getLong("Points");
        for (long hive : tag.getLongArray("Hives")) {
            data.hives.add(hive);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putLong("Points", points);
        tag.putLongArray("Hives", hives.toLongArray());
        return tag;
    }

    public long points() {
        return points;
    }

    public void setPoints(long value) {
        points = Math.max(0L, value);
        setDirty();
    }

    public void addPoints(long amount) {
        setPoints(points + amount);
    }

    public int phase() {
        for (int i = MAX_PHASE; i > 0; i--) {
            if (points >= PHASE_THRESHOLDS[i]) {
                return i;
            }
        }
        return 0;
    }

    /** Progreso (0..1) dentro de la fase actual. En la última fase sigue llenándose hasta el doble. */
    public float phaseProgress() {
        int phase = phase();
        long start = PHASE_THRESHOLDS[phase];
        long end = phase < MAX_PHASE ? PHASE_THRESHOLDS[phase + 1] : start * 2L;
        return Math.min(1.0F, (float) (points - start) / (float) (end - start));
    }

    public void addHive(BlockPos pos) {
        if (hives.add(pos.asLong())) {
            setDirty();
        }
    }

    public void removeHive(BlockPos pos) {
        if (hives.remove(pos.asLong())) {
            setDirty();
        }
    }

    public int hiveCount() {
        return hives.size();
    }

    @Nullable
    public BlockPos nearestHive(BlockPos from) {
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        for (long packed : hives) {
            BlockPos pos = BlockPos.of(packed);
            double dist = pos.distSqr(from);
            if (dist < bestDist) {
                bestDist = dist;
                best = pos;
            }
        }
        return best;
    }
}
