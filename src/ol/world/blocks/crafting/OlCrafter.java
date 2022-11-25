package ol.world.blocks.crafting;

import arc.Core;
import arc.Events;
import arc.func.Cons;
import arc.math.Interp;
import arc.math.Mathf;
import arc.struct.EnumSet;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.entities.Damage;
import mindustry.entities.Effect;
import mindustry.game.EventType;
import mindustry.gen.Sounds;
import mindustry.graphics.Pal;
import mindustry.logic.LAccess;
import mindustry.ui.Bar;
import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.consumers.ConsumeItems;
import mindustry.world.meta.BlockFlag;
import mindustry.world.meta.Env;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import ol.content.OlFx;
import ol.graphics.OlPal;

import static mindustry.Vars.state;
import static mindustry.Vars.tilesize;

public class OlCrafter extends GenericCrafter{
    public float accelerationSpeed = 0.03f, decelerationSpeed = 0.05f;
    public Interp interp = Interp.smoother;
    public float powerProduction;
    public int explosionRadius = 18;
    public int explosionDamage = 1400;
    public Effect explodeEffect = OlFx.olCentryfugeExplosion;
    public Stat generationType = Stat.basePowerGeneration;

    public Cons<OlCrafterBuild> onCraft = tile -> {};

    public OlCrafter(String name){
        super(name);
        hasPower = true;
        hasLiquids = true;
        liquidCapacity = 30f;
        hasItems = true;
        outputsPower = consumesPower = true;
        flags = EnumSet.of(BlockFlag.factory, BlockFlag.generator);
        lightRadius = 115f;
        emitLight = true;
        envEnabled = Env.any;
    }

    @Override
    public void setStats(){
        super.setStats();

        if(hasItems){
            stats.add(generationType, powerProduction * 60.0f, StatUnit.powerSecond);
        }
    }

    @Override
    public void setBars(){
        super.setBars();

        addBar("acceleration", (OlCrafterBuild entity) -> new Bar(() ->
                Core.bundle.format("bar.acceleration",
                        Mathf.round(entity.getAcceleration() * 100f)),
                ()-> OlPal.oLBlue.cpy().lerp(Pal.lightOrange, entity.getAcceleration() / 1.2f), entity::getAcceleration)
        );
    }

    public class OlCrafterBuild extends GenericCrafterBuild {
        public float acceleration, totalActivity;
        public float generateTime;
        public float productionEfficiency = 0.0f;

        @Override
        public void updateTile() {
            boolean prevOut = getPowerProduction() <= consPower.requestedPower(this);
            float s = getAcceleration();
            if (hasItems()) {
                progress += getProgressIncrease(craftTime) * s;
                totalProgress += delta() * s;
            }
            totalActivity += delta() * s;

            if (Mathf.chanceDelta(updateEffectChance * s)) {
                updateEffect.at(x + Mathf.range(size * 4f), y + Mathf.range(size * 4));
            }

            if(!prevOut && (getPowerProduction() > consPower.requestedPower(this))){
                Events.fire(EventType.Trigger.impactPower);
            }

            if (canConsume()) {
                warmup = Mathf.approachDelta(warmup, 1f, warmupSpeed);
                float e = efficiency;
                if (acceleration <= e) {
                    acceleration = Mathf.approachDelta(acceleration, e, accelerationSpeed * e);
                } else {
                    acceleration = Mathf.approachDelta(acceleration, e, decelerationSpeed);
                }
            } else {
                warmup = Mathf.approachDelta(warmup, 0f, warmupSpeed);
                acceleration = Mathf.approachDelta(acceleration, 0f, decelerationSpeed);
            }

            if (progress >= 1f) {
                craft();
                onCraft.get(this);
            }

            totalProgress += acceleration * Time.delta;

            productionEfficiency = Mathf.pow(acceleration, 5f);

            dumpOutputs();
        }

        public float getDisplayAcceleration() {
            return hasItems() ? getAcceleration() : 0f;
        }

        @Override
        public double sense(LAccess sensor){
            if(sensor == LAccess.heat) return acceleration;
            return super.sense(sensor);
        }

        @Override
        public float ambientVolume(){
            return acceleration / 1.5f;
        }

        public float getAcceleration() {
            return interp.apply(acceleration);
        }

        @Override
        public float getProgressIncrease(float baseTime) {
            return 1f / baseTime * delta();
        }

        public boolean hasItems() {
            ConsumeItems cItems = findConsumer(c -> c instanceof ConsumeItems);
            return cItems == null || cItems.efficiency(this) == 1;
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
        public float getPowerProduction() {
            return powerProduction * productionEfficiency;
        }

        @Override
        public byte version() {
            return 1;
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.f(acceleration);
            write.f(productionEfficiency);
            write.f(generateTime);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            acceleration = read.f();
            productionEfficiency = read.f();
            if (revision >= 1) {
                generateTime = read.f();
            }
        }
    }
}
