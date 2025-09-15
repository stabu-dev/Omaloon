package omaloon.world.blocks.distribution;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.entities.*;
import mindustry.type.*;
import mindustry.world.meta.*;
import omaloon.annotations.Annotations.*;
import omaloon.content.*;
import omaloon.math.*;
import omaloon.world.*;
import omaloon.world.interfaces.*;
import omaloon.world.meta.*;
import omaloon.world.meta.PressureTank.*;

import static mindustry.Vars.renderer;
import static mindustry.type.Liquid.animationFrames;

public class PressureLiquidPump extends GenericPressureBlock{
    public float pumpStrength = 0.1f;

    public float pressureDifference = 10;

    public float liquidPadding = 3f;
    public float smoothAlphaSpeed = 0.014f;

    public float effectInterval = 5f;
    public Effect pumpEffectOut = OlFx.pumpOut;
    public Effect pumpEffectIn = OlFx.pumpIn;

    public TextureRegion[][] liquidRegions;
    public @Load(value = "@-#0$", lengths = {4}) TextureRegion[] tiles;
    public @Load(value = "@-top") TextureRegion topRegion;
    public @Load(value = "@-bottom", fallBack = "omaloon-liquid-bottom") TextureRegion bottomRegion;
    public @Load("@-arrow") TextureRegion arrowRegion;

    public PressureLiquidPump(String name){
        super(name);
        rotate = true;
        destructible = true;
        update = true;
        saveConfig = copyConfig = true;
    }

//    @Override
//    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list){
//
//        var tiling = IntRef.tmp1.zero();
//
//        int dx = Geometry.d4x(plan.rotation), dy = Geometry.d4y(plan.rotation);
//        var front = Point2.pack(plan.x + dx, plan.y + dy);
//        var back = Point2.pack(plan.x - dx, plan.y - dy);
//
//        boolean inverted = plan.rotation == 1 || plan.rotation == 2;
//        list.each(next -> {
//            var nextPoint = Point2.pack(next.x, next.y);
//            if(!next.block.outputsLiquid) return;
//            if(nextPoint == front) tiling.value |= inverted ? 0b10 : 1;
//            if(nextPoint == back) tiling.value |= inverted ? 1 : 0b10;
//        });
//
//        Draw.rect(bottomRegion, plan.drawx(), plan.drawy(), 0);
//        if(tiling.value != 0) Draw.rect(arrowRegion, plan.drawx(), plan.drawy(), (plan.rotation) * 90f);
//        Draw.rect(tiles[tiling.value], plan.drawx(), plan.drawy(), (plan.rotation + 1) * 90f % 180 - 90);
//        if(tiling.value == 0) Draw.rect(topRegion, plan.drawx(), plan.drawy(), (plan.rotation) * 90f);
//    }

    @Override
    public TextureRegion[] icons(){
        return new TextureRegion[]{
        Core.atlas.find(name + "-0"),
        Core.atlas.find(name + "-top")
        };
    }

    @Override
    public void init(){
        pressureConfig.hasPressure = true;
        pressureConfig.acceptsPressure = pressureConfig.outputsPressure = false;

        super.init();

        pressureConfig.group = TankGroup.pumps;
    }

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

    @Override
    public void setBars(){
        super.setBars();
        removeBar("omaloon-fluid-bar");
    }

    @Override
    public void setStats(){
        super.setStats();
        stats.remove(Stat.liquidCapacity);
        stats.add(OlStats.pumpStrength, pumpStrength * 60f, StatUnit.liquidSecond);
        stats.add(OlStats.pressureGradient, OlStats.number(pressureDifference, OlStats.pressureUnit, false));
    }

    public class PressureLiquidPumpBuild extends GenericPressureBlockBuild{
        public float effectTimer;
        public int tiling;
        public float smoothAlpha;

        public boolean functioning;

        @Override
        public boolean acceptsFluid(HasPressure from, @Nullable Liquid liquid, float amount){
            return false;
        }

        @Override
        public float ambientVolume(){
            return 1f / chainSize();
        }

        /**
         * Returns the length of the pump chain
         */
        public int chainSize(){
            return pressure.section.builds.size;
        }

        @Override
        public boolean connects(HasPressure to){
            return
            super.connects(to) &&
            (
            front() == to || back() == to ||
            !proximity.contains(to.toBuilding())
            ) &&
            (
            !(to instanceof PressureLiquidPumpBuild pump) ||
            pump.rotation == rotation
            );
        }

        @Override
        public void draw(){
            Draw.rect(bottomRegion, x, y);
            if(tiling != 0){
                HasPressure front = getTo();
                HasPressure back = getFrom();

                @Nullable Liquid frontLiquid = front != null && front.pressure() != null ? front.pressure().getMain() : null;
                @Nullable Liquid backLiquid = back != null && back.pressure() != null ? back.pressure().getMain() : null;
                @Nullable Liquid pumpLiquid = backLiquid != null ? backLiquid : frontLiquid;

                Color drawColor = Tmp.c1.set(
                frontLiquid == null ? Color.clear : frontLiquid.color
                ).lerp(
                backLiquid == null ? Color.clear : backLiquid.color, 0.5f
                );

                float alpha = 0;
                alpha += frontLiquid != null ? 0.5f : 0f;
                alpha += backLiquid != null ? 0.5f : 0f;

                smoothAlpha = Mathf.approachDelta(smoothAlpha, alpha, smoothAlphaSpeed);

                if(pumpLiquid != null){
                    Draw.color(drawColor, smoothAlpha);
                    Draw.rect(liquidRegions[Mathf.num(pumpLiquid.gas)][pumpLiquid.getAnimationFrame()], x, y);
                    Draw.color();
                }

                Draw.rect(arrowRegion, x, y, rotdeg());
            }

            if(rotation == 1 || rotation == 2) Draw.yscl = -1f;
            Draw.rect(tiles[tiling], x, y, rotdeg());
            Draw.yscl = 1f;

            if(tiling == 0) Draw.rect(topRegion, x, y, rotdeg());
        }

