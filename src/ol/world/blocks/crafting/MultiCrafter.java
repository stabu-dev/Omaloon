package ol.world.blocks.crafting;

import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.util.*;
import arc.struct.*;
import arc.util.io.*;
import arc.scene.ui.layout.*;

import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.world.draw.*;
import mindustry.world.meta.*;
import mindustry.world.consumers.*;

import ol.ui.*;
import ol.utils.*;
import ol.world.meta.*;

import static arc.Core.*;

import static ol.graphics.OlPal.*;

/**
 * Original code from Monolith
 * Author: @uujuju
 */

public class MultiCrafter extends PressureCrafter {
    public Seq<Craft> crafts = new Seq<>();

    public MultiCrafter(String name) {
        super(name);

        configurable = hasItems = solid = update = sync = destructible = copyConfig = true;

        flags = EnumSet.of(BlockFlag.factory);

        config(Integer.class, (MultiCrafterBuild build, Integer i) ->
                build.currentPlan = i
        );

        consume(new ConsumeItemDynamic((MultiCrafterBuild e) ->
                e.currentPlan != -1 ? e.getCraft().consumeItems : ItemStack.empty)
        );
        consume(new ConsumeLiquidDynamic(e ->
                ((MultiCrafterBuild) e).getLiquidCons())
        );
        consume(new ConsumePowerDynamic(e ->
                ((MultiCrafterBuild) e).getPowerCons())
        );
    }

    public class Craft {
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
                craftTime = 0f,

                downScl = 0.25f,
                pressureConsume = 0,
                pressureProduce = 0,
                maxPressure2 = -1;

        public DrawBlock
                drawer = new DrawDefault();

        public float getMaxPressure() {
            if(changesPressureCapacity) {
                return maxPressure2 == -1 ? Math.max(pressureConsume, pressureProduce) * 2 : maxPressure2;
            }

            return maxPressure;
        }

        public boolean
                changesPressureCapacity = false,
                downPressure = false;

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
                                    input.add("-" + (craft.consumePower * 60f)
                                    ).pad(8);
                                }

                                if(craft.pressureConsume > 0){
                                    input.add(
                                            bundle.get("stat.pressure") +
                                            "[#bfbfbf] " + (int)(craft.pressureConsume) +
                                            " " + bundle.get("unit.pressure")
                                    );
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
                            stats.add(bundle.get("stat.productiontime") + ": ").color(Color.gray);
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

        removeBar("pressure");
        removeBar("liquid");
        removeBar("items");

        addBar("pressure", (PressureCrafterBuild b) -> {
            float pressure = b.pressure / b.maxPressure();
            return new Bar(
                    () -> bundle.format("bar.pressureEfficient", (int)(b.pressure), (int)(b.efficenty() * 100 + 0.0001f)),
                    () -> mixcol(oLPressureMin, oLPressure, pressure),
                    () -> pressure
            );
        });

        crafts.each(this::addCraftBars);
    }

    public void addCraftBars(Craft craft) {
        for(LiquidStack stack : craft.consumeLiquids) {
            addLiquidBar(stack.liquid);
        }

        for(LiquidStack stack : craft.outputLiquids) {
            addLiquidBar(stack.liquid);
        }
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list) {
        crafts.get(0).drawer.drawPlan(this, plan, list);
    }

    @Override
    public TextureRegion[] icons() {
        return crafts.get(0).drawer.finalIcons(this);
    }

    @Override
    public void getRegionsToOutline(Seq<TextureRegion> out) {
        crafts.get(0).drawer.getRegionsToOutline(this, out);
    }

    @Override
    public void load() {
        super.load();
        for (Craft craft : crafts) craft.drawer.load(this);
    }

    public class MultiCrafterBuild extends PressureCrafterBuild {
        public int currentPlan = -1;
        public float progress, totalProgress, warmup;

        @Override
        public float pressureConsume() {
            return getCraft() != null ? getCraft().pressureConsume : 0f;
        }

        @Override
        public float pressureProduce() {
            return getCraft() != null ? getCraft().pressureProduce : 0f;
        }

        @Override
        public float maxPressure() {
            return getCraft() != null ? getCraft().getMaxPressure() : maxPressure;
        }

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

            return Structs.contains(getCraft().consumeItems, stack ->
                    stack.item == item && items.get(item) < stack.amount * itemCapacity
            );
        }

        @Override
        public boolean acceptLiquid(Building source, Liquid liquid) {
            if (getCraft() == null) {
                return false;
            }

            return liquids.get(liquid) <
                    block.liquidCapacity && Structs.contains(getCraft()
                    .consumeLiquids, stack ->
                    stack.liquid == liquid
            );
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
            efficiency *= efficenty();
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

                    label60: {
                        if(pressureConsume() > 0 && efficenty() == 0) {
                            break label60;
                        }

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
                }
            } else {
                warmup = Mathf.approachDelta(warmup, 0f, 0.019f);
            }

            dumpOutputs();

            if(canConsume() && effectx < 100) {
                effectx++;
                if(effectx > 100) {
                    effectx = 100;
                }
            } else {
                if(!canConsume() && effectx > 0) {
                    effectx--;
                    if(effectx < 0) {
                        effectx = 0;
                    }
                }
            }

            effect = effectx * efficenty();
        }

        @Override
        public boolean downPressure() {
            return getCraft() != null && getCraft().downPressure;
        }

        @Override
        public float downPercent() {
            return getCraft() != null ? getCraft().downScl : 0f;
        }

        public void dumpOutputs() {
            if(getCraft() != null && timer(timerDump, dumpTime / timeScale)) {
                if(getCraft().outputItems == null) {
                    return;
                }

                for(ItemStack output : getCraft().outputItems) {
                    dump(output.item);
                }

                if(getCraft().outputLiquids == null) {
                    return;
                }

                for(LiquidStack output : getCraft().outputLiquids) {
                    dumpLiquid(output.liquid);
                }
            }
        }

        @Override
        public void draw() {
            if (getCraft() != null) {
                getCraft().drawer.draw(this);
            } else {
                crafts.get(0).drawer.draw(this);
            }
        }
        @Override
        public void drawLight() {
            if (getCraft() != null) {
                getCraft().drawer.drawLight(this);
            } else {
                crafts.get(0).drawer.drawLight(this);
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