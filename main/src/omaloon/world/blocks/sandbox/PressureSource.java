package omaloon.world.blocks.sandbox;

import arc.*;
import arc.graphics.g2d.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.ImageButton.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.*;
import mindustry.entities.units.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.blocks.liquid.*;
import mindustry.world.meta.*;
import omaloon.annotations.Annotations.*;
import omaloon.content.*;
import omaloon.world.graph.*;
import omaloon.world.interfaces.*;
import omaloon.world.meta.*;
import omaloon.world.modules.*;

public class PressureSource extends Block{
    public PressureConfig pressureConfig = new PressureConfig();

    public @Load(value = "@-bottom", fallBack = "omaloon-liquid-bottom") TextureRegion bottomRegion;

    public PressureSource(String name){
        super(name);
        solid = true;
        destructible = true;
        update = true;
        configurable = true;
        saveConfig = copyConfig = true;
        category = Category.liquid;
        buildVisibility = BuildVisibility.sandboxOnly;

        config(SourceEntry.class, (PressureLiquidSourceBuild build, SourceEntry entry) -> {
            build.liquid = entry.fluid == null ? -1 : entry.fluid.id;
            build.targetAmount = entry.amount;

            for(int i = -1; i < Vars.content.liquids().size; i++){
                build.pressure.setAmount(i, 0);
                build.pressure.setPressure(i, 0);
            }
        });
    }

    @Override
    public void drawPlanConfig(BuildPlan plan, Eachable<BuildPlan> list){
        Draw.rect(bottomRegion, plan.drawx(), plan.drawy());
        if(plan.config instanceof SourceEntry e && e.fluid != null) LiquidBlock.drawTiledFrames(size, plan.drawx(), plan.drawy(), 0f, e.fluid, 1f);
        Draw.rect(region, plan.drawx(), plan.drawy());
    }

    @Override
    protected TextureRegion[] icons(){
        return new TextureRegion[]{
            Core.atlas.find(name + "-bottom", "omaloon-liquid-bottom"),
            region
        };
    }

    @Override
    public void init(){
        pressureConfig.hasPressure = pressureConfig.acceptsPressure = pressureConfig.outputsPressure = true;

        super.init();

        if (hasLiquids) hasLiquids = false;

        pressureConfig.group = null;
    }

    @Override
    public void setBars(){
        super.setBars();
        pressureConfig.addBars(this);
    }

    @Override
    public void setStats(){
        super.setStats();
        pressureConfig.addStats(this, stats);

        stats.remove(OlStats.minPressure);
        stats.remove(OlStats.maxPressure);
    }

    public class PressureLiquidSourceBuild extends Building implements HasPressure{
        public PressureModule pressure;

        public int liquid = -1;
        public float targetAmount;

        @Override
        public void buildConfiguration(Table cont){
            cont.table(Styles.black6, table -> {
                table.pane(Styles.smallPane, liquids -> Vars.content.liquids().each(liquid -> {
                    Button button = liquids.button(
                    new TextureRegionDrawable(liquid.uiIcon),
                    new ImageButtonStyle(){{
                        over = Styles.flatOver;
                        down = checked = Tex.flatDownBase;
                    }}, () -> {
                        if(this.liquid != liquid.id){
                            configure(new SourceEntry(){{
                                fluid = liquid;
                                amount = targetAmount;
                            }});
                        }else{
                            configure(new SourceEntry(){{
                                fluid = null;
                                amount = targetAmount;
                            }});
                        }
                    }
                    ).tooltip(liquid.localizedName).size(40f).get();
                    button.update(() -> button.setChecked(liquid.id == this.liquid));
                    if((Vars.content.liquids().indexOf(liquid) + 1) % 4 == 0) liquids.row();
                })).maxHeight(160f).row();
                table.add("@filter.option.amount").padTop(5f).padBottom(5f).row();
                table.field(
                "" + targetAmount,
                (field, c) -> Character.isDigit(c) || ((!field.getText().contains(".")) && c == '.') || (field.getText().isEmpty() && c == '-'),
                s -> configure(new SourceEntry(){{
                    fluid = Vars.content.liquid(liquid);
                    amount = Strings.parseFloat(s, 0f);
                }})
                );
            }).margin(5f);
        }

        @Override
        public SourceEntry config(){
            return new SourceEntry(){{
                fluid = Vars.content.liquid(liquid);
                amount = targetAmount;
            }};
        }

        @Override
        public Building create(Block block, Team team){
            super.create(block, team);
            if (pressureConfig().hasPressure) {
                pressure = new PressureModule();
                pressureGraph().addRaw(this);
            }
            return this;
        }

        @Override public boolean doPressureDamage(){
            return false;
        }

        @Override
        public void draw(){
            Draw.rect(bottomRegion, x, y);

            if(liquid != -1){
                LiquidBlock.drawTiledFrames(size, x, y, 0f, Vars.content.liquid(liquid), 1f);
            }

            Draw.rect(region, x, y);
        }

        @Override
        public void onProximityUpdate(){
            super.onProximityUpdate();
            if (pressureConfig. hasPressure){
                new PressureGraph().floodMergeGraph(this);
            }
        }

        @Override public PressureModule pressure(){
            return pressure;
        }
        @Override public PressureConfig pressureConfig(){
            return pressureConfig;
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);

            if (pressureConfig. hasPressure){
                pressure.read(read);
            }

            liquid = read.i();
            if(Vars.content.liquid(liquid) == null) liquid = -1;
            targetAmount = read.f();
        }

        @Override
        public void updateTile(){
            for(int i = -1; i < Vars.content.liquids().size; i++){
                if (i == liquid) {
                    pressure.setAmount(liquid, targetAmount);

                    float p = targetAmount /
                    pressureConfig.fluidCapacity /
                    OlLiquids.getDensity(Vars.content.liquid(liquid));

                    pressure.setPressure(liquid, p);
                } else {
                    pressure.setAmount(i, 0);
                    pressure.setPressure(i, 0);
                }
            }
        }

        @Override
        public void write(Writes write){
            super.write(write);

            if (pressureConfig. hasPressure){
                pressure.write(write);
            }

            write.i(liquid);
            write.f(targetAmount);
        }
    }


    public static class SourceEntry{
        public @Nullable Liquid fluid;
        public float amount;
    }
}