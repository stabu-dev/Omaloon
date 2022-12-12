package ol.world.blocks.crafting;

import arc.Core;
import arc.func.*;
import arc.graphics.*;
import arc.math.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.util.*;
import arc.struct.*;
import arc.util.io.*;
import arc.scene.ui.layout.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.meta.*;
import mindustry.world.consumers.*;
import ol.ui.*;
import ol.utils.*;
import ol.world.blocks.pressure.PressureCrafter;
import ol.world.meta.*;

/**
 * Original code from Monolith
 * Author: @uujuju
 */

public class MultiCrafter extends GenericCrafter {
    public Seq<Craft> crafts = new Seq<>();

    public MultiCrafter(String name) {
        super(name);

        configurable = hasItems = solid = update = sync = destructible = copyConfig = true;

        flags = EnumSet.of(BlockFlag.factory);

        config(Integer.class, (MultiCrafterBuild build, Integer i) -> build.currentPlan = i);

        consume(new ConsumeItemDynamic((MultiCrafterBuild e) -> {
            return e.currentPlan != -1 ? e.getCraft().consumeItems : ItemStack.empty;
        }));

        consume(new ConsumeLiquidDynamic(e -> {
            return ((MultiCrafterBuild) e).getLiquidCons();
        }));

        consume(new ConsumePowerDynamic(e -> {
            return ((MultiCrafterBuild) e).getPowerCons();
        }));
    }

    @Override
    public void setStats() {
        super.setStats();

        stats.remove(Stat.basePowerGeneration);
        stats.remove(Stat.productionTime);
        stats.add(OlStat.requirements, crafts(crafts));
    }

    public static StatValue crafts(Seq<MultiCrafter.Craft> crafts) {
        return stat -> {
            stat.row();
            stat.table(t -> {
                for(MultiCrafter.Craft craft : crafts) {
                    t.table(((TextureRegionDrawable) Tex.whiteui).tint(Pal.darkestGray), table -> {
                        table.table(Tex.underline, plan -> {
                            plan.table(input -> {
                                input.add(Stat.input.localized() + ": ");

                                for(ItemStack stack : craft.consumeItems) input.add(new Table() {{
                                    add(new ItemImage(stack.item.uiIcon, stack.amount));

                                    add(Strings.autoFixed(stack.amount / (craft.craftTime / 60f), 2) + StatUnit.perSecond.localized())
                                            .padLeft(2)
                                            .padRight(5)
                                            .color(Color.lightGray)
                                            .style(Styles.outlineLabel);
                                }}).pad(5f);

                                for(LiquidStack stack : craft.consumeLiquids) input.add(new Table() {{
                                    add(new LiquidImage(stack.liquid.uiIcon, stack.amount * 60f));

                                    add(StatUnit.perSecond.localized())
                                            .padLeft(2)
                                            .padRight(5)
                                            .color(Color.lightGray)
                                            .style(Styles.outlineLabel);
                                }}).pad(8f);

                                if(craft.consumePower > 0) {
                                    input.image(Icon.power).color(Pal.power);
                                    input.add("-" + (craft.consumePower * 60f));
                                }
                            }).left().row();

                            plan.table(output -> {
                                output.add(Stat.output.localized() + ":");

                                for(ItemStack stack : craft.outputItems) output.add(new Table() {{
                                    add(new ItemImage(stack.item.uiIcon, stack.amount));

                                    add(Strings.autoFixed(stack.amount / (craft.craftTime / 60f), 2) + StatUnit.perSecond.localized())
                                            .padLeft(2)
                                            .padRight(5)
                                            .color(Color.lightGray)
                                            .style(Styles.outlineLabel);
                                }}).pad(5f);

                                for(LiquidStack stack : craft.outputLiquids) output.add(new Table() {{
                                    add(new LiquidImage(stack.liquid.uiIcon, stack.amount * 60f));

                                    add(StatUnit.perSecond.localized())
                                            .padLeft(2)
                                            .padRight(5)
                                            .color(Color.lightGray)
                                            .style(Styles.outlineLabel);
                                }}).pad(8f);
                            }).left();
                        }).growX().pad(10f).row();

                        table.table(stats -> {
                            stats.add(Core.bundle.get("stat.productiontime") + ": ").color(Color.gray);
                            stats.add(StatValues.fixValue(craft.craftTime/60) + " " + StatUnit.seconds.localized());
                        }).pad(8f);
                    }).growX().pad(10f).row();
                }
            });
        };
    }

    @Override
    public void setBars() {
        super.setBars();

        removeBar("liquid");
        removeBar("items");

        crafts.each(this::addCraftBars);
    }

    public void addCraftBars(Craft craft) {
        for(LiquidStack stack : craft.consumeLiquids) {
            addLiquidBar(stack.liquid);
        }

        for(LiquidStack stack : craft.outputLiquids) {
            addLiquidBar(stack.liquid);
        };
    }

    public static class Craft {
        public ItemStack[]
                consumeItems = ItemStack.empty,
                outputItems = ItemStack.empty;

        public LiquidStack[]
                consumeLiquids = LiquidStack.empty,
                outputLiquids = LiquidStack.empty;

