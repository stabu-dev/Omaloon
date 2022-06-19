package ol.ui;

import arc.graphics.g2d.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import mindustry.core.*;
import mindustry.type.*;
import mindustry.ui.Styles;

public class LiquidImage extends Stack{

    public LiquidImage(TextureRegion region, int amount){

        add(new Table(o -> {
            o.left();
            o.add(new Image(region)).size(32f);
        }));

        if(amount != 0){
            add(new Table(t -> {
                t.left().bottom();
                t.add(amount >= 1000 ? UI.formatAmount(amount) : amount + "").style(Styles.outlineLabel);
                t.pack();
            }));
        }
    }

    public LiquidImage(LiquidStack stack){
        this(stack.liquid.uiIcon, (int) stack.amount);
    }

    public LiquidImage(PayloadStack stack){
        this(stack.item.uiIcon, stack.amount);
    }
}