        /**
         * Returns the building at the start of the pump chain.
         */
        public @Nullable HasPressure getFrom(){
            PressureLiquidPumpBuild last = this;
            HasPressure out = back() instanceof HasPressure back ? back.getFluidDestination(last, null) : null;
            while(out instanceof PressureLiquidPumpBuild pump){
                if(!HasPressure.connects(pump, last)) return null;
                last = pump;
                out = pump.back() instanceof HasPressure back ? back.getFluidDestination(last, null) : null;
            }
            return (out != null && HasPressure.connects(out, last)) ? out : null;
        }

        /**
         * Returns the building at the end of the pump chain.
         */
        public @Nullable HasPressure getTo(){
            PressureLiquidPumpBuild last = this;
            HasPressure out = front() instanceof HasPressure front ? front.getFluidDestination(last, null) : null;
            while(out instanceof PressureLiquidPumpBuild pump){
                if(!HasPressure.connects(pump, last)) return null;
                last = pump;
                out = pump.front() instanceof HasPressure front ? front.getFluidDestination(last, null) : null;
            }
            return (out != null && HasPressure.connects(out, last)) ? out : null;
        }

        @Override
        public void onPressureGraphUpdate(){
            tiling = 0;
            if(front() instanceof HasPressure front && HasPressure.connects(this, front.getFluidDestination(this, null))) tiling |= 1;
            if(back() instanceof HasPressure back && HasPressure.connects(this, back.getFluidDestination(this, null))) tiling |= 2;
        }

        @Override
        public boolean outputsFluid(HasPressure to, @Nullable Liquid liquid, float amount){
            return false;
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            smoothAlpha = read.f();
        }

        @Override
        public boolean shouldAmbientSound(){
            return functioning;
        }

        @Override
        public void updateTile(){
            if(efficiency > 0){
                HasPressure front = getTo();
                HasPressure back = getFrom();

                @Nullable Liquid pumpLiquid = (back == null ? null : back.pressure().getMain());

                float frontPressure = front == null ? 0 : front.getPressure(pumpLiquid);
                float backPressure = back == null ? 0 : back.getPressure(pumpLiquid);

                float maxFlow = -Physics.fluidFlow(
                backPressure + pressureDifference * chainSize(),
                back == null ? 8 : back.pressureConfig().fluidCapacity,
                frontPressure,
                front == null ? 8 : front.pressureConfig().fluidCapacity,
                OlLiquids.getDensity(pumpLiquid),
                1, 1
                );

                if(back != null){
                    pressure.pressures[0] = back.getPressure(pumpLiquid);
                }else pressure.pressures[0] = 0;
                if(front != null){
                    pressure.pressures[0] += front.getPressure(pumpLiquid);
                }
                pressure.pressures[0] /= 2f;

                float flow = Mathf.clamp(
                (maxFlow > 0 ? pumpStrength : -pumpStrength) / chainSize() * Time.delta,
                -Math.abs(maxFlow),
                Math.abs(maxFlow)
                );

                if(effectTimer >= effectInterval && !Mathf.zero(flow, 0.001f)){
                    if(flow < 0){
                        if(pumpLiquid == null || (front != null && front.getFluid(pumpLiquid) > 0.001f)){
                            if(back == null && !(back() instanceof PressureLiquidPumpBuild p && p.rotation == rotation)) pumpEffectOut.at(x, y, rotdeg() + 180f, pumpLiquid == null ? Color.white : pumpLiquid.color);
                            if(front == null && !(front() instanceof PressureLiquidPumpBuild p && p.rotation == rotation)) pumpEffectIn.at(x, y, rotdeg(), Color.white);
                        }
                    }else{
                        if(pumpLiquid == null || (back != null && back.getFluid(pumpLiquid) > 0.001f)){
                            if(back == null && !(back() instanceof PressureLiquidPumpBuild p && p.rotation == rotation)) pumpEffectIn.at(x, y, rotdeg() + 180f, Color.white);
                            if(front == null && !(front() instanceof PressureLiquidPumpBuild p && p.rotation == rotation)) pumpEffectOut.at(x, y, rotdeg(), pumpLiquid == null ? Color.white : pumpLiquid.color);
                        }
                    }
                    effectTimer %= 1;
                }

                functioning = !Mathf.zero(flow, 0.001f);

                if(
                front == null || back == null ||
                (front.acceptsFluid(back, pumpLiquid, flow) &&
                back.outputsFluid(front, pumpLiquid, flow))
                ){
                    effectTimer += edelta();
                    if(front != null){
                        front.addFluid(pumpLiquid, flow);
                    }else if(pumpLiquid != null && flow > 0) Puddles.deposit(tile.nearby(rotation), tile, pumpLiquid, flow);
                    if(back != null){
                        back.removeFluid(pumpLiquid, flow);
                    }else if(pumpLiquid != null && flow < 0) Puddles.deposit(tile.nearby((rotation + 2) % 4), tile, pumpLiquid, -flow);
                }
            }
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.f(smoothAlpha);
        }
    }
}