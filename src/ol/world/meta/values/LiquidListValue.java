package ol.world.meta.values;

import arc.scene.ui.layout.Table;
import mindustry.type.LiquidStack;
import mindustry.world.meta.StatValue;

public class LiquidListValue implements StatValue {
    private final LiquidStack[] stacks;
    private final boolean displayName;

    public LiquidListValue(LiquidStack... stacks) {
        this(true, stacks);
    }

    public LiquidListValue(boolean displayName, LiquidStack... stacks) {
        this.stacks = stacks;
        this.displayName = displayName;
    }

    public void display(Table table) {
        for (LiquidStack stack : stacks) {
            table.add(new OlLiquidDisplay(stack.liquid, stack.amount, displayName)).padRight(5.0F);
        }

    }
}
