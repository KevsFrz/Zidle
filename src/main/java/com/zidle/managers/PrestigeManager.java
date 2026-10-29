package com.zidle.managers;

import com.zidle.config.Constants;
import com.zidle.core.PlayerState;

/** Apocalipsis Total: prestigio y árbol de ADN mutante. */
public final class PrestigeManager {
    private PrestigeManager() {}

    public enum Skill {
        CEPA("cepa", "Cepa Autoinmune", "-5% avance de la vacuna por nivel", 5, 10),
        METABOLISMO("metabolismo", "Metabolismo Acelerado", "+10% VPS global por nivel", 3, 20),
        LATENTE("latente", "Transmisión Latente", "empiezas con edificios ya construidos", 10, 5);

        public final String id, title, desc;
        public final double baseCost;
        public final int maxLevel;

        Skill(String id, String title, String desc, double baseCost, int maxLevel) {
            this.id = id; this.title = title; this.desc = desc; this.baseCost = baseCost; this.maxLevel = maxLevel;
        }

        public int level(PlayerState s) {
            return switch (this) { case CEPA -> s.cepa; case METABOLISMO -> s.metabolismo; case LATENTE -> s.latente; };
        }

        void setLevel(PlayerState s, int v) {
            switch (this) { case CEPA -> s.cepa = v; case METABOLISMO -> s.metabolismo = v; case LATENTE -> s.latente = v; }
        }

        public int cost(PlayerState s) { return (int) Math.ceil(baseCost * Math.pow(1.5, level(s))); }

        public static Skill byId(String id) {
            for (Skill k : values()) if (k.id.equals(id)) return k;
            return null;
        }
    }

    public static boolean canPrestige(PlayerState s) { return s.virusRun >= Constants.POPULATION; }

    /** ADN_Ganado = Sqrt(virus de la partida / divisor) */
    public static int adnGain(PlayerState s) {
        return (int) Math.floor(Math.sqrt(s.virusRun / Constants.ADN_DIVISOR));
    }

    /** Devuelve el ADN ganado, o -1 si aún no se puede. */
    public static int prestige(PlayerState s) {
        if (!canPrestige(s)) return -1;
        int gain = adnGain(s);
        s.adn += gain;
        s.prestiges++;
        s.virus = 0;
        s.virusRun = 0;
        s.vaccine = 0;
        s.upgrades.clear();
        for (int i = 0; i < s.owned.length; i++) {
            // Transmisión Latente: nivel*(5-i) edificios de los 5 primeros productores
            s.owned[i] = Math.max(0, s.latente * (5 - i));
        }
        return gain;
    }

    /** 0 = ok, 1 = nivel máximo, 2 = ADN insuficiente. */
    public static int buySkill(PlayerState s, Skill k) {
        if (k.level(s) >= k.maxLevel) return 1;
        int c = k.cost(s);
        if (s.adn < c) return 2;
        s.adn -= c;
        k.setLevel(s, k.level(s) + 1);
        return 0;
    }
}
