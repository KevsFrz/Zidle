package com.zidle.utils;

import java.util.Locale;

public final class Formatters {
    private Formatters() {}

    private static final String[] SUFFIX = {"", "K", "M", "B", "T", "Qa", "Qi", "Sx", "Sp", "Oc", "No", "Dc"};

    public static String format(double v) {
        if (Double.isNaN(v) || Double.isInfinite(v)) return "∞";
        if (v < 1000) return v < 10 ? String.format(Locale.ROOT, "%.2f", v) : String.format(Locale.ROOT, "%.1f", v);
        int idx = (int) Math.floor(Math.log10(v) / 3);
        if (idx >= SUFFIX.length) return String.format(Locale.ROOT, "%.2e", v);
        return String.format(Locale.ROOT, "%.2f%s", v / Math.pow(1000, idx), SUFFIX[idx]);
    }
}
