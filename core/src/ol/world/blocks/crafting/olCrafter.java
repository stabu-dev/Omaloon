package ol.world.blocks.crafting;

import arc.*;
import arc.func.*;
import arc.math.*;
import arc.util.Strings;
import arc.util.Tmp;
import arc.util.io.*;
import mindustry.content.Fx;
import mindustry.entities.Damage;
import mindustry.entities.Effect;
import mindustry.game.EventType;
import mindustry.gen.Sounds;
import mindustry.graphics.*;
import mindustry.logic.LAccess;
import mindustry.ui.*;
import mindustry.world.blocks.power.PowerGenerator;
import mindustry.world.blocks.production.*;
import mindustry.world.consumers.*;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;

import static mindustry.Vars.state;
import static mindustry.Vars.tilesize;

public class olCrafter extends GenericCrafter{
    public final int timerUse = timers++;
    public float warmupSpeed = 0.03f, deWarmupSpeed = 0.05f;
    public Interp interp = Interp.smoother;
    public float powerProduction;
    public Stat generationType = Stat.basePowerGeneration;
    public int explosionRadius = 23;
    public int explosionDamage = 1000;
    public Effect explodeEffect = Fx.impactReactorExplosion;

    public Cons<olCrafterBuild> onCraft = tile -> {};

    public olCrafter(String name){
        super(name);
        outputsPower = true;
    }

    @Override
    public void setStats(){
        super.setStats();
        stats.add(generationType, powerProduction * 60.0f, StatUnit.powerSecond);
        if(hasItems){
            stats.add(Stat.productionTime, itemCapacity / 60f, StatUnit.seconds);
        }
    }

    @Override
    public void setBars(){
        super.setBars();

        bars.add("speed", (olCrafterBuild entity) -> new Bar(
                () -> Core.bundle.format("bar.acceleration", Mathf.round(entity.getSpeed() * 100f)),
                () -> Pal.orangeSpark,
                entity::getSpeed
        ));
        bars.add("poweroutput", (olCrafterBuild entity) -> new Bar(() ->
                Core.bundle.format("bar.poweroutput",
                        Strings.fixed(Math.max(entity.getPowerProduction() - consumes.getPower().usage, 0) * 60 * entity.timeScale, 1)),
                () -> Pal.powerBar,
                () -> entity.productionEfficiency));
    }

    public class olCrafterBuild extends GenericCrafterBuild{
        public float speed, totalActivity, generateTime, warmup;
        public float productionEfficiency = 0.0f;

        @Override
        public void updateTile(){
            float s = getSpeed();
            if(consumes.has(ConsumeType.item) && consumes.getItem().valid(this) || !consumes.has(ConsumeType.item)){
                progress += getProgressIncrease(craftTime) * s;
                totalProgress += delta() * s;
            }
            totalActivity += delta() * s;

            if(Mathf.chanceDelta(updateEffectChance * s)){
                updateEffect.at(x + Mathf.range(size * 4f), y + Mathf.range(size * 4));
            }

            if(consValid()){
                warmup = Mathf.approachDelta(warmup, 1f, warmupSpeed);
                float e = efficiency();
                if(speed <= e){
                    speed = Mathf.approachDelta(speed, e, warmupSpeed * e);
                }else{
                    speed = Mathf.approachDelta(speed, e, deWarmupSpeed);
                }
            }else{
                warmup = Mathf.approachDelta(warmup, 0f, warmupSpeed);
                speed = Mathf.approachDelta(speed, 0f, deWarmupSpeed);
            }

            if(progress >= 1f){
                craft();
                onCraft.get(this);
            }

            if(consValid() && power.status >= 0.99f){
                boolean prevOut = getPowerProduction() <= consumes.getPower().requestedPower(this);

                warmup = Mathf.lerpDelta(warmup, 1f, warmupSpeed * timeScale);
                if(Mathf.equal(warmup, 1f, 0.001f)){
                    warmup = 1f;
                }

                if(!prevOut && (getPowerProduction() > consumes.getPower().requestedPower(this))){
                    Events.fire(EventType.Trigger.impactPower);
                }

                if(timer(timerUse, itemCapacity / timeScale)){
                    consume();
                }
            }else{
                warmup = Mathf.lerpDelta(warmup, 0f, 0.01f);
            }

            dumpOutputs();
            productionEfficiency = Mathf.pow(warmup, 5f);
        }

        public float getSpeed(){
            return interp.apply(speed);
        }

        @Override
        public void onDestroyed(){
            super.onDestroyed();

            if(warmup < 0.3f || !state.rules.reactorExplosions) return;

            Sounds.explosionbig.at(this);

            Damage.damage(x, y, explosionRadius * tilesize, explosionDamage * 4);

            Effect.shake(6f, 16f, x, y);
            explodeEffect.at(x, y);
        }

        @Override
        public float getPowerProduction(){
            return powerProduction * productionEfficiency;
        }

        @Override
        public byte version(){
            return 1;
        }

        @Override
        public float getProgressIncrease(float baseTime){
            return 1f / baseTime * delta();
        }

        @Override
        public double sense(LAccess sensor){
            if(sensor == LAccess.heat) return warmup;
            return super.sense(sensor);
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.f(warmup);
            write.f(speed);
            write.f(productionEfficiency);
            write.f(generateTime);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            warmup = read.f();
            speed = read.f();
            productionEfficiency = read.f();
            if(revision >= 1) {
                generateTime = read.f();
            }
        }
    }
}