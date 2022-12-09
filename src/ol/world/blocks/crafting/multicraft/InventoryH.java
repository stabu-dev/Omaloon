package ol.world.blocks.crafting.multicraft;

import mindustry.type.LiquidStack;
import mindustry.world.modules.LiquidModule;

public class InventoryH {
    public static boolean has(LiquidModule fluids, LiquidStack[] reqs) {
        for (LiquidStack req : reqs) {
            if (fluids.get(req.liquid) < req.amount)
                return false;
        }
        return true;
    }

    public static void remove(LiquidModule fluids, LiquidStack[] reqs, float multiplier) {
        for (LiquidStack req : reqs) {
            fluids.remove(req.liquid, req.amount * multiplier);
        }
    }
}
