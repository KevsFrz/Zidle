package com.zidle;

import com.zidle.commands.ZIdleCommands;
import com.zidle.config.Constants;
import com.zidle.core.PlayerState;
import com.zidle.core.ZIdleSaveData;
import com.zidle.item.InfectedFangItem;
import com.zidle.managers.EconomyManager;
import com.zidle.utils.Formatters;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class ZIdleMod implements ModInitializer {
    public static Item FANG;
    private int counter = 0;

    @Override
    public void onInitialize() {
        FANG = Registry.register(Registries.ITEM, new Identifier(Constants.MOD_ID, "colmillo_infectado"),
            new InfectedFangItem(new FabricItemSettings().maxCount(1)));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(e -> e.add(FANG));

        CommandRegistrationCallback.EVENT.register(ZIdleCommands::register);
        ServerTickEvents.END_SERVER_TICK.register(this::onTick);

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> onJoin(handler.player, server));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ZIdleSaveData data = ZIdleSaveData.get(server);
            data.of(handler.player.getUuid()).lastSeen = System.currentTimeMillis();
            data.markDirty();
        });
    }

    private void onTick(MinecraftServer server) {
        counter++;
        if (counter % Constants.LOGIC_TICK_INTERVAL != 0) return;

        ZIdleSaveData data = ZIdleSaveData.get(server);
        boolean everySecond = counter % 20 == 0;

        for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
            PlayerState s = data.of(p.getUuid());
            boolean wasActive = s.vaccineActive();
            boolean wasComplete = s.vaccineComplete();

            EconomyManager.tick(s, Constants.DT);
            s.lastSeen = System.currentTimeMillis();

            if (!wasActive && s.vaccineActive()) {
                p.sendMessage(Text.literal("[Noticiero] Los gobiernos inician la investigación de la vacuna.")
                    .formatted(Formatting.YELLOW));
            }
            if (!wasComplete && s.vaccineComplete()) {
                p.sendMessage(Text.literal("[Noticiero] ¡La vacuna mundial está lista! Producción -90%. Usa /zidle sabotage.")
                    .formatted(Formatting.RED));
            }
            if (everySecond) {
                p.sendMessage(Text.literal("Virus " + Formatters.format(s.virus)
                    + " | " + Formatters.format(EconomyManager.vps(s)) + "/s"
                    + " | Vacuna " + String.format("%.1f", s.vaccine) + "%"
                    + " | Infección " + String.format("%.2f", s.infectedPercent()) + "%")
                    .formatted(Formatting.GREEN), true);
            }
        }
        if (everySecond) data.markDirty();
    }

    private void onJoin(ServerPlayerEntity p, MinecraftServer server) {
        ZIdleSaveData data = ZIdleSaveData.get(server);
        PlayerState s = data.of(p.getUuid());

        if (s.lastSeen == 0 && s.virusLifetime == 0) {
            p.giveItemStack(new ItemStack(FANG));
            p.sendMessage(Text.literal("Project Z-Idle: clic derecho con el Colmillo Infectado para infectar. /zidle ayuda").formatted(Formatting.GREEN));
        } else if (s.lastSeen > 0) {
            double seconds = (System.currentTimeMillis() - s.lastSeen) / 1000.0;
            double gained = EconomyManager.applyOffline(s, seconds);
            if (gained > 0) {
                p.sendMessage(Text.literal("Mientras no estabas (" + (long) Math.min(seconds, Constants.OFFLINE_MAX_SECONDS)
                    + " s) la horda produjo " + Formatters.format(gained) + " virus.").formatted(Formatting.GOLD));
            }
        }
        s.lastSeen = System.currentTimeMillis();
        data.markDirty();
    }
}
