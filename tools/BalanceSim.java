import com.zidle.config.Constants;
import com.zidle.config.Producers;
import com.zidle.core.PlayerState;
import com.zidle.managers.*;
import com.zidle.utils.Formatters;

/** Simula 24h con un jugador "voraz": compra lo más barato rentable, mejoras y clics ocasionales. */
public class BalanceSim {
    public static void main(String[] a) {
        PlayerState s = new PlayerState();
        int ticksPerHour = 36000;
        double[] milestones = {0.1, 1, 3, 6, 12, 24};
        int m = 0;
        for (int tick = 1; tick <= 24 * ticksPerHour; tick++) {
            EconomyManager.tick(s, Constants.DT);
            if (tick % 10 == 0) { // cada segundo: 3 clics + decisiones de compra
                for (int i = 0; i < 3; i++) EconomyManager.click(s);
                for (var u : com.zidle.config.Upgrades.ALL) {
                    if (EconomyManager.upgradeVisible(s, u) && s.virus >= u.cost()) EconomyManager.buyUpgrade(s, u.id());
                }
                for (int i = Producers.COUNT - 1; i >= 0; i--) {
                    if (EconomyManager.buy(s, i, -1) > 0) break;
                }
                if (s.vaccineComplete()) VaccineManager.sabotage(s);
                if (PrestigeManager.canPrestige(s)) {
                    System.out.printf("  >> prestigio en t=%.2fh, ADN +%d%n", tick / (double) ticksPerHour, PrestigeManager.adnGain(s));
                    PrestigeManager.prestige(s);
                }
            }
            if (m < milestones.length && tick >= milestones[m] * ticksPerHour) {
                System.out.printf("t=%5.1fh virus=%s vps=%s vacuna=%.1f%% infeccion=%.2f%% adn=%.0f%n", milestones[m],
                    Formatters.format(s.virus), Formatters.format(EconomyManager.vps(s)), s.vaccine, s.infectedPercent(), s.adn);
                m++;
            }
        }
    }
}
