package omaloon.world.blocks.distribution;

import arc.graphics.g2d.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.*;
import mindustry.entities.units.*;
import mindustry.type.*;
import mindustry.world.blocks.*;
import omaloon.annotations.Annotations.*;
import omaloon.world.*;
import omaloon.world.interfaces.*;

public class PressureLiquidOutlet extends GenericPressureBlock{
    public @Load("@-liquid") TextureRegion liquidRegion;

    public PressureLiquidOutlet(String name){
        super(name);
        configurable = true;
        clearOnDoubleTap = true;
        destructible = true;
        saveConfig = true;

        config(Liquid.class, (PressureLiquidOutletBuild build, Liquid liq) -> build.currentLiquid = liq);
        configClear((PressureLiquidOutletBuild build) -> build.currentLiquid = null);
    }

    @Override
    public void drawPlanConfig(BuildPlan plan, Eachable<BuildPlan> list){
        if(plan.config instanceof Liquid liq){
            Draw.color(liq.color);
            Draw.rect(liquidRegion, plan.drawx(), plan.drawy());
            Draw.color();
        }
    }

    @Override
    public void init(){
        pressureConfig.hasPressure = pressureConfig.acceptsPressure = pressureConfig.outputsPressure = true;

        super.init();

        pressureConfig.group = null;
    }

    public class PressureLiquidOutletBuild extends GenericPressureBlockBuild{
        public Liquid currentLiquid = null;

        @Override
        public boolean acceptsFluid(HasPressure from, Liquid fluid, float amount){
            return super.acceptsFluid(from, fluid, amount) && fluid == currentLiquid;
        }

        @Override
        public void buildConfiguration(Table table){
            ItemSelection.buildTable(
            table,
            Vars.content.liquids(),
            () -> currentLiquid,
            this::configure
            );
        }

        @Override
        public Liquid config(){
            return currentLiquid;
        }

        @Override
        public void draw(){
            super.draw();

            if(currentLiquid != null){
                Draw.color(currentLiquid.color);
                Draw.rect(liquidRegion, x, y);
                Draw.color();
            }
        }

        @Override
        public void drawSelect(){
            super.drawSelect();
            drawItemSelection(currentLiquid);
        }
    }
}
