package ol.world.blocks.crafting.multicraft;

import arc.func.Func;
import arc.scene.ui.layout.Table;
import mindustry.gen.Building;
import mindustry.type.LiquidStack;
import mindustry.ui.ReqImage;
import mindustry.world.Block;
import mindustry.world.consumers.Consume;

public class ConsumeFluidDynamic extends Consume {
    public final Func<Building, Pair> fluids;

    public static class Pair {
        public LiquidStack[] fluids;
        public float displayMultiplier;

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Pair) {
                Pair b = (Pair) obj;
                return this.fluids == b.fluids &&
                    this.displayMultiplier == b.displayMultiplier;
            }
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends Building> ConsumeFluidDynamic(Func<T, Pair> fluids) {
        this.fluids = (Func<Building, Pair>) fluids;
    }

    @Override
    public void apply(Block block) {
        block.hasLiquids = true;
    }

    @Override
    public void update(Building build) {
        Pair pair = fluids.get(build);
        InventoryH.remove(build.liquids, pair.fluids, build.edelta());
    }

    @Override
    public void build(Building build, Table table) {
        final Pair[] current = {fluids.get(build)};

        table.table(cont -> {
            table.update(() -> {
                Pair pair = fluids.get(build);
                if (!current[0].equals(pair)) {
                    rebuild(build, cont);
                    current[0] = pair;
                }
            });

            rebuild(build, cont);
        });
    }

    private void rebuild(Building tile, Table table) {
        table.clear();
        int i = 0;

        Pair pair = fluids.get(tile);
        float displayMultiplier = pair.displayMultiplier;
        for (LiquidStack stack : pair.fluids) {
            table.add(new ReqImage(new FluidImage(stack.liquid.uiIcon, stack.amount, displayMultiplier),
                () -> tile.items != null && tile.liquids.get(stack.liquid) >= stack.amount)).padRight(8).left();
            if (++i % 4 == 0) table.row();
        }
    }

    @Override
    public float efficiency(Building build) {
        Pair pair = fluids.get(build);
        return build.consumeTriggerValid() || InventoryH.has(build.liquids, pair.fluids) ? 1f : 0f;
    }
}
