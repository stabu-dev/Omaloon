package ol.utils;

import arc.func.Func;
import arc.scene.ui.layout.Table;
import mindustry.gen.Building;
import mindustry.type.Liquid;
import mindustry.type.LiquidStack;
import mindustry.ui.ItemImage;
import mindustry.ui.ReqImage;
import mindustry.world.Block;
import mindustry.world.consumers.*;
import mindustry.world.modules.LiquidModule;

/** from mindustry by Anuken but edited for liquids */
public class ConsumeLiquidDynamic extends Consume {
    public final Func<Building, LiquidStack[]> liquids;

    @SuppressWarnings("unchecked")
    public <T extends Building> ConsumeLiquidDynamic(Func<T, LiquidStack[]> liquids) {
        this.liquids = (Func<Building, LiquidStack[]>) liquids;
    }

    @Override
    public void apply(Block block){
        block.hasLiquids = true;
    }

    @Override
    public void build(Building build, Table table) {
        LiquidStack[][] current = {
                liquids.get(build)
        };

        table.table(cont -> {
            table.update(() -> {
                if(current[0] != liquids.get(build)) {
                    rebuild(build, cont);

                    current[0] = liquids.get(build);
                }
            });

            rebuild(build, cont);
        });
    }

    public boolean hasx(Liquid liquid, float amount, LiquidModule m) {
        return m.get(liquid) >= amount;
    }

    public boolean hasx(LiquidStack[] s, LiquidModule m) {
        for(LiquidStack st : s) {
            if(!hasx(st.liquid, st.amount, m)) {
                return false;
            }
        }

        return true;
    }

    private void rebuild(Building tile, Table table) {
        table.clear();
        int i = 0;

        for(LiquidStack stack : liquids.get(tile)) {
            table.add(new ReqImage(new ItemImage(stack.liquid.uiIcon, (int) stack.amount), () -> {
                return tile.liquids != null && hasx(stack.liquid, stack.amount, tile.liquids);
            })).padRight(8).left();

            if(++i % 4 == 0) {
                table.row();
            }
        }
    }

    @Override
    public void trigger(Building build) {
        for(LiquidStack stack : liquids.get(build)) {
            build.liquids.remove(stack.liquid, stack.amount);
        }
    }

    @Override
    public float efficiency(Building build) {
        return build.consumeTriggerValid() || hasx(liquids.get(build), build.liquids) ? 1f : 0f;
    }
}
