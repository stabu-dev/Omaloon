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

public class OlImpactCrafter extends PressureCrafter implements RegionAble {
    //used for impact reactors that have custom onCraft
    public Cons<Tile> onCraft = tile -> {};

    //how many ticks impact reactor turns off
    public float deadlineTime = 300F;

    //value that need to launch reactor, if 0 when launch is impossible
    public final float START_ACCELERATION = 0.000001F;

    //accelerationSpeed / decelerationSpeed used in impact
    public float accelerationSpeed;
    public float decelerationSpeed;

    //how many product power
    public float powerProduction;

    public OlImpactCrafter(String name) {
        super(name);
    }

    @Override
    public void setBars() {
        super.setBars();

        //acceleration bar
        this.addBar("acceleration", (OlImpactCrafterBuild building) -> new Bar(
                //print current acceleration that rounder (do not use arc if java have this better version)
                () -> Core.bundle.format("bar.acceleration") + ": " + Math.round(building.getAcceleration() * 100F) + "%",
                //bar color
                () -> Color.orange,
                //bar value from 0 to 1
                building::getAcceleration
        ));
    }

    @Override
    public String name() {
        return this.name;
    }

    public class OlImpactCrafterBuild extends PressureCrafterBuild {
        public float acceleration = OlImpactCrafter.this.START_ACCELERATION;
        public boolean deadDisabled = false;
        public float deadlineTimer = 0F;

        @Override
        public void craft() {
            super.craft();

            //execute on craft and set acceleration to 0
            OlImpactCrafter.this.getOnCraft().get(this.tile());
            this.setAcceleration(OlImpactCrafter.this.START_ACCELERATION);

            //turning off reactor
            this.setDeadlineTimer(OlImpactCrafter.this.getDeadlineTime());
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.f(this.getAcceleration());
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            this.setAcceleration(read.f());
        }

        @Override
        public byte version() {
            return 1;
        }

        @Override
        public float getPowerProduction() {
            return OlImpactCrafter.this.getPowerProduction();
        }

        public float getAccelerationHandler() {
            //return acceleration speed if you can consume and enabled
            if(this.canConsume() && this.enabled()) {
                return OlImpactCrafter.this.getAccelerationSpeed() * (this.getAcceleration() * 2F + 1F) * this.efficenty();
            }

            //if reactor must decelerate
            return -OlImpactCrafter.this.getDecelerationSpeed();
        }

        @Override
        public float handleProgress() {
            return this.getAccelerationHandler();
        }

        public float getEnergy() {
            float timer = this.getDeadlineTimer() / OlImpactCrafter.this.getDeadlineTime();
            return timer > 0.5F ? timer : 1F - timer;
        }

        @Override
        public float handleTotalProgress() {
            return this.enabled() ? this.delta() : this.getEnergy() * this.delta();
        }

        @Override
        public void updateTile() {
            //turn off reactor with given time
            if(this.getDeadlineTimer() > 0F) {
                this.enabled(false);
                this.setDeadDisabled(true);

                //like deadlineTimer--
                this.setDeadlineTimer(this.getDeadlineTimer() - 1F);
            }

            //if you must turn on when turns on
            if(this.getDeadlineTimer() <= 0F && this.isDeadDisabled()) {
                this.enabled(true);
                this.setDeadDisabled(false);
            }

            //set acceleration
            float accelerationHandler = this.getAccelerationHandler();
            this.scaleAcceleration(accelerationHandler);

            //limit acceleration
            if(this.getAcceleration() < 0F) {
                this.setAcceleration(0F);
            }

            //super method
            super.updateTile();

            //set progress
            this.progress = this.getAcceleration();

            ////handle by efficiency
            //if(this.efficiency() > 0F) {
            //    //set progress++
            //    this.progress += accelerationHandler;
            //
            //    //set warmup to approach delta
            //    this.warmup = Mathf.approachDelta(this.warmup(), this.warmupTarget(), OlImpactCrafter.this.warmupSpeed);
            //
            //    //continuously output based on efficiency
            //    if(OlImpactCrafter.this.outputLiquids != null) {
            //        float inc = this.getProgressIncrease(1F);
            //
            //        //handle each liquid
            //        for(var output : OlImpactCrafter.this.outputLiquids) {
            //            handleLiquid(
            //                    //source == this
            //                    this,
            //                    //output liquid
            //                    output.liquid,
            //
            //                    //Math.min amount
            //                    Math.min(
            //                            output.amount * inc,
            //                            OlImpactCrafter.this.liquidCapacity - this.liquids().get(output.liquid)
            //                    )
            //            );
            //        }
            //    }
            //
            //    //if crafter is visible and can spawn updateEffect
            //    if(this.wasVisible() && Mathf.chanceDelta(OlImpactCrafter.this.updateEffectChance)) {
            //        //spawn update effect
            //        OlImpactCrafter.this.updateEffect.at(
            //                this.x() + Mathf.range(OlImpactCrafter.this.size * 4F),
            //                this.y() + Mathf.range(OlImpactCrafter.this.size * 4F)
            //        );
            //    }
            //} else {
            //    this.warmup = Mathf.approachDelta(this.warmup(), 0F, OlImpactCrafter.this.warmupSpeed);
            //}
            //
            ////total progress add
            //this.totalProgress += this.warmup() * Time.delta;
            //
            //if progress >= 1F when craft
            //if(this.progress() >= 1F) {
            //    this.craft();
            //}
            //
            ////dump outputs
            //this.dumpOutputs();
        }

        public void scaleAcceleration(float scale) {
            this.setAcceleration(this.getAcceleration() + scale);
        }

        public boolean isDeadDisabled() {
            return this.deadDisabled;
        }

        public float getAcceleration() {
            return this.acceleration;
        }

        public float getDeadlineTimer() {
            return this.deadlineTimer;
        }

        public void setAcceleration(float acceleration) {
            this.acceleration = acceleration;
        }

        public void setDeadlineTimer(float deadlineTimer) {
            this.deadlineTimer = deadlineTimer;
        }

        public void setDeadDisabled(boolean deadDisabled) {
            this.deadDisabled = deadDisabled;
        }
    }

    public Cons<Tile> getOnCraft() {
        return this.onCraft;
    }

    public float getAccelerationSpeed() {
        return this.accelerationSpeed;
    }

    public float getDeadlineTime() {
        return this.deadlineTime;
    }

    public float getPowerProduction() {
        return this.powerProduction;
    }

    public float getDecelerationSpeed() {
        return this.decelerationSpeed;
    }

    public void setDeadlineTime(float deadlineTime) {
        this.deadlineTime = deadlineTime;
    }

    public void setAccelerationSpeed(float accelerationSpeed) {
        this.accelerationSpeed = accelerationSpeed;
    }

    public void setDecelerationSpeed(float decelerationSpeed) {
        this.decelerationSpeed = decelerationSpeed;
    }

    public void setOnCraft(Cons<Tile> onCraft) {
        this.onCraft = onCraft;
    }

    public void setPowerProduction(float powerProduction) {
        this.powerProduction = powerProduction;
    }
}