package ol.pressure;

import mindustry.gen.Building;
import ol.world.blocks.pressure.IPressureBuild;

public class PUtil {
    public static final float ANY_TIER_VALUE = -666.66613f;
    public static final float DEFAULT_ODM = 1 / 6f; //ODM - Overload Damage Multiplayer

    public static final float
            PRESSURE_TIER_BASIC = 1,
            PRESSURE_TIER_ADVANCED = 2,
            PRESSURE_TIER_ARMORED = 3;

    public static boolean isPressureBlock(Building building) {
        return building instanceof IPressureBuild b && b.isPressureBlock();
    }

    public static boolean isPressureBlock(IPressureBuild other) {
        return other != null && other.isPressureBlock();
    }

    public static boolean compareTier(float a, float b) {
        return a == ANY_TIER_VALUE || b == ANY_TIER_VALUE || a == b;
    }
}