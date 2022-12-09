package ol.world.blocks.pressure;

import arc.Core;
import arc.func.Cons;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.struct.EnumSet;
import arc.struct.Seq;
import arc.util.io.*;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.gen.Building;
import mindustry.ui.Bar;
import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.meta.BlockFlag;
import mindustry.world.meta.BlockStatus;

import static ol.graphics.OlPal.*;
public class PressureCrafter extends GenericCrafter {

    public int tier = -1;
    /**how many pressure crafter consumes*/
    public float pressureConsume = 0;
    /**how many pressure produse*/
    public float pressureProduce = 0;

    public float maxPressure;
    public boolean canExplode = true;

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
    public void setBars() {
        super.setBars();

        addBar("pressure", (PressureCrafterBuild b) -> {
            float pressure = b.pressure / maxPressure;

            return new Bar(
                    () -> Core.bundle.get("bar.pressure"),
                    () -> mixcol(oLPressureMin, oLPressure, pressure),
                    () -> pressure
            );
        });
        if(pressureConsume > 0) {
            addBar("efficient", (PressureCrafterBuild b) -> {
                float x = b.efficenty() * 100;

                return new Bar(
                        () -> "efficient: " + (int) Math.floor(x) + "%",
                        () -> Color.orange,
                        () -> Math.min(x / 100, 1)
                );
            });
        }
    }

    public class PressureCrafterBuild extends GenericCrafterBuild implements PressureAble {
        public float pressure;
        public float effect;
        public float effectx;
        public float dt = 0;

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
        public void pressure(float pressure) {
            this.pressure = pressure;
        }
        @Override
        public boolean online() {
            return pressureConsume > 0 || pressureProduce > 0;
        }
        @Override
        public boolean storageOnly() {
            return !(pressureProduce > 0) || (downPressure && downPercent > 0);
        }

        @Override
        public float pressureThread() {
            return (pressureProduce * (effect / 100) * efficenty()) -
                    (downPressure ? (pressureConsume * (effect / 100) * downPercent) : 0);
        }

        @Override
        public void craft() {
            if(pressureConsume > 0 && efficenty() == 0) {
                return;
            }
            super.craft();
        }

        @Override
        public Seq<Building> net(Building building, Cons<PressureJunction.PressureJunctionBuild> cons, Seq<Building> buildings) {
            return PressureAble.super.net(building, cons, buildings);
        }

        @Override
        public int tier() {
            return tier;
        }

        @Override
        public BlockStatus status() {
            BlockStatus SUPER = super.status();
            if(SUPER == BlockStatus.logicDisable || SUPER == BlockStatus.noOutput) {
                return SUPER;
            }
            return pressure <= 0 ? BlockStatus.noInput : SUPER;
        }

        public float efficenty() {
            if(pressureConsume <= 0) {
                return 1;
            }

            return Math.max(Math.min(pressure/pressureConsume, 2), 0);
        }

        @Override
        public void updateTile() {

            float s = efficenty();
            progress += getProgressIncrease(craftTime) * s;
            totalProgress += delta() * s;

            if (Mathf.chanceDelta(updateEffectChance * s)) {
                updateEffect.at(x + Mathf.range(size * 4f), y + Mathf.range(size * 4));
            }

            if (canConsume()) {
                warmup = Mathf.approachDelta(warmup, 1f, warmupSpeed);
                float e = efficiency;
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

            dt++;
            if(dt >= 60) {
                dt = 0;
            }

            onUpdate(canExplode, maxPressure, explodeEffect);
        }

        @Override
        public boolean inNet(Building b, PressureAble p, boolean j) {
            return true;
        }
    }
}