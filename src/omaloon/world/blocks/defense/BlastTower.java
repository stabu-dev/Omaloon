package omaloon.world.blocks.defense;

import arc.audio.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import asmlib.annotations.DebugAST;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.consumers.*;
import mindustry.world.meta.*;
import omaloon.annotations.AutoImplement;
import omaloon.annotations.Load;
import omaloon.content.*;
import omaloon.world.interfaces.*;
import omaloon.world.meta.*;
import omaloon.world.modules.*;

import static arc.Core.*;
import static mindustry.Vars.*;

public class BlastTower extends Block {
    public PressureConfig pressureConfig = new PressureConfig();
    public boolean useConsumerMultiplier = true;
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
    public Effect hitEffect = Fx.hitBulletColor;
    public Color waveColor = Color.white;
    public Effect waveEffect = Fx.dynamicWave;
    public Sound shootSound = OlSounds.hammer;
    @Load("@-hammer")
    public TextureRegion hammerRegion;

    public BlastTower(String name){
        super(name);
        update = true;
        solid = true;
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.add(Stat.targetsAir, targetAir);
        stats.add(Stat.targetsGround, targetGround);
        stats.add(Stat.damage, damage, StatUnit.none);
        stats.add(Stat.range, range / tilesize, StatUnit.blocks);
        stats.add(Stat.reload, 60f / reload, StatUnit.perSecond);
        pressureConfig.addStats(stats);
    }

    @Override
    public void setBars() {
        super.setBars();
        pressureConfig.addBars(this);
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

    public class BlastTowerBuild extends Building implements HasPressureImpl {
        public float smoothProgress = 0f;
        public float charge;
        public float lastShootTime = -reload;
        public Seq<Teamc> targets = new Seq<>();

        public float efficiencyMultiplier() {
            float val = 1f;
            if (!useConsumerMultiplier) return val;
            for (Consume consumer : consumers) {
                val *= consumer.efficiencyMultiplier(this);
            }
            return val;
        }

        @Override
        public void updateTile() {
            updatePressure();
            dumpPressure();
            super.updateTile();

            targets.clear();
            Units.nearbyEnemies(team, x, y, range, u -> {
                if(u.checkTarget(targetAir, targetGround)) {
                    targets.add(u);
                }
            });

            indexer.allBuildings(x, y, range, b -> {
                if(b.team != team){
                    targets.add(b);
                }
            });

            float effMultiplier = efficiencyMultiplier();

            if (targets.size > 0 && canConsume()) {
                smoothProgress = Mathf.approach(smoothProgress, 1f, Time.delta / chargeTime * effMultiplier);

                if (efficiency > 0 && (charge += Time.delta * effMultiplier) >= reload && smoothProgress >= 0.99f) {
                    shoot();
                    charge = 0f;
                }
            } else {
                smoothProgress = Mathf.approach(smoothProgress, 0f, Time.delta / chargeTime * effMultiplier);
            }
        }

        public void shoot() {
            if (!canConsume()) return;

            consume();
            lastShootTime = Time.time;
            Effect.shake(shake, shake, this);
            shootSound.at(this);
            waveEffect.layer(Layer.blockUnder).at(x, y, range, waveColor);
            tile.getLinkedTiles(t -> OlFx.hammerHit.layer(Layer.blockUnder).at(
                t.worldx(), t.worldy(),
                angleTo(t.worldx(), t.worldy()) + Mathf.range(360f),
                Tmp.c1.set(t.floor().mapColor).mul(1.5f + Mathf.range(0.15f)))
            );

            float damageMultiplier = efficiencyMultiplier();
            for (Teamc target : targets) {
                hitEffect.at(target.x(), target.y(), hitColor);
                if(target instanceof Healthc){
                    ((Healthc)target).damage(damage * damageMultiplier);
                }
                if(target instanceof Statusc){
                    ((Statusc)target).apply(status, statusDuration);
                }
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
        @DebugAST
        public void write(Writes write) {
            super.write(write);
            write.f(lastShootTime);
            write.f(smoothProgress);
            write.f(charge);
            AutoImplement.Util.Inject(HasPressureImpl.class);
        }

        @Override
        @DebugAST
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            lastShootTime = read.f();
            smoothProgress = read.f();
            charge = read.f();
            AutoImplement.Util.Inject(HasPressureImpl.class);
        }
    }
}