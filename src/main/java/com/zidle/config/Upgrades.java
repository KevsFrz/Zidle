package com.zidle.config;

import java.util.*;

public final class Upgrades {
    private Upgrades() {}

    public enum Type { CLICK, PRODUCER, GLOBAL }

    /** target = índice de productor (solo PRODUCER); reqOwned = cantidad mínima del productor para verla. */
    public record Upgrade(String id, String name, double cost, Type type, int target, double mult, int reqOwned) {}

    public static final List<Upgrade> ALL = new ArrayList<>();
    public static final Map<String, Upgrade> BY_ID = new LinkedHashMap<>();

    static {
        add(new Upgrade("click1", "Colmillos afilados", 100, Type.CLICK, -1, 2, 0));
        add(new Upgrade("click2", "Mordida infecciosa", 10_000, Type.CLICK, -1, 2, 0));
        add(new Upgrade("click3", "Saliva tóxica", 1_000_000, Type.CLICK, -1, 2, 0));

        for (int i = 0; i < Producers.COUNT; i++) {
            Producers.Producer p = Producers.ALL.get(i);
            add(new Upgrade(p.id() + "_x2", p.name() + " x2", p.baseCost() * 100, Type.PRODUCER, i, 2, 10));
            add(new Upgrade(p.id() + "_x3", p.name() + " x3", p.baseCost() * 1_000, Type.PRODUCER, i, 3, 25));
            add(new Upgrade(p.id() + "_x5", p.name() + " x5", p.baseCost() * 20_000, Type.PRODUCER, i, 5, 50));
        }

        add(new Upgrade("global1", "Cepa agresiva", 50_000, Type.GLOBAL, -1, Constants.GLOBAL_UPGRADE_MULT, 0));
        add(new Upgrade("global2", "Cepa aerotransportada", 50_000_000, Type.GLOBAL, -1, Constants.GLOBAL_UPGRADE_MULT, 0));
        add(new Upgrade("global3", "Pandemia sincronizada", 50_000_000_000.0, Type.GLOBAL, -1, Constants.GLOBAL_UPGRADE_MULT, 0));
    }

    private static void add(Upgrade u) { ALL.add(u); BY_ID.put(u.id(), u); }
}
