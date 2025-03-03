package omaloon.world.blocks.sandbox;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.ImageButton.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.blocks.liquid.*;
import omaloon.annotations.*;
import omaloon.content.*;
import omaloon.ui.elements.*;
import omaloon.world.interfaces.*;
import omaloon.world.meta.*;

public class PressureLiquidSource extends Block{
    public PressureConfig pressureConfig = new PressureConfig();

    public TextureRegion bottomRegion;

    public PressureLiquidSource(String name){
        super(name);
        solid = true;
        destructible = true;
        update = true;
        configurable = true;
        saveConfig = copyConfig = true;

        config(SourceEntry.class, (PressureLiquidSourceBuild build, SourceEntry entry) -> {
            build.liquid = entry.fluid == null ? -1 : entry.fluid.id;
            build.targetAmount = entry.amount;

            Vars.content.liquids().each(liquid -> {
                build.pressure.liquids[liquid.id] = 0;
                build.pressure.pressures[liquid.id] = 0;
            });

            build.pressure.air = build.pressure.pressure = 0;
        });
    }

    @Override
    public void drawPlanConfig(BuildPlan plan, Eachable<BuildPlan> list){
        Draw.rect(bottomRegion, plan.drawx(), plan.drawy());
        if(plan.config instanceof SourceEntry e && e.fluid != null) LiquidBlock.drawTiledFrames(size, plan.drawx(), plan.drawy(), 0f, e.fluid, 1f);
        Draw.rect(region, plan.drawx(), plan.drawy());
    }

    @Override
    public void load(){
        super.load();
        bottomRegion = Core.atlas.find(name + "-bottom");
    }

    @Override
    protected TextureRegion[] icons(){
        return new TextureRegion[]{bottomRegion, region};
    }

    @Override
    public void init(){
        super.init();

        if(pressureConfig.fluidGroup != null) pressureConfig.fluidGroup = FluidGroup.transportation;
    }

    @Override
    public void setBars(){
        super.setBars();
        pressureConfig.addBars(this);
        addBar("pressure", entity -> {
            HasPressure build = (HasPressure)entity;

            return new CenterBar(
                () -> Core.bundle.get("bar.pressure") + (build.pressure().getPressure(build.pressure().getMain()) < 0 ? "-" : "+") + Strings.autoFixed(Math.abs(build.pressure().getPressure(build.pressure().getMain())), 2),
                () -> Color.white,
                () -> Mathf.map(build.pressure().getPressure(build.pressure().getMain()), pressureConfig.minPressure, pressureConfig.maxPressure, -1, 1)
            );
        });
    }

    public class PressureLiquidSourceBuild extends Building implements HasPressureImpl{

        public int liquid = -1;
        public float targetAmount;

        @Override
        public boolean acceptsPressurizedFluid(HasPressure from, Liquid liquid, float amount){
            return HasPressureImpl.super.acceptsPressurizedFluid(from, liquid, amount) && liquid == Vars.content.liquid(this.liquid);
        }

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
        public void draw(){
            Draw.rect(bottomRegion, x, y);

            if(liquid != -1){
                LiquidBlock.drawTiledFrames(size, x, y, 0f, Vars.content.liquid(liquid), 1f);
            }

            Draw.rect(region, x, y);
        }

        @Override
        public boolean outputsPressurizedFluid(HasPressure to, Liquid liquid, float amount){
            return HasPressureImpl.super.outputsPressurizedFluid(to, liquid, amount) && liquid == Vars.content.liquid(this.liquid);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            liquid = read.i();
            if(Vars.content.liquid(liquid) == null) liquid = -1;
            targetAmount = read.f();
        }

        @Override
        @AutoImplement.NoInject(HasPressureImpl.class)
        public void updateTile(){
            pressure.section.updateTransfer();

            pressure.air = Vars.content.liquid(liquid) == null ? targetAmount : 0;
            pressure.pressure = pressure.air / pressureConfig.fluidCapacity * OlLiquids.defaultLiquidInfo.density;
            Vars.content.liquids().each(liq -> {
                pressure.liquids[liq.id] = liq.id == liquid ? Mathf.maxZero(targetAmount) : 0;
                pressure.pressures[liq.id] = pressure.liquids[liq.id] / pressureConfig.fluidCapacity * OlLiquids.liquidInfo(liq).density;
            });
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.i(liquid);
            write.f(targetAmount);
        }
    }


    public static class SourceEntry{
        public @Nullable Liquid fluid;
        public float amount;
    }
}
