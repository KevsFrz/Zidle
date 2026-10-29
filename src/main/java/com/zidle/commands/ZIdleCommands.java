package com.zidle.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.zidle.config.Producers;
import com.zidle.config.Upgrades;
import com.zidle.core.PlayerState;
import com.zidle.core.ZIdleSaveData;
import com.zidle.managers.EconomyManager;
import com.zidle.managers.PrestigeManager;
import com.zidle.managers.PrestigeManager.Skill;
import com.zidle.managers.VaccineManager;
import com.zidle.utils.Formatters;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Arrays;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

/** Interfaz del juego: /zidle ... (mercado, mejoras, vacuna, prestigio). */
public final class ZIdleCommands {
    private ZIdleCommands() {}

    public static void register(CommandDispatcher<ServerCommandSource> d, CommandRegistryAccess access,
                                CommandManager.RegistrationEnvironment env) {
        d.register(literal("zidle")
            .executes(ZIdleCommands::status)
            .then(literal("ayuda").executes(ZIdleCommands::help))
            .then(literal("mercado").executes(ZIdleCommands::market))
            .then(literal("comprar")
                .then(argument("productor", IntegerArgumentType.integer(1, Producers.COUNT))
                    .executes(c -> buy(c, 1))
                    .then(literal("max").executes(c -> buy(c, -1)))
                    .then(argument("cantidad", IntegerArgumentType.integer(1, 100000))
                        .executes(c -> buy(c, IntegerArgumentType.getInteger(c, "cantidad"))))))
            .then(literal("mejoras").executes(ZIdleCommands::upgradesList))
            .then(literal("mejora")
                .then(argument("id", StringArgumentType.word())
                    .suggests((c, b) -> CommandSource.suggestMatching(Upgrades.BY_ID.keySet(), b))
                    .executes(ZIdleCommands::buyUpgrade)))
            .then(literal("sabotage").executes(ZIdleCommands::sabotage))
            .then(literal("prestigio")
                .executes(ZIdleCommands::prestigeInfo)
                .then(literal("confirmar").executes(ZIdleCommands::prestigeDo)))
            .then(literal("adn")
                .executes(ZIdleCommands::adnTree)
                .then(literal("comprar")
                    .then(argument("habilidad", StringArgumentType.word())
                        .suggests((c, b) -> CommandSource.suggestMatching(
                            Arrays.stream(Skill.values()).map(k -> k.id), b))
                        .executes(ZIdleCommands::adnBuy)))));
    }

    // ---------- helpers ----------
    private static PlayerState state(CommandContext<ServerCommandSource> c) throws CommandSyntaxException {
        var p = c.getSource().getPlayerOrThrow();
        ZIdleSaveData data = ZIdleSaveData.get(c.getSource().getServer());
        data.markDirty();
        return data.of(p.getUuid());
    }

    private static void say(CommandContext<ServerCommandSource> c, String msg, Formatting color) {
        c.getSource().sendFeedback(() -> Text.literal(msg).formatted(color), false);
    }

    private static void say(CommandContext<ServerCommandSource> c, String msg) { say(c, msg, Formatting.WHITE); }

    // ---------- comandos ----------
    private static int help(CommandContext<ServerCommandSource> c) {
        say(c, "/zidle · estado | mercado | comprar <n> [cantidad|max] | mejoras | mejora <id>", Formatting.GREEN);
        say(c, "/zidle sabotage · prestigio [confirmar] · adn [comprar <habilidad>]", Formatting.GREEN);
        say(c, "Clic derecho con el Colmillo Infectado = infectar manualmente.", Formatting.GRAY);
        return 1;
    }

    private static int status(CommandContext<ServerCommandSource> c) throws CommandSyntaxException {
        PlayerState s = state(c);
        say(c, "=== Project Z-Idle ===", Formatting.GREEN);
        say(c, "Virus: " + Formatters.format(s.virus) + "  (" + Formatters.format(EconomyManager.vps(s)) + "/s)");
        say(c, "Infección mundial: " + String.format("%.3f", s.infectedPercent()) + "%");
        Formatting vc = s.vaccineComplete() ? Formatting.RED : Formatting.YELLOW;
        say(c, "Vacuna: " + String.format("%.1f", s.vaccine) + "%" + (s.vaccineActive() ? "" : " (aún sin investigar)"), vc);
        if (s.prestiges > 0 || s.adn > 0) {
            say(c, "ADN mutante: " + Formatters.format(s.adn) + " · Apocalipsis: " + s.prestiges, Formatting.LIGHT_PURPLE);
        }
        return 1;
    }

    private static int market(CommandContext<ServerCommandSource> c) throws CommandSyntaxException {
        PlayerState s = state(c);
        say(c, "=== Mercado de agentes de infección ===", Formatting.GREEN);
        for (int i = 0; i < Producers.COUNT; i++) {
            Producers.Producer p = Producers.ALL.get(i);
            double cost = EconomyManager.cost(i, s.owned[i]);
            Formatting f = s.virus >= cost ? Formatting.GREEN : Formatting.DARK_GRAY;
            say(c, (i + 1) + ". " + p.name() + " x" + s.owned[i] + " · costo " + Formatters.format(cost)
                + " · +" + Formatters.format(p.baseVps()) + "/s", f);
        }
        return 1;
    }

