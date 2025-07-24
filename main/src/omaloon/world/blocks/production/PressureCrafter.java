package omaloon.world.blocks.production;

import arc.*;
import arc.graphics.*;
import arc.math.*;
import arc.util.io.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.blocks.production.*;
import mindustry.world.consumers.*;
import mindustry.world.meta.*;
import omaloon.world.graph.*;
import omaloon.world.interfaces.*;
import omaloon.world.meta.*;
import omaloon.world.modules.*;

public class PressureCrafter extends GenericCrafter{
    public PressureConfig pressureConfig = new PressureConfig();

    public boolean useConsumerMultiplier = true;

    public float outputAir;

    public PressureCrafter(String name){
        super(name);
    }

    @Override
    public void init(){
        super.init();

        if(hasLiquids){
            hasLiquids = false;
            pressureConfig.hasPressure = true;
        }
    }

    @Override
    public void setBars(){
        super.setBars();
        pressureConfig.addBars(this);

        if(outputLiquids != null && outputLiquids.length > 0){
            removeBar("omaloon-fluid-bar");

            for(var stack : outputLiquids){
                addBar("omaloon-fluid-bar-" + stack.liquid.name, build -> {
                    HasPressure e = (HasPressure)build;
                    Liquid liq = stack.liquid;
                    return new Bar(
                    () -> liq == null ?
                    Core.bundle.format("bar.omaloon-air-bar", OlStats.formatValue(e.getFluid(liq), 2, false)) :
                    Core.bundle.format("bar.omaloon-fluid-bar", liq.localizedName, OlStats.formatValue(e.getFluid(liq), 2, false), OlStats.formatValue(e.getFluid(null), 2, false)),
                    () -> liq == null ? Color.white : liq.color,
                    () -> liq == null ? 0f : e.getFluid(liq) / Math.max(1f, Math.abs(e.getFluid(null)))
                    );
                });
            }

            if(outputAir > 0){
                addBar("omaloon-fluid-bar-air", build -> {
                    HasPressure e = (HasPressure)build;
                    Liquid liq = null;
                    return new Bar(
                    () -> Core.bundle.format("bar.omaloon-air-bar", OlStats.formatValue(e.getFluid(liq), 2, false)),
                    () -> Color.white,
                    () -> 0f
                    );
                });
            }
        }
    }

    @Override
    public void setStats(){
        super.setStats();
        pressureConfig.addStats(this, stats);

        if(outputAir > 0){
            stats.add(Stat.output, OlStats.fluid(null, outputAir, 1f, true));
        }
    }

    public class PressureCrafterBuild extends GenericCrafterBuild implements HasPressure{
        public PressureModule pressure;

        @Override
        public Building create(Block block, Team team){
            super.create(block, team);
            if(pressureConfig().hasPressure){
                pressure = new PressureModule();
                pressureGraph().addRaw(this);
            }
            return this;
        }

        @Override
        public void dumpOutputs(){
            if(outputItems != null && timer(timerDump, dumpTime / timeScale)){
                for(ItemStack output : outputItems){
                    dump(output.item);
                }
            }
        }

        public float efficiencyMultiplier(){
            float val = 1;
            if(!useConsumerMultiplier) return val;
            for(Consume consumer : consumers){
                val *= consumer.efficiencyMultiplier(this);
            }
            return val;
        }

        @Override
        public float efficiencyScale(){
            return super.efficiencyScale() * efficiencyMultiplier();
        }

        @Override
        public void onProximityUpdate(){
            super.onProximityUpdate();
            if(pressureConfig.hasPressure){
                new PressureGraph().floodMergeGraph(this);
            }
        }

        @Override
        public PressureModule pressure(){
            return pressure;
        }

        @Override
        public PressureConfig pressureConfig(){
            return pressureConfig;
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            if(pressureConfig.hasPressure){
                pressure.read(read);
            }
        }

        @Override
        public boolean shouldConsume(){
            if(outputLiquids != null && !ignoreLiquidFullness){
                boolean allFull = true;
                boolean someFull = false;

                if(getFluid(null) >= pressureConfig.fluidCapacity){
                    someFull = true;
                }else{
                    allFull = false;
                }

                for(LiquidStack output : outputLiquids){
                    if(getFluid(output.liquid) >= pressureConfig.fluidCapacity){
                        someFull = true;
                    }else{
                        allFull = false;
                    }
                }

                if(allFull || (someFull && !ignoreLiquidFullness)) return false;
            }
            return enabled;
        }

        @Override
        public void updateTile(){
            if(efficiency > 0){
                progress += getProgressIncrease(craftTime);
                warmup = Mathf.approachDelta(warmup, warmupTarget(), warmupSpeed);

                //continuously output based on efficiency, uncapped
                float inc = getProgressIncrease(1f);
                if(outputLiquids != null){
                    for(var output : outputLiquids) addFluid(output.liquid, output.amount * inc);
                }
                if(outputAir > 0) addFluid(null, outputAir * inc);

                if(wasVisible && Mathf.chanceDelta(updateEffectChance)){
                    updateEffect.at(x + Mathf.range(size * updateEffectSpread), y + Mathf.range(size * updateEffectSpread));
                }
            }else{
                warmup = Mathf.approachDelta(warmup, 0f, warmupSpeed);
            }

            totalProgress += warmup * edelta();

            if(progress >= 1f){
                craft();
            }
            dumpOutputs();
        }

        @Override
        public void write(Writes write){
            super.write(write);
            if(pressureConfig.hasPressure){
                pressure.write(write);
            }
        }
    }
}