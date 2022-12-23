package ol.utils.pressure;

import mindustry.gen.*;

import ol.world.blocks.pressure.*;

public class PressureAPI {
    public static final int NULL_TIER = -1;

    public static boolean netAble(Building ba, Building bb) {
        return PressureAPI.netAble(ba, bb, false);
    }

    public static boolean netAble(Building ba, Building bb, boolean jun) {
        if(ba instanceof PressureAble<?> pa && bb instanceof PressureAble<?> pb) {
            return pa.inNet(bb, pb, jun) && pb.inNet(ba, pa, jun);
        }

        //if check net impossible
        return false;
    }

    public static boolean overload(PressureAble<?> pressureAble) {
        if(pressureAble == null) {
            return false; //null always false
        }

        //if block is overloaded and can explode
        return pressureAble.pressure() > pressureAble.maxPressure() && pressureAble.canExplode();
    }

    public static boolean tierAble(PressureBlock block, int tier) {
        if(block == null) {
            return false;
        }

        if(block.tier == tier) {
            return true;
        }

        return block.tier == PressureAPI.NULL_TIER || tier == PressureAPI.NULL_TIER;
    }

    public static boolean tierAble(PressureAble<?> pa, int tier) {
        if(tier == PressureAPI.NULL_TIER) {
            return true;
        }

        if(pa == null) {
            return false;
        }

        int tier_pa = pa.tier();
        return tier_pa == tier || tier_pa == PressureAPI.NULL_TIER;
    }

    public static boolean tierAble(PressureAble<?> pa, PressureAble<?> pb) {
        if(pa == null || pb == null) {
            return false; //null can be tiered
        }

        int tier_pa = pa.tier();
        int tier_pb = pb.tier();

        if(tier_pa == PressureAPI.NULL_TIER || tier_pb == PressureAPI.NULL_TIER) {
            return true; //if 1 of blocks have null tier when it can be connected
        }

        //default tier check
        return tier_pa == tier_pb;
    }
}