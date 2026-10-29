package com.zidle.config;

/** Valores iniciales y balance (Fase 1 de la hoja de ruta). */
public final class Constants {
    private Constants() {}

    public static final String MOD_ID = "zidle";

    // Tick lógico: 10/s. Minecraft corre a 20 ticks/s, así que se ejecuta cada 2 ticks.
    public static final int LOGIC_TICK_INTERVAL = 2;
    public static final double DT = 0.1;

    public static final double COST_GROWTH = 1.15;
    public static final double POPULATION = 7_800_000_000.0;

    // Vacuna
    public static final double VACCINE_START_VIRUS = 100_000;
    public static final double VACCINE_BASE_PER_SEC = 0.05;   // % por segundo (~33 min hasta 100%)
    public static final double VACCINE_DEBUFF = 0.10;         // producción x0.10 (-90%) con vacuna al 100%
    public static final double SABOTAGE_COST_FRACTION = 0.25; // gasta 25% del virus actual
    public static final double SABOTAGE_REDUCTION = 30.0;     // puntos de vacuna que se restan

    // Offline
    public static final double OFFLINE_MIN_SECONDS = 60;
    public static final double OFFLINE_MAX_SECONDS = 43_200;  // 12 h
    public static final double OFFLINE_EFFICIENCY = 0.5;

    // Prestigio
    // OJO: la hoja de ruta usaba 10^10 y el prestigio se activa en 7.8e9 -> daba 0 ADN.
    // Con 10^9 el primer prestigio da floor(sqrt(7.8)) = 2 ADN.
    public static final double ADN_DIVISOR = 1e9;
    public static final double PRESTIGE_MULT_PER_ADN = 0.02;
    public static final double GLOBAL_UPGRADE_MULT = 1.5;
}
