package ol.world.blocks.crafting;

import arc.Core;
import arc.func.Cons;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.ui.Bar;
import mindustry.world.Tile;
import mindustry.world.blocks.production.*;
import ol.world.blocks.RegionAble;

public class OlImpactCrafter extends GenericCrafter implements RegionAble {
    public Cons<Tile> onCraft = tile -> {};

    public float deadlineTime = 300;

    public float accelerationSpeed;
    public float decelerationSpeed;
    public float powerProduction;

    public OlImpactCrafter(String name) {
        super(name);
    }

    @Override
    public void setBars() {
        super.setBars();

        addBar("acceleration", (OlImpactCrafterBuild building) -> new Bar(
                () -> Core.bundle.format("bar.acceleration", Mathf.round(building.acceleration * 100f)),
                () -> Color.orange,
                () -> building.acceleration
        ));
    }

    @Override
    public String name() {
        return name;
    }

    public class OlImpactCrafterBuild extends GenericCrafterBuild {
        public float deadlineTimer = 0;
        public float acceleration = 0;
        public boolean deadDisabled = false;

        @Override
        public void craft() {
            super.craft();

            onCraft.get(tile);
            acceleration = 0;

            deadlineTimer = deadlineTime;
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.f(acceleration);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            acceleration = read.f();
        }

        @Override
        public byte version() {
            return 1;
        }

        @Override
        public float getPowerProduction() {
            return powerProduction;
        }

        public float getAccelerationHandler() {
            return canConsume() && enabled ? accelerationSpeed * (acceleration * 2 + 1) : -decelerationSpeed;
        }

        @Override
        public void updateTile() {
            if(deadlineTimer > 0) {
                enabled = false;
                deadDisabled = true;
                deadlineTimer--;
            }

            if(deadlineTimer <= 0 && deadDisabled) {
                enabled = true;
                deadDisabled = false;
            }

            if(efficiency > 0) {
                acceleration += getAccelerationHandler();
                progress += getAccelerationHandler();

                warmup = Mathf.approachDelta(warmup, warmupTarget(), warmupSpeed);

                //continuously output based on efficiency
                if(outputLiquids != null){
                    float inc = getProgressIncrease(1f);
                    for(var output : outputLiquids){
                        handleLiquid(this, output.liquid, Math.min(output.amount * inc, liquidCapacity - liquids.get(output.liquid)));
                    }
                }

                if(wasVisible && Mathf.chanceDelta(updateEffectChance)){
                    updateEffect.at(x + Mathf.range(size * 4f), y + Mathf.range(size * 4));
                }
            }else{
                warmup = Mathf.approachDelta(warmup, 0f, warmupSpeed);
            }

            //TODO may look bad, revert to edelta() if so
            totalProgress += warmup * Time.delta;

            if(progress >= 1f) {
                craft();
            }

            dumpOutputs();
        }
    }
}