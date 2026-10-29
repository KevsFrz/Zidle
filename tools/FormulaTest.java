import com.zidle.core.PlayerState;
import com.zidle.managers.*;
import com.zidle.utils.Formatters;

public class FormulaTest {
    static int fails = 0;
    static void check(String name, boolean ok) { System.out.println((ok ? "OK   " : "FAIL ") + name); if (!ok) fails++; }

    public static void main(String[] a) {
        // bulk cost == suma de costos individuales
        double sum = 0;
        for (int i = 0; i < 10; i++) sum += EconomyManager.cost(0, 5 + i);
        check("bulkCost(k=10) = suma de costos", Math.abs(sum - EconomyManager.bulkCost(0, 5, 10)) < 1e-6);

        // maxAffordable coherente
        int k = EconomyManager.maxAffordable(1, 3, 12345);
        check("maxAffordable alcanza", EconomyManager.bulkCost(1, 3, k) <= 12345);
        check("maxAffordable es máximo", EconomyManager.bulkCost(1, 3, k + 1) > 12345);

        // compra
        PlayerState s = new PlayerState();
        s.virus = 15;
        check("compra 1 alcantarilla", EconomyManager.buy(s, 0, 1) == 1 && s.owned[0] == 1 && s.virus == 0);
        check("compra sin fondos falla", EconomyManager.buy(s, 0, 1) == 0);

        // vps y vacuna
        s.owned[0] = 10;
        check("vps = 10*0.5", Math.abs(EconomyManager.vps(s) - 5.0) < 1e-9);
        s.virusRun = 200_000;
        s.vaccine = 100;
        check("debuff 90% con vacuna al 100%", Math.abs(EconomyManager.vps(s) - 0.5) < 1e-9);
        s.virus = 1000;
        check("sabotaje baja vacuna y gasta virus", VaccineManager.sabotage(s) && s.vaccine == 70 && s.virus == 750);

        // prestigio
        PlayerState p = new PlayerState();
        p.virusRun = 7.8e9;
        check("prestigio disponible a 7.8e9", PrestigeManager.canPrestige(p));
        check("primer prestigio da 2 ADN", PrestigeManager.adnGain(p) == 2);
        p.latente = 2;
        PrestigeManager.prestige(p);
        check("tras prestigio: adn=2, run=0, latente da 10 alcantarillas", p.adn == 2 && p.virusRun == 0 && p.owned[0] == 10);

        // offline
        PlayerState o = new PlayerState();
        o.owned[0] = 100; // 50 vps
        check("offline <60s no da nada", EconomyManager.applyOffline(o, 30) == 0);
        double g = EconomyManager.applyOffline(o, 1_000_000);
        check("offline limitado a 12h al 50%", Math.abs(g - 50 * 43200 * 0.5) < 1e-6);

        check("formato", Formatters.format(1_500_000).equals("1.50M") && Formatters.format(999).equals("999.0"));
        System.out.println(fails == 0 ? "TODO OK" : fails + " FALLOS");
        System.exit(fails == 0 ? 0 : 1);
    }
}
