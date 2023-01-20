package ol.world.blocks.crafting;

import arc.func.Cons;
import arc.math.*;
import arc.struct.*;
import arc.util.io.*;

import mindustry.content.*;
import mindustry.entities.*;
import mindustry.gen.*;
import mindustry.ui.*;
import mindustry.world.blocks.production.*;
import mindustry.world.meta.*;

import ol.gen.*;
import ol.utils.pressure.*;
import ol.world.blocks.pressure.PressureBridge;
import ol.world.blocks.pressure.PressureJunction;
import ol.world.meta.*;

import static arc.Core.*;
import static ol.graphics.OlPal.*;

public class PressureCrafter extends GenericCrafter {

    public int tier = -1;
    /**how many pressure crafter consumes*/
    public float pressureConsume = 0;
    /**how many pressure produce*/
    public float pressureProduce = 0;

    public float maxPressure;
    public boolean canExplode = true;
    public boolean showPressure = false;

    /**when block works pressure is make lower*/
    public boolean downPressure;
    public float downPercent = 0.25f;

    //boom
    public Effect explodeEffect = Fx.none;
    public PressureCrafter(String name) {
        super(name);

        flags = EnumSet.of(BlockFlag.factory);
    }

    @Override
    public void setStats(){
        super.setStats();
        if(showPressure) {
            stats.remove(Stat.productionTime);
        }

        if(tier > 0){
            stats.add(OlStat.tier, tier);
        }

        if(pressureProduce > 0) {
            stats.add(OlStat.pressureProduction, (int) pressureProduce, OlStatUnit.pressure);
        }

        if(pressureConsume > 0) {
            stats.add(OlStat.pressureConsume, (int) pressureConsume, OlStatUnit.pressure);
        }

        if(canExplode) {
            stats.add(OlStat.maxPressure, maxPressure, OlStatUnit.pressure);
        }
    }

    @Override
    public void setBars() {
        super.setBars();

        if(!showPressure && pressureConsume > 0) {
            addBar("pressure", (PressureCrafterBuild b) -> {
                float pressure = b.pressure / b.maxPressure();
                return new Bar(
                        () -> bundle.format("bar.pressureEfficient", (int) Math.floor(b.pressure), (int) (b.efficenty() * 100 + 0.0001f)),
                        () -> mixcol(oLPressureMin, oLPressure, pressure),
                        () -> pressure
                );
            });
        } else {
            addBar("pressure", (PressureCrafterBuild b) ->{
                float pressure = b.pressure / b.maxPressure();
                return new Bar(
                        () -> bundle.get("bar.pressure") + " " + (int)(b.pressure),
                        () -> mixcol(oLPressureMin, oLPressure, pressure),
                        () -> pressure
                );
            });
        }
    }

    public class PressureCrafterBuild extends GenericCrafterBuild implements PressureAblecImpl{
        public PressureNet pressureNet;
        protected int pressureAble_index=-1;
        @Override
        public void setIndex__pressureAble(int index){
            pressureAble_index=index;
        }
        @Override
        public PressureNet pressureNet(){
            return pressureNet;
        }
        @Override
        public void pressureNet(PressureNet pressureNet){
            this.pressureNet = pressureNet;
        }

        public float pressure;
        public float effect;
        public float effectx;

        @Override
        public void add(){
            boolean wasAdded = added;
            super.add();
            if (!wasAdded && added){
                OlGroups.pressureAble.add(this);
            }
        }

        @Override
        public void remove(){
            boolean wasAdded = added;
            super.remove();
            if (wasAdded && !added){
                OlGroups.pressureAble.remove(this);
            }
        }

        @Override
        public boolean inNet(Building b, PressureAblec p, boolean j) {
            return !(b instanceof PressureCrafterBuild);
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.f(pressure);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            pressure = read.f();
        }

        @Override
        @SuppressWarnings("unchecked")
        public PressureCrafterBuild self() {
            return this;
        }

        @Override
        public float pressure() {
            return pressure;
        }

        @Override
        public void draw() {
            /*don't work: if(!squareSprite) {
                for(Building b : proximity) {
                    if(b instanceof PressureAble<?> pressureAble && pressureAble.inNet(b, false) && !(b instanceof PressureCrafterBuild)) {
                        Draw.draw(Layer.max, () -> {
                            Draw.rect(
                                    b.block.region,
                                    b.x + (b.x > x ? -8 : 8),
                                    b.y + (b.y > y ? -8 : 8),
                                    b.drawrot()
                            );
                        });
                    }
                }
            }*/

            super.draw();
        }

        @Override
        public void pressure(float pressure) {
            this.pressure = pressure;
        }

        @Override
        public boolean online() {
            return pressureConsume() > 0 || pressureProduce() > 0;
        }

        @Override
        public float pressureThread() {
            return (pressureProduce() * (effect / 100) * efficenty()) -
                    (downPressure() && status() == BlockStatus.active ? (pressureConsume() * downPercent()) : 0);
        }

        @Override
        public boolean downPressure() {
            return downPressure;
        }

        public float downPercent() {
            return downPercent;
        }

        @Override
        public float calculatePressureDown() {
            return pressureConsume() * downPercent() * (canConsume() ? 1 : 0);
        }

        public float pressureConsume() {
            return pressureConsume;
        }

        public float pressureProduce() {
            return pressureProduce;
        }

        @Override
        public float maxPressure() {
            return maxPressure;
        }

        @Override
        public boolean canExplode() {
            return canExplode;
        }

        @Override
        public Effect explodeEffect() {
            return explodeEffect;
        }

        @Override
        public void craft() {
            if(pressureConsume() > 0 && efficenty() == 0) {
                return;
            }

            super.craft();
        }

        @Override
        public int tier() {
            return tier;
        }

        @Override
        public BlockStatus status() {
            if(!online()) {
                return super.status();
            }

            BlockStatus SUPER = super.status();
            if(SUPER == BlockStatus.logicDisable || SUPER == BlockStatus.noOutput) {
                return SUPER;
            }

            return pressure <= 0 ? BlockStatus.noInput : SUPER;
        }

        public float efficenty() {
            if(pressureConsume() <= 0) {
                return 1;
            }

            return Math.max(Math.min(pressure/pressureConsume(), 2), 0);
        }

        public float handleProgress() {
            return this.efficenty() * this.getProgressIncrease(craftTime);
        }

        public float handleTotalProgress() {
            return this.efficenty() * this.delta();
        }

        @Override
        public void updateTile() {
            progress += this.handleProgress();
            totalProgress += this.handleTotalProgress();

            if (Mathf.chanceDelta(updateEffectChance * this.efficenty())) {
                updateEffect.at(x + Mathf.range(size * 4f), y + Mathf.range(size * 4));
            }

            if (canConsume()) {
                warmup = Mathf.approachDelta(warmup, 1f, warmupSpeed);
            } else {
                warmup = Mathf.approachDelta(warmup, 0f, warmupSpeed);
            }

            if (progress >= 1f) {
                craft();
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
        public boolean producePressure() {
            return pressureProduce() > 0;
        }
    }
}