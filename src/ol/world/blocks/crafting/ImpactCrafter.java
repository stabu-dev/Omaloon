package ol.world.blocks.crafting;

import arc.*;
import arc.func.*;
import arc.graphics.*;

import arc.util.io.*;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.ui.*;
import mindustry.world.*;

import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import ol.world.blocks.*;

public class ImpactCrafter extends PressureCrafter implements RegionAble {
    //used for impact reactors that have custom onCraft
    public Cons<Tile> onCraft = tile -> {};
    public Effect stopEffect = Fx.none;

    //how many ticks impact reactor turns off
    public float deadlineTime = 3F;

    //value that need to launch reactor, if 0 when launch is impossible
    public final float START_ACCELERATION = 0.000001F;

    //accelerationSpeed / decelerationSpeed used in impact
    public float accelerationSpeed;
    public float decelerationSpeed;

    /*how many product power*/
    public float powerProduction;

    public ImpactCrafter(String name) {
        super(name);
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.add(Stat.basePowerGeneration, powerProduction * 60.0f, StatUnit.powerSecond);
    }

    @Override
    public void setBars() {
        super.setBars();

        //acceleration bar
        this.addBar("acceleration", (ImpactCrafterBuild building) -> new Bar(
                //print current acceleration that rounder (do not use arc if java have this better version)
                () -> Core.bundle.format("bar.acceleration") + " " + Math.round(building.getAcceleration() * 100F) + "%",
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

    public class ImpactCrafterBuild extends PressureCrafterBuild {
        public float acceleration = ImpactCrafter.this.START_ACCELERATION;
        public boolean deadDisabled = false;
        public float deadlineTimer = 0F;

        @Override
        public void craft() {
            super.craft();

            //execute on craft and set acceleration to 0
            ImpactCrafter.this.getOnCraft().get(this.tile());
            this.setAcceleration(ImpactCrafter.this.START_ACCELERATION);

            //turning off reactor
            this.setDeadlineTimer(ImpactCrafter.this.getDeadlineTime());
            stopEffect.at(this);
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.f(this.getAcceleration());
            write.f(this.getDeadlineTimer());
            write.bool(this.isDeadDisabled());
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            this.setAcceleration(read.f());
            this.setDeadlineTimer(read.f());
            this.setDeadDisabled(read.bool());
        }

        @Override
        public byte version() {
            return 1;
        }

        @Override
        public float getPowerProduction() {
            return ImpactCrafter.this.getPowerProduction() * acceleration;
        }

        public float getAccelerationHandler() {
            //return acceleration speed if you can consume and enabled
            if(this.canConsume() && this.enabled()) {
                return ImpactCrafter.this.getAccelerationSpeed() *
                        (this.getAcceleration() * 2F + 1F) * this.efficenty() * super.efficiency();
            }

            //if reactor must decelerate
            return -ImpactCrafter.this.getDecelerationSpeed();
        }

        @Override
        public float handleProgress() {
            return this.getAccelerationHandler();
        }

        public float getEnergy() {
            /*
             * acceleration                        stop point
             * ^                                     **
             * |                                    * *
             * |                                   *  *
             * |                                  *   *
             * |                                **    *
             * |_____________________________***______*________________________________ Time.delta();
             * |                         ****          ****
             * |                    *****                  ***
             * |              ******                          **
             * |       *******                                   *
             * |*******                                           *
             * #--------------------- time of work ---------------------------------->
             */

            float timer = this.getDeadlineTimer() / ImpactCrafter.this.getDeadlineTime();
            timer = 1 - timer;

            if(timer > 0.3) {
                return 0;
            }

            return 1f - (timer / 0.3f);
        }

        @Override
        public float handleTotalProgress() {
            return this.enabled() ? this.delta() * (this.acceleration * 2) : this.getEnergy() * this.delta();
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

        @Override
        public float ambientVolume(){
            return acceleration*2;
        }
    }

    public Cons<Tile> getOnCraft() {
        return this.onCraft;
    }

    public float getAccelerationSpeed() {
        return this.accelerationSpeed;
    }

    public float getDeadlineTime() {
        return this.deadlineTime*100;
    }

    public float getPowerProduction() {
        return this.powerProduction;
    }

    public float getDecelerationSpeed() {
        return this.decelerationSpeed;
    }

}