package com.zidle.managers;

import com.zidle.config.Constants;
import com.zidle.core.PlayerState;

/** Resistencia humana: la cura mundial. */
public final class VaccineManager {
    private VaccineManager() {}

    public static double resistance(PlayerState s) {
        return Math.min(0.9, 0.05 * s.cepa); // Cepa Autoinmune: -5% por nivel
    }

    /** Incremento = Base * (1 - ResistenciaJugador) */
    public static void update(PlayerState s, double dt) {
        if (!s.vaccineActive() || s.vaccineComplete()) return;
        s.vaccine = Math.min(100.0, s.vaccine + Constants.VACCINE_BASE_PER_SEC * (1 - resistance(s)) * dt);
    }

    public static double sabotageCost(PlayerState s) {
        return s.virus * Constants.SABOTAGE_COST_FRACTION;
    }

    /** Solo disponible con la vacuna al 100%. */
    public static boolean sabotage(PlayerState s) {
        if (!s.vaccineComplete()) return false;
        s.virus -= sabotageCost(s);
        s.vaccine = Math.max(0, s.vaccine - Constants.SABOTAGE_REDUCTION);
        return true;
    }
}
