package omaloon.world.blocks.defense;

import arc.*;
import arc.audio.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.gl.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import arclibrary.graphics.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.units.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.consumers.*;
import mindustry.world.meta.*;
import omaloon.annotations.*;
import omaloon.content.*;
import omaloon.core.*;
import omaloon.utils.*;
import omaloon.world.interfaces.*;
import omaloon.world.meta.*;

import static omaloon.OmaloonMod.*;

public class Shelter extends Block{
    @Load(value = "@-base", fallback = {"block-@size"})
    public TextureRegion baseRegion;
    @Load("@-glow")
    public TextureRegion glowRegion;

    public PressureConfig pressureConfig = new PressureConfig();

//    public float shieldAngle = 120f;
    public float shieldHealth = 120f;
    public float shieldRange = 80f;
    public float rechargeStandard = 0.1f;
    public float rechargeBroken = 1f;
    public float warmupTime = 0.1f;
    public boolean useConsumerMultiplier = true;
    public float rotateSpeed = 1;

    public float glowMinAlpha = 0f, glowMaxAlpha = 0.5f, glowBlinkSpeed = 0.16f;

    public Color deflectColor = Pal.heal;

    public Effect hitEffect = Fx.absorb;
    public Sound hitSound = Sounds.none;
    public float hitSoundVolume = 1f;

    public Effect rotateEffect = OlFx.shelterRotate;

    private static final FrameBuffer fieldBuffer = new FrameBuffer();
    public static final Seq<Runnable> runs = new Seq<>();

    {
        Events.run(EventType.Trigger.draw, () -> {
            fieldBuffer.resize(Core.graphics.getWidth(), Core.graphics.getHeight());
            Seq<Runnable> buffer = runs.copy();
            runs.clear();

            Draw.draw(Layer.shields, () -> {
                Draw.flush();
                fieldBuffer.begin(Color.clear);
                buffer.each(Runnable::run);
                fieldBuffer.end();
                Draw.color(deflectColor, Vars.renderer.animateShields ? 1f : OlSettings.shieldOpacity.get() / 100f);
                EDraw.drawBuffer(fieldBuffer);
                Draw.flush();
                Draw.color();
            });
        });
    }

    public Shelter(String name){
        super(name);
        update = true;
        solid = true;
        saveConfig = true;
        group = BlockGroup.projectors;
        ambientSound = Sounds.shield;
        ambientSoundVolume = 0.08f;
        pressureConfig.isWhitelist = true;
    }

    @Override
    public TextureRegion[] icons(){
        return new TextureRegion[]{baseRegion, region};
    }

    @Override
    public void init(){
        super.init();
        clipSize = shieldRange * 2f;
    }

    @Override
    public void setStats(){
        super.setStats();
        pressureConfig.addStats(stats);

        stats.add(Stat.shieldHealth, shieldHealth, StatUnit.none);
        stats.add(Stat.cooldownTime, (int)(rechargeStandard * 60f), StatUnit.perSecond);
    }

    @Override
    public void setBars(){
        super.setBars();
        pressureConfig.addBars(this);
        addBar("shield", (ShelterBuild entity) -> new Bar("stat.shieldhealth", Pal.accent, () -> entity.broken ? 0f : 1 - entity.shieldDamage / shieldHealth).blink(Color.white));
    }

    @Override
    public void drawOverlay(float x, float y, int rotation){
        Drawf.dashCircle(x, y, shieldRange, Pal.accent);
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list){
        Draw.rect(baseRegion, plan.drawx(), plan.drawy());
        float rot = plan.config instanceof Float ? (float)plan.config : 0;
        Draw.rect(region, plan.drawx(), plan.drawy(), rot - 90);
    }

    public class ShelterBuild extends Building implements HasPressureImpl{
        public float rot = 90;
        public float shieldAngle = 0;
        public float shieldDamage = 0;
        public float warmup = 0;
        public boolean broken = false;

        @Override
        public void draw(){
            Draw.rect(baseRegion, x, y, 0);
            Draw.rect(region, x, y, rot - 90);

            float z = Draw.z();
            Draw.z(Layer.blockAdditive);
            Draw.blend(Blending.additive);
            Draw.color(Color.valueOf("cbffc2"));

            float cycleAlpha = glowMinAlpha + (glowMaxAlpha - glowMinAlpha) * (0.5f + 0.5f * Mathf.sin(Time.time * glowBlinkSpeed));
            Draw.alpha(warmup * cycleAlpha);
            Draw.rect(glowRegion, x, y, rot - 90);
            Draw.reset();
            Draw.blend();
            Draw.z(z);

            runs.add(() -> {
                Draw.color();
                Fill.circle(x, y, warmup * (hitSize() * 1.2f));
                Fill.arc(x, y, shieldRange * warmup, shieldAngle / 360f, -shieldAngle / 2f + rot);
                Draw.color();
            });
        }

        @Override
        public float edelta(){
            return super.edelta() * efficiencyMultiplier();
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
        public boolean onConfigureTapped(float x, float y){
            configure(Tmp.v1.set(this.x, this.y).angleTo(x, y));
            rotateEffect.at(this.x, this.y, rot, block);
            return false;
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            rot = read.f();
            shieldDamage = read.f();
            warmup = read.f();
            broken = read.bool();
        }

        public void updatePositioning(){
            var builds = team.data().buildings.select(b -> dst(b) <= shieldAngle * warmup + shieldBuffer);

            Tmp.v1.setZero();
            builds.each(Tmp.v1::add);
            rot = angleTo(Tmp.v1);

            shieldAngle = OlUtils.angleDistSigned(rot, angleTo(builds.max(b -> OlUtils.angleDistSigned(rot, angleTo(b))))) -
            OlUtils.angleDistSigned(rot, angleTo(builds.min(b -> OlUtils.angleDistSigned(rot, angleTo(b)))));
        }

        @Override
        public void updateTile(){
            updatePressure();
            updatePositioning();
            if(efficiency > 0){
                if(shieldDamage >= 0){
                    shieldDamage -= edelta() * (broken ? rechargeBroken : rechargeStandard);
                }else{
                    broken = false;
                }

                if(broken){
                    warmup = Mathf.approachDelta(warmup, 0f, warmupTime);
                }else{
                    warmup = Mathf.approachDelta(warmup, efficiency * efficiencyMultiplier(), warmupTime);

                    float radius = shieldRange * warmup + shieldBuffer;

                    Groups.bullet.intersect(
                        x - radius,
                        y - radius,
                        radius * 2f,
                        radius * 2f,
                        b -> {
                            if(b.team == Team.derelict){
                                float distance = Mathf.dst(x, y, b.x, b.y);
                                float angle = Math.abs(((b.angleTo(x, y) - rot) % 360f + 360f) % 360f - 180f);
                                boolean inWarmupRadius = distance <= warmup * (hitSize() * 1.4f);

                                if((distance <= shieldRange * warmup + b.type.splashDamageRadius && angle <= shieldAngle / 2f) || inWarmupRadius){
                                    b.absorb();
                                    hitEffect.at(b.x, b.y, b.hitSize);
                                    hitSound.at(b.x, b.y, Mathf.random(0.9f, 1.1f), hitSoundVolume);
                                    shieldDamage += b.damage;
                                    if(shieldDamage >= shieldHealth) broken = true;
                                }
                            }
                        }
                    );
                }
            }else{
                warmup = Mathf.approachDelta(warmup, 0f, warmupTime);
            }
        }

        @Override
        public byte version(){
            return 1;
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.f(rot);
            write.f(shieldDamage);
            write.f(warmup);
            write.bool(broken);
        }
    }
}
