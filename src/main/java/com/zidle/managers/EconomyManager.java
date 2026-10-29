package com.zidle.managers;

import com.zidle.config.Constants;
import com.zidle.config.Producers;
import com.zidle.config.Upgrades;
import com.zidle.config.Upgrades.Type;
import com.zidle.config.Upgrades.Upgrade;
import com.zidle.core.PlayerState;

/** Costos, compras, producción pasiva y progreso offline. */
public final class EconomyManager {
    private EconomyManager() {}

    private static final double G = Constants.COST_GROWTH;

    // ---------- costos ----------
    public static double cost(int idx, int owned) {
        return Producers.ALL.get(idx).baseCost() * Math.pow(G, owned);
    }

    /** CostoTotal(k) = CostoBase * 1.15^n * (1.15^k - 1) / 0.15 */
    public static double bulkCost(int idx, int owned, int k) {
        if (k <= 0) return 0;
        return cost(idx, owned) * (Math.pow(G, k) - 1) / (G - 1);
    }

    public static int maxAffordable(int idx, int owned, double virus) {
        double c0 = cost(idx, owned);
        if (virus < c0) return 0;
        int k = (int) Math.min(1_000_000, Math.floor(Math.log(1 + virus * (G - 1) / c0) / Math.log(G)));
        while (k > 0 && bulkCost(idx, owned, k) > virus) k--;
        return k;
    }

    /** amount = -1 para MAX. Devuelve cuántos se compraron. */
    public static int buy(PlayerState s, int idx, int amount) {
        int k = amount < 0 ? maxAffordable(idx, s.owned[idx], s.virus) : amount;
        if (k <= 0) return 0;
        double total = bulkCost(idx, s.owned[idx], k);
        if (total > s.virus) return 0;
        s.virus -= total;
        s.owned[idx] += k;
        return k;
    }

    // ---------- mejoras ----------
    public static boolean upgradeVisible(PlayerState s, Upgrade u) {
        if (s.upgrades.contains(u.id())) return false;
        if (u.type() == Type.PRODUCER) return s.owned[u.target()] >= u.reqOwned();
        return s.virusRun >= u.cost() / 2;
    }

    /** 0 = ok, 1 = no existe/ya comprada/no disponible, 2 = sin virus suficiente. */
    public static int buyUpgrade(PlayerState s, String id) {
        Upgrade u = Upgrades.BY_ID.get(id);
        if (u == null || !upgradeVisible(s, u)) return 1;
        if (s.virus < u.cost()) return 2;
        s.virus -= u.cost();
        s.upgrades.add(u.id());
        return 0;
    }

    // ---------- producción ----------
    private static double producerMult(PlayerState s, int idx) {
        double m = 1;
        for (Upgrade u : Upgrades.ALL) {
            if (u.type() == Type.PRODUCER && u.target() == idx && s.upgrades.contains(u.id())) m *= u.mult();
        }
        return m;
    }

    public static double globalMultiplier(PlayerState s) {
        double m = 1;
        for (Upgrade u : Upgrades.ALL) {
            if (u.type() == Type.GLOBAL && s.upgrades.contains(u.id())) m *= u.mult();
        }
        m *= 1 + 0.10 * s.metabolismo;                                // Metabolismo Acelerado
        m *= 1 + Constants.PRESTIGE_MULT_PER_ADN * s.adn;             // multiplicadorPrestigio
        if (s.vaccineComplete()) m *= Constants.VACCINE_DEBUFF;       // vacuna al 100%
        return m;
    }

    /** VPS_Total = SUMA(cant_i * vpsBase_i * mult_i) * multGlobal */
    public static double vps(PlayerState s) {
        double sum = 0;
        for (int i = 0; i < Producers.COUNT; i++) {
            sum += s.owned[i] * Producers.ALL.get(i).baseVps() * producerMult(s, i);
        }
        return sum * globalMultiplier(s);
    }

    public static double clickPower(PlayerState s) {
        double m = 1;
        for (Upgrade u : Upgrades.ALL) {
            if (u.type() == Type.CLICK && s.upgrades.contains(u.id())) m *= u.mult();
        }
        double base = m * (1 + vps(s) * 0.01);
        return s.vaccineComplete() ? base * Constants.VACCINE_DEBUFF : base;
    }

    public static void addVirus(PlayerState s, double amount) {
        s.virus += amount;
        s.virusRun += amount;
        s.virusLifetime += amount;
    }

    public static double click(PlayerState s) {
        double g = clickPower(s);
        addVirus(s, g);
        return g;
    }

    /** Un tick lógico (dt en segundos). */
    public static void tick(PlayerState s, double dt) {
        addVirus(s, vps(s) * dt);
        VaccineManager.update(s, dt);
    }

    // ---------- offline ----------
    /** Devuelve los virus ganados (0 si el tiempo no supera el mínimo). */
    public static double applyOffline(PlayerState s, double seconds) {
        if (seconds <= Constants.OFFLINE_MIN_SECONDS) return 0;
        double t = Math.min(seconds, Constants.OFFLINE_MAX_SECONDS);
        double gained = vps(s) * t * Constants.OFFLINE_EFFICIENCY;
        addVirus(s, gained);
        return gained;
    }
}
