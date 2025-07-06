package omaloon.world.blocks.distribution;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.meta.*;
import omaloon.annotations.Annotations.*;
import omaloon.world.*;
import omaloon.world.interfaces.*;
import omaloon.world.meta.PressureTank.*;

import static mindustry.Vars.renderer;
import static mindustry.type.Liquid.animationFrames;

public class PressureLiquidConduit extends GenericPressureBlock{
    public @Load(value = "@-bottom", fallBack = "@modname-liquid-bottom") TextureRegion bottomRegion;
    public @Load(value = "@-#0$", lengths = {16}) TextureRegion[] topRegions;
    public TextureRegion[][] liquidRegions;

    public float liquidPadding = 3f;
    public float smoothAlphaSpeed = 0.014f;

    public @Nullable Block junctionReplacement, bridgeReplacement;

    public PressureLiquidConduit(String name){
        super(name);
        rotate = true;
        destructible = true;
        update = true;
        canOverdrive = false;
        group = BlockGroup.liquids;
    }

    @Override
    protected TextureRegion[] icons(){
        return new TextureRegion[]{
        Core.atlas.find(name + "-bottom", "omaloon-liquid-bottom"),
        Core.atlas.find(name + "-0")
        };
    }

    @Override
    public void init(){
        pressureConfig.hasPressure = pressureConfig.acceptsPressure = pressureConfig.outputsPressure = true;

        super.init();

        if(hasLiquids) hasLiquids = false;
//        if(junctionReplacement == null) junctionReplacement = OlDistributionBlocks.liquidJunction;
//        if(bridgeReplacement == null || !(bridgeReplacement instanceof ItemBridge)) bridgeReplacement = OlDistributionBlocks.liquidBridge;
//
        if(pressureConfig.group == null) pressureConfig.group = TankGroup.transportation;
    }

//    @Override
//    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list){
//        int tiling = 0;
//        BuildPlan[] proximity = new BuildPlan[4];
//
//        list.each(next -> {
//            for(int i = 0; i < 4; i++){
//                Point2 side = new Point2(plan.x, plan.y).add(Geometry.d4[i]);
//                if(new Point2(next.x, next.y).equals(side) && (
//                (next.block instanceof PressureLiquidConduit || next.block instanceof PressureLiquidPump || next.block instanceof PressureLiquidValve) ?
//                (plan.rotation % 2 == i % 2 || next.rotation % 2 == i % 2) : (next.block.outputsLiquid))
//                ){
//                    proximity[i] = next;
//                    break;
//                }
//            }
//        });
//
//        for(int i = 0; i < 4; i++){
//            if(proximity[i] != null) tiling |= (1 << i);
//        }
//
//        Draw.rect(bottomRegion, plan.drawx(), plan.drawy(), 0);
//        if(tiling == 0){
//            Draw.rect(topRegions[tiling], plan.drawx(), plan.drawy(), (plan.rotation + 1) * 90f % 180 - 90);
//        }else{
//            Draw.rect(topRegions[tiling], plan.drawx(), plan.drawy(), 0);
//        }
//    }

    @Override
    public void load(){
        super.load();

        liquidRegions = new TextureRegion[2][animationFrames];
        if(renderer != null){
            var frames = renderer.getFluidFrames();

            for(int fluid = 0; fluid < 2; fluid++){
                for(int frame = 0; frame < animationFrames; frame++){
                    TextureRegion base = frames[fluid][frame];
                    TextureRegion result = new TextureRegion();
                    result.set(base);

                    result.setHeight(result.height - liquidPadding);
                    result.setWidth(result.width - liquidPadding);
                    result.setX(result.getX() + liquidPadding);
                    result.setY(result.getY() + liquidPadding);

                    liquidRegions[fluid][frame] = result;
                }
            }
        }
    }

//    @Override
//    public Block getReplacement(BuildPlan req, Seq<BuildPlan> plans){
//        if(junctionReplacement == null) return this;
//
//        Boolf<Point2> cont = p -> plans.contains(o -> o.x == req.x + p.x && o.y == req.y + p.y && (req.block instanceof PressureLiquidConduit || req.block instanceof PressureLiquidJunction));
//        return cont.get(Geometry.d4(req.rotation)) &&
//        cont.get(Geometry.d4(req.rotation - 2)) &&
//        req.tile() != null &&
//        req.tile().block() instanceof PressureLiquidConduit &&
//        Mathf.mod(req.build().rotation - req.rotation, 2) == 1 ? junctionReplacement : this;
//    }

//    @Override
//    public void handlePlacementLine(Seq<BuildPlan> plans){
//        if(bridgeReplacement == null) return;
//
//        Placement.calculateBridges(plans, (ItemBridge)bridgeReplacement);
//    }

    public class PressureLiquidConduitBuild extends GenericPressureBlockBuild{
        public int tiling = 0;
        public float smoothAlpha;

//        @Override
//        public boolean acceptsPressurizedFluid(HasPressure from, @Nullable Liquid liquid, float amount){
//            return HasPressureImpl.super.acceptsPressurizedFluid(from, liquid, amount) && (liquid == pressure.getMain() || liquid == null || pressure.getMain() == null || from.pressure().getMain() == null);
//        }

        @Override
        public boolean connects(HasPressure to){
            return
            super.connects(to) &&
            (
            !(to instanceof PressureLiquidConduitBuild) ||
            to == front() || to == back() ||
            this == to.toBuilding().front() || this == to.toBuilding().back()
            );
        }

        @Override
        public void draw(){
            Draw.rect(bottomRegion, x, y);
            Liquid main = pressure.getMain();

            smoothAlpha = Mathf.approachDelta(smoothAlpha, main == null ? 0f : getFluid(main) / (getFluid(main) + getFluid(null)), smoothAlphaSpeed);

            if(smoothAlpha > 0.001f && main != null){
                int frame = main.getAnimationFrame();
                int gas = main.gas ? 1 : 0;

                float xscl = Draw.xscl, yscl = Draw.yscl;
                Draw.scl(1f, 1f);
                Drawf.liquid(liquidRegions[gas][frame], x, y, Mathf.clamp(smoothAlpha), main.color.write(Tmp.c1).a(1f));
                Draw.scl(xscl, yscl);
            }
            Draw.rect(topRegions[tiling], x, y, tiling != 0 ? 0 : (rotdeg() + 90) % 180 - 90);
        }

//        @Override
//        public boolean outputsPressurizedFluid(HasPressure to, Liquid liquid, float amount){
//            return HasPressureImpl.super.outputsPressurizedFluid(to, liquid, amount) && (liquid == to.pressure().getMain() || liquid == null || pressure.getMain() == null || to.pressure().getMain() == null);
//        }

        @Override
        public void onPressureGraphUpdate(){
            tiling = 0;
            for(int i = 0; i < 4; i++){
                HasPressure build = nearby(i) instanceof HasPressure ? (HasPressure)nearby(i) : null;
                if(
                build != null && HasPressure.connects(this, build)
                ) tiling |= (1 << i);
            }
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            smoothAlpha = read.f();
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.f(smoothAlpha);
        }
    }
}
