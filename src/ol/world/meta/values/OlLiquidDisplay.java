package ol.world.meta.values;

import arc.graphics.Color;
import arc.scene.ui.Image;
import arc.scene.ui.layout.Stack;
import arc.scene.ui.layout.Table;
import arc.util.Strings;
import mindustry.type.Liquid;
import mindustry.ui.Styles;
import mindustry.world.meta.StatUnit;

public class OlLiquidDisplay extends Table{
    public final Liquid liquid;
    public final float amount;
    public final boolean perSecond;

    public OlLiquidDisplay(final Liquid liquid, final float amount, boolean showName) {
    this(liquid,amount,showName,false);
    }
    public OlLiquidDisplay(final Liquid liquid, final float amount, boolean showName, boolean perSecond) {
        this.liquid = liquid;
        this.amount = amount;
        this.perSecond = perSecond;
        this.add(new Stack() {
            {
                this.add(new Image(liquid.uiIcon));
                if (amount != 0.0F) {
                    Table t = (new Table()).left().bottom();
                    t.add(Strings.autoFixed(amount, 1)).style(Styles.outlineLabel);
                    this.add(t);
                }

            }
        }).size(32.0F).padRight((float)(3 + (amount != 0.0F && Strings.autoFixed(amount, 1).length() > 2 ? 8 : 0)));
        if (perSecond) {
            this.add(StatUnit.perSecond.localized()).padLeft(2.0F).padRight(5.0F).color(Color.lightGray).style(Styles.outlineLabel);
        }

       if(showName) this.add(liquid.localizedName);
    }
}
