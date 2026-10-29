package com.zidle.core;

import com.zidle.config.Constants;
import com.zidle.config.Producers;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Guarda el estado de todos los jugadores en el mundo (data/zidle.dat). */
public class ZIdleSaveData extends PersistentState {
    private final Map<UUID, PlayerState> players = new HashMap<>();

    public static ZIdleSaveData get(MinecraftServer server) {
        return server.getOverworld().getPersistentStateManager()
            .getOrCreate(ZIdleSaveData::fromNbt, ZIdleSaveData::new, Constants.MOD_ID);
    }

    public PlayerState of(UUID id) { return players.computeIfAbsent(id, k -> new PlayerState()); }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtCompound all = new NbtCompound();
        players.forEach((id, s) -> all.put(id.toString(), write(s)));
        nbt.put("players", all);
        return nbt;
    }

    public static ZIdleSaveData fromNbt(NbtCompound nbt) {
        ZIdleSaveData data = new ZIdleSaveData();
        NbtCompound all = nbt.getCompound("players");
        for (String key : all.getKeys()) {
            data.players.put(UUID.fromString(key), read(all.getCompound(key)));
        }
        return data;
    }

    private static NbtCompound write(PlayerState s) {
        NbtCompound t = new NbtCompound();
        t.putDouble("virus", s.virus);
        t.putDouble("virusRun", s.virusRun);
        t.putDouble("virusLifetime", s.virusLifetime);
        t.putDouble("vaccine", s.vaccine);
        t.putDouble("adn", s.adn);
        t.putInt("prestiges", s.prestiges);
        t.putIntArray("owned", s.owned);
        NbtList ups = new NbtList();
        s.upgrades.forEach(u -> ups.add(NbtString.of(u)));
        t.put("upgrades", ups);
        t.putInt("cepa", s.cepa);
        t.putInt("metabolismo", s.metabolismo);
        t.putInt("latente", s.latente);
        t.putLong("lastSeen", s.lastSeen);
        return t;
    }

    private static PlayerState read(NbtCompound t) {
        PlayerState s = new PlayerState();
        s.virus = t.getDouble("virus");
        s.virusRun = t.getDouble("virusRun");
        s.virusLifetime = t.getDouble("virusLifetime");
        s.vaccine = t.getDouble("vaccine");
        s.adn = t.getDouble("adn");
        s.prestiges = t.getInt("prestiges");
        int[] o = t.getIntArray("owned");
        System.arraycopy(o, 0, s.owned, 0, Math.min(o.length, Producers.COUNT));
        NbtList ups = t.getList("upgrades", NbtElement.STRING_TYPE);
        for (int i = 0; i < ups.size(); i++) s.upgrades.add(ups.getString(i));
        s.cepa = t.getInt("cepa");
        s.metabolismo = t.getInt("metabolismo");
        s.latente = t.getInt("latente");
        s.lastSeen = t.getLong("lastSeen");
        return s;
    }
}
