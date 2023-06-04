package ol.pressure.netting;

import arc.struct.Seq;
import mindustry.gen.Building;
import ol.pressure.block.IHasCustomConnectionCondition;
import ol.pressure.block.IWasNetWire;

/** Utils */
public class Netting {
    /** Перевіряє чи може будівля бути в системі тиску, за умовчанням треба мати спеціальну анотацію */
    public static boolean inNet(Building building) {
        return building != null && building.getClass().isAnnotationPresent(IncludeToTheNet.class) &&
                (!(building instanceof IHasCustomConnectionCondition has) || has.canConnect());
    }

    /**
     * Аналогічно IWasNetWire.getChild(), але завжди буде NotNull
     * @param wasNetWire IWasNetWire.getChild()
     * @return IWasNetWire.getChild()
     */
    public static Seq<Building> getConnections(IWasNetWire wasNetWire) {
        var ch = wasNetWire == null ? null : wasNetWire.getChild();
        return Seq.with(ch == null ? new Building[0] : ch);
    }

    /**
     * Створює систему тиску, додавати в масив всі будівлі системи тиску
     * @param raw масив з якого все починається
     * @param start початкова будівля
     */
    public static void getNet(Seq<Building> raw, Building start) {
        if(inNet(start) && !raw.contains(start)) {
            raw.add(start);
        }
        if(start instanceof IWasNetWire wire) {
            getConnections(wire).forEach((build) -> {
                if(inNet(build) && !raw.contains(build)) {
                    getNet(raw, build);
                }
            });
        }
    }

    /**
     * Створює систему тиску будівлі
     * @param build будівля
     * @return система тиску
     */
    public static PressureNet net(Building build) {
        Seq<Building> raw = new Seq<>();
        getNet(raw, build);
        return new PressureNet(raw);
    }
}
