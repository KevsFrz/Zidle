package com.zidle.core;

import com.zidle.config.Constants;
import com.zidle.config.Producers;

import java.util.HashSet;
import java.util.Set;

/** Estado de un jugador (sin dependencias de Minecraft para poder probarlo aparte). */
public class PlayerState {
    public double virus;          // virusActual
    public double virusRun;       // virus producido en la partida actual (usado para vacuna y prestigio)
    public double virusLifetime;  // virusHistoricoTotal (nunca se reinicia)
    public double vaccine;        // avanceVacunaMundial 0..100
    public double adn;            // ADN mutante disponible
    public int prestiges;

    public final int[] owned = new int[Producers.COUNT];
    public final Set<String> upgrades = new HashSet<>();

    // Niveles del árbol de ADN
    public int cepa;
    public int metabolismo;
    public int latente;

    public long lastSeen; // epoch ms, para progreso offline

    public boolean vaccineActive() { return virusRun >= Constants.VACCINE_START_VIRUS; }
    public boolean vaccineComplete() { return vaccine >= 100.0; }
    public double infectedPercent() { return Math.min(100.0, virusRun / Constants.POPULATION * 100.0); }
}
