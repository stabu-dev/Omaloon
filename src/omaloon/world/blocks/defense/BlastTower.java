package omaloon.world.blocks.defense;

import arc.audio.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.meta.*;
import omaloon.content.*;

import static arc.Core.*;
import static mindustry.Vars.*;

public class BlastTower extends Block {
    public float shake = 3f;
    public float range = 110f;
    public float reload = 60f * 1.5f;
    public float chargeTime = 60f * 1.5f;
    public float damage = 60f;
    public StatusEffect status = StatusEffects.unmoving;
    public float statusDuration = 60f;
    public boolean targetAir = false;
    public boolean targetGround = true;
    public Color hitColor = Pal.accent;
    public Effect hitEffect = Fx.none;
    public Color waveColor = Color.white;
    public Effect waveEffect = Fx.dynamicWave;
    public Sound shootSound = OlSounds.hammer;

    public TextureRegion hammerRegion;

    public BlastTower(String name){
        super(name);
        update = true;
        solid = true;
    }

    @Override
    public void load(){
        super.load();
        hammerRegion = atlas.find(name + "-hammer");
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.add(Stat.targetsAir, targetAir);
        stats.add(Stat.targetsGround, targetGround);
        stats.add(Stat.damage, damage, StatUnit.none);
        stats.add(Stat.range, range / tilesize, StatUnit.blocks);
        stats.add(Stat.reload, 60f / reload, StatUnit.perSecond);
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list) {
        super.drawPlanRegion(plan, list);
        Draw.rect(hammerRegion, plan.drawx(), plan.drawy());
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid){
        super.drawPlace(x, y, rotation, valid);

        Drawf.dashCircle(x * tilesize + offset, y * tilesize + offset, range, Pal.accent);
    }

    @Override
    public TextureRegion[] icons(){
        return new TextureRegion[]{region, hammerRegion};
    }

    public class BlastTowerBuild extends Building {
        public float smoothProgress = 0f;
        public float charge;
        public float lastShootTime = -reload;
        public boolean targetAcquired = false;
        public Teamc currentTarget = null;

        @Override
        public void updateTile() {
            Teamc target = Units.closestTarget(team, x, y, range,
                    unit -> unit.checkTarget(targetAir, targetGround));

            if (target != null && !targetAcquired) {
                targetAcquired = true;
                currentTarget = target;
            } else if (target == null) {
                targetAcquired = false;
                currentTarget = null;
            }

            if (targetAcquired) {
                smoothProgress = Mathf.approach(smoothProgress, 1f, Time.delta / chargeTime);

                if (efficiency > 0 && (charge += Time.delta) >= reload && smoothProgress >= 0.99f) {
                    shoot(currentTarget);
                    charge = 0f;
                }
            } else {
                smoothProgress = Mathf.approach(smoothProgress, 0f, Time.delta / chargeTime);
            }
        }

        public void shoot(Teamc target) {
            lastShootTime = Time.time;
            Effect.shake(shake, shake, this);
            shootSound.at(this);
            waveEffect.layer(Layer.blockUnder).at(x, y, range, waveColor);
            tile.getLinkedTiles(t -> OlFx.hammerHit.layer(Layer.blockUnder).at(
                    t.worldx(), t.worldy(),
                    angleTo(t.worldx(), t.worldy()) + Mathf.range(360f),
                    Tmp.c1.set(t.floor().mapColor).mul(1.5f + Mathf.range(0.15f)))
            );
            hitEffect.at(target.x(), target.y(), hitColor);

            if (target instanceof Healthc h) {
                h.damage(damage);
            }

            if (target instanceof Statusc s) {
                s.apply(status, statusDuration);
            }

            smoothProgress = 0f;
        }

        @Override
        public void draw() {
            Draw.rect(region, x, y);

            float fract = Mathf.clamp(smoothProgress, 0.25f, 0.3f);
            Draw.color(Pal.shadow, Pal.shadow.a);
            Draw.rect(hammerRegion, x - (fract - 0.25f) * 40, y - (fract - 0.25f) * 40, hammerRegion.width * fract, hammerRegion.width * fract);
            Draw.color();
            Draw.z(Layer.blockAdditive);
            Draw.rect(hammerRegion, x, y, hammerRegion.width * fract, hammerRegion.height * fract);
        }

        @Override
        public void drawSelect(){
            Drawf.dashCircle(x, y, range, Pal.accent);
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.f(lastShootTime);
            write.f(smoothProgress);
            write.f(charge);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            lastShootTime = read.f();
            smoothProgress = read.f();
            charge = read.f();
        }
    }
}