    private static int buy(CommandContext<ServerCommandSource> c, int amount) throws CommandSyntaxException {
        PlayerState s = state(c);
        int idx = IntegerArgumentType.getInteger(c, "productor") - 1;
        int bought = EconomyManager.buy(s, idx, amount);
        if (bought == 0) {
            say(c, "No tienes suficiente virus.", Formatting.RED);
            return 0;
        }
        say(c, "Compraste " + bought + "x " + Producers.ALL.get(idx).name() + " (total " + s.owned[idx] + ").", Formatting.GREEN);
        return bought;
    }

    private static int upgradesList(CommandContext<ServerCommandSource> c) throws CommandSyntaxException {
        PlayerState s = state(c);
        int shown = 0;
        say(c, "=== Laboratorio de mutaciones ===", Formatting.GREEN);
        for (Upgrades.Upgrade u : Upgrades.ALL) {
            if (!EconomyManager.upgradeVisible(s, u)) continue;
            Formatting f = s.virus >= u.cost() ? Formatting.GREEN : Formatting.DARK_GRAY;
            say(c, u.id() + " · " + u.name() + " · " + Formatters.format(u.cost()), f);
            if (++shown >= 12) { say(c, "... (se muestran las 12 primeras disponibles)", Formatting.GRAY); break; }
        }
        if (shown == 0) say(c, "No hay mejoras disponibles por ahora.", Formatting.GRAY);
        return shown;
    }

    private static int buyUpgrade(CommandContext<ServerCommandSource> c) throws CommandSyntaxException {
        PlayerState s = state(c);
        int r = EconomyManager.buyUpgrade(s, StringArgumentType.getString(c, "id"));
        switch (r) {
            case 0 -> say(c, "Mutación adquirida.", Formatting.GREEN);
            case 1 -> say(c, "Esa mejora no existe, ya la tienes o aún no está disponible.", Formatting.RED);
            default -> say(c, "No tienes suficiente virus.", Formatting.RED);
        }
        return r == 0 ? 1 : 0;
    }

    private static int sabotage(CommandContext<ServerCommandSource> c) throws CommandSyntaxException {
        PlayerState s = state(c);
        if (!s.vaccineComplete()) {
            say(c, "Solo puedes sabotear los laboratorios cuando la vacuna llegó al 100%.", Formatting.RED);
            return 0;
        }
        double cost = VaccineManager.sabotageCost(s);
        VaccineManager.sabotage(s);
        say(c, "Laboratorios saboteados: -" + Formatters.format(cost) + " virus, vacuna al "
            + String.format("%.0f", s.vaccine) + "%.", Formatting.YELLOW);
        return 1;
    }

    private static int prestigeInfo(CommandContext<ServerCommandSource> c) throws CommandSyntaxException {
        PlayerState s = state(c);
        if (!PrestigeManager.canPrestige(s)) {
            say(c, "Necesitas infectar al 100% de la población (7.8B). Llevas " + String.format("%.2f", s.infectedPercent()) + "%.", Formatting.GRAY);
            return 0;
        }
        say(c, "Apocalipsis Total disponible: +" + PrestigeManager.adnGain(s) + " ADN. Reinicia virus, edificios y mejoras.", Formatting.LIGHT_PURPLE);
        say(c, "Confirma con /zidle prestigio confirmar", Formatting.LIGHT_PURPLE);
        return 1;
    }

    private static int prestigeDo(CommandContext<ServerCommandSource> c) throws CommandSyntaxException {
        PlayerState s = state(c);
        int gain = PrestigeManager.prestige(s);
        if (gain < 0) {
            say(c, "Aún no has infectado a toda la población.", Formatting.RED);
            return 0;
        }
        say(c, "¡APOCALIPSIS TOTAL! +" + gain + " ADN mutante. La pandemia vuelve a empezar.", Formatting.LIGHT_PURPLE);
        return 1;
    }

    private static int adnTree(CommandContext<ServerCommandSource> c) throws CommandSyntaxException {
        PlayerState s = state(c);
        say(c, "=== Árbol de ADN mutante (ADN: " + Formatters.format(s.adn) + ") ===", Formatting.LIGHT_PURPLE);
        for (Skill k : Skill.values()) {
            say(c, k.id + " · " + k.title + " [" + k.level(s) + "/" + k.maxLevel + "] · "
                + (k.level(s) >= k.maxLevel ? "MAX" : k.cost(s) + " ADN") + " · " + k.desc, Formatting.LIGHT_PURPLE);
        }
        return 1;
    }

    private static int adnBuy(CommandContext<ServerCommandSource> c) throws CommandSyntaxException {
        PlayerState s = state(c);
        Skill k = Skill.byId(StringArgumentType.getString(c, "habilidad"));
        if (k == null) { say(c, "Esa habilidad no existe.", Formatting.RED); return 0; }
        int r = PrestigeManager.buySkill(s, k);
        switch (r) {
            case 0 -> say(c, k.title + " subió a nivel " + k.level(s) + ".", Formatting.LIGHT_PURPLE);
            case 1 -> say(c, "Ya está al nivel máximo.", Formatting.RED);
            default -> say(c, "ADN insuficiente.", Formatting.RED);
        }
        return r == 0 ? 1 : 0;
    }
}