        public Effect
                craftEffect = Fx.none,
                updateEffect = Fx.none;

        public float
                consumePower = 0f,
                updateEffectChance = 0f,
                warmupSpeed = 0f,
                craftTime = 0f;
    }

    public class MultiCrafterBuild extends GenericCrafterBuild {
        public int currentPlan = -1;
        public float progress, totalProgress, warmup;

        public @Nullable Craft getCraft() {
            return currentPlan == -1 ? null : crafts.get(currentPlan);
        }

        public float getPowerCons() {
            return getCraft() != null ? getCraft().consumePower : 0f;
        }

        public LiquidStack[] getLiquidCons() {
            return getCraft() != null ? getCraft().consumeLiquids : LiquidStack.empty;
        }

        public void changeCraft(Craft craft) {
            currentPlan = crafts.indexOf(craft);
        }

        @Override
        public float warmup() {
            return warmup;
        }

        @Override
        public float progress() {
            return progress;
        }

        @Override
        public float totalProgress() {
            return totalProgress;
        }

        @Override
        public Integer config() {
            return currentPlan;
        }

        @Override
        public void buildConfiguration(Table table) {
            Cons<Craft> consumer = this::changeCraft;
            Prov<Craft> provider = this::getCraft;

            Table cont = new Table();
            for(Craft craft : crafts) {
                Button button = cont.button(b -> {
                    if (craft.outputItems != null) {
                        for (ItemStack stack : craft.outputItems) {
                            b.add(new Image(stack.item.uiIcon));
                        }
                    }
                }, Styles.clearTogglei, () -> {})
                        .left()
                        .growX()
                        .get();

                button.sizeBy(500);
                button.changed(() -> consumer.get(button.isChecked() ? craft : null));
                button.update(() -> button.setChecked(provider.get() == craft));
            }

            table.add(cont);
        }

        @Override
        public boolean acceptItem(Building source, Item item) {
            if(getCraft() == null) {
                return false;
            }

            return Structs.contains(getCraft().consumeItems, stack -> {
                return stack.item == item && items.get(item) < stack.amount * itemCapacity;
            });
        }

        @Override
        public boolean acceptLiquid(Building source, Liquid liquid) {
            if (getCraft() == null) {
                return false;
            }

            return liquids.get(liquid) < block.liquidCapacity && Structs.contains(getCraft().consumeLiquids, stack -> {
                return stack.liquid == liquid;
            });
        }

        @Override
        public boolean shouldConsume() {
            if(getCraft() == null) {
                return false;
            }

            for(ItemStack stack : getCraft().outputItems) {
                if(items.get(stack.item) >= getMaximumAccepted(stack.item)) {
                    return false;
                }
            }

            for(LiquidStack stack : getCraft().outputLiquids) {
                if(liquids.get(stack.liquid) >= block.liquidCapacity) {
                    return false;
                }
            }

            return enabled;
        }

        @Override
        public boolean shouldAmbientSound(){
            if(getCraft() == null) {
                return false;
            }

            return efficiency > 0;
        }

        @Override
        public void updateTile() {
            if(efficiency > 0 && getCraft() != null) {
                warmup = Mathf.approachDelta(warmup, 1f, getCraft().warmupSpeed);
                progress += getProgressIncrease(getCraft().craftTime) * warmup;
                totalProgress += edelta() * warmup;

                if(wasVisible && Mathf.chance(getCraft().updateEffectChance)) {
                    getCraft().updateEffect.at(x + Mathf.range(size * 4f), y + Mathf.range(size * 4f));
                }

                for(LiquidStack output : getCraft().outputLiquids) {
                    handleLiquid(this, output.liquid, Math.min(output.amount * getProgressIncrease(1f), liquidCapacity - liquids.get(output.liquid)));
                }

                if(progress >= 1f) {
                    progress %= 1f;
                    consume();

                    if(wasVisible) {
                        getCraft().craftEffect.at(x, y);
                    }

                    if(getCraft() != null) {
                        for(ItemStack out : getCraft().outputItems) {
                            for(int i = 0; i < out.amount; i++) {
                                offload(out.item);
                            }
                        }
                    }
                }
            } else {
                warmup = Mathf.approachDelta(warmup, 0f, 0.019f);
            }

            dumpOutputs();
        }

        public void dumpOutputs() {
            if(getCraft() != null && timer(timerDump, dumpTime / timeScale)) {
                if(getCraft().outputItems == null) {
                    return;
                }

                for(ItemStack output : getCraft().outputItems){
                    dump(output.item);
                }

                if(getCraft().outputLiquids == null) {
                    return;
                }

                for(LiquidStack output : getCraft().outputLiquids){
                    dumpLiquid(output.liquid);
                }
            }
        }

        @Override
        public void write(Writes w) {
            super.write(w);

            w.f(warmup);
            w.f(progress);
            w.f(totalProgress);
            w.i(currentPlan);
        }

        @Override
        public void read(Reads r, byte revision) {
            super.read(r, revision);

            warmup = r.f();
            progress = r.f();
            totalProgress = r.f();
            currentPlan = r.i();
        }
    }
}