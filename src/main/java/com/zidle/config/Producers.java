package com.zidle.config;

import java.util.List;

public final class Producers {
    private Producers() {}

    public record Producer(String id, String name, double baseCost, double baseVps) {}

    public static final List<Producer> ALL = List.of(
        new Producer("alcantarilla", "Infección de Alcantarilla", 15, 0.5),
        new Producer("campus", "Estudiantes del Campus", 100, 4.0),
        new Producer("jaurias", "Jaurías Callejeras", 1_100, 32.0),
        new Producer("sanitarios", "Personal Sanitario Caído", 12_000, 260.0),
        new Producer("swat", "Fuerzas de Asalto (SWAT)", 130_000, 1_400.0),
        new Producer("laboratorios", "Laboratorios Clandestinos", 1_400_000, 7_800.0),
        new Producer("bases", "Bases Militarizadas", 20_000_000, 44_000.0),
        new Producer("satelites", "Satélites Contaminados", 330_000_000, 260_000.0)
    );

    public static final int COUNT = ALL.size();
}
