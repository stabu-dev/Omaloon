package ol.world.blocks.crafting.multicraft;

import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.scene.ui.Image;
import arc.scene.ui.layout.Stack;
import arc.scene.ui.layout.Table;
import mindustry.core.UI;
import mindustry.type.LiquidStack;
import mindustry.ui.Styles;

public class FluidImage extends Stack {

    public FluidImage(TextureRegion region, float amount) {
        this(region, amount, 1f);
    }

    public FluidImage(TextureRegion region, float amount, float multiplier) {

        add(new Table(o -> {
            o.left();
            o.add(new Image(region)).size(32f);
        }));

        if (amount != 0) {
            add(new Table(t -> {
                t.left().bottom();
                t.add(amount >= 1000 ?
                                UI.formatAmount((long) (amount * multiplier)) :
                        Mathf.round(amount * multiplier) + "").fontScale(0.9f)
                        .style(Styles.outlineLabel);
                t.pack();
            }));
        }
    }

    public FluidImage(LiquidStack stack) {
        this(stack.liquid.uiIcon, stack.amount);
    }

    public FluidImage(LiquidStack stack, float multiplier) {
        this(stack.liquid.uiIcon, stack.amount, multiplier);
    }
}
