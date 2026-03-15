package omaloon.world.blocks.defense;

import arc.audio.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.units.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.logic.*;
import mindustry.ui.*;
import mindustry.world.consumers.*;
import mindustry.world.draw.*;
import mindustry.world.meta.*;
import omaloon.annotations.Annotations.*;
import omaloon.utils.*;
import omaloon.world.*;

public class Shelter extends GenericPressureBlock{
    private static final Seq<Building> sharedBuildings = new Seq<>();
    private final int retargetTimer = timers++;

    public float range = 80f;
    public float minRange = 12f;
    public float retargetTime = 10f;
    public float rotateSpeed = 1f;
    public float growSpeed = 1f;
    public float warmupSpeed = 0.014f;
    public Color arcColor = Pal.heal;
    public Color glowColor = Pal.heal;
    public float glowScl = 2.84f, glowMag = 0.02f;

    public float shieldHealth = 100;
    public float shieldHeal = 0.1f;

    public Sound startSound = Sounds.none;
    public float startSoundVolume = 0.05f;

    public boolean scaleEfficiency = true;

    public Effect shieldHealEffect = Fx.none, shieldBreakEffect = Fx.none;

    public @Load("@-glow") TextureRegion glowRegion;

    public DrawBlock drawer = new DrawDefault();

    public Shelter(String name){
        super(name);
        update = rotate = true;
        quickRotate = drawArrow = false;
    }

    @Override
    public void load(){
        super.load();
        drawer.load(this);
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list){
        drawer.drawPlan(this, plan, list);
        Draw.rect(region, plan.drawx(), plan.drawy(), plan.rotation * 90 - 90);
    }

    @Override
    public void drawOverlay(float x, float y, int rotation){
        Drawf.dashCircle(x, y, range, Pal.placing);
    }

    @Override
    protected TextureRegion[] icons(){
        return Structs.add(drawer.icons(this), region);
    }

    @Override
    public void init(){
        super.init();
        updateClipRadius(range + 20f);
    }

    @Override
    public void setBars(){
        super.setBars();
        addBar("shield", (ShelterBuild b) -> new Bar("stat.shieldhealth", Pal.accent, () -> b.broken ? 0f : b.shield / shieldHealth).blink(Color.white));
    }

    @Override
    public void setStats(){
        super.setStats();
        stats.add(Stat.shieldHealth, shieldHealth);
        stats.add(Stat.regenerationRate, shieldHeal * 60f, StatUnit.seconds);
    }

    public class ShelterBuild extends GenericPressureBlockBuild{
        public float shield = shieldHealth;
        public boolean broken = false;

        public float warmup, targetRotation, currentRotation, targetArcLength, currentArcLength;
        public float targetLRadius, currentLRadius, targetRRadius, currentRRadius;
        public Seq<Building> myBuildings = new Seq<>();

        @Override
        public void created(){
            super.created();
            currentRotation = targetRotation = rotation * 90;
        }

        @Override
        public float warmup(){
            return warmup;
        }

        @Override
        public void draw(){
            drawer.draw(this);
            float headRotation = currentRotation + currentArcLength / 2f - 90f;
            Draw.rect(region, x, y, headRotation);
            if(glowRegion.found() && warmup > 0.001f){
                float glowAlpha = Mathf.absin(Time.time, glowScl, glowMag) * warmup;
                Drawf.additive(glowRegion, glowColor, glowAlpha, x, y, headRotation, Layer.blockAdditive);
            }
            drawArc();
        }

        public void drawArc(){
            //Draw.z(OlShaders.shelterShieldLayer);
            Draw.z(Layer.shields);
            Draw.color(arcColor);
            Fill.circle(x, y, minRange * warmup * efficiency);
            float ov = 4f;
            if(currentArcLength > 0.01f) Fill.arc(x, y, range * warmup * efficiency, (currentArcLength + ov) / 360f, currentRotation - ov / 2f);
            if(currentLRadius > 0.01f) Fill.arc(x, y, currentLRadius * warmup, (90f + ov) / 360f, currentRotation + currentArcLength - ov / 2f);
            if(currentRRadius > 0.01f) Fill.arc(x, y, currentRRadius * warmup, (90f + ov) / 360f, currentRotation - 90f - ov / 2f);
        }

        @Override
        public void drawSelect() {
            Drawf.dashCircle(x, y, efficiency > 0 ? range * efficiency : range, Pal.placing);
        }

        @Override
        public float efficiencyScale() {
            float mul = 1f;
            if (scaleEfficiency) for(Consume cons : consumers) {
                mul *= cons.efficiencyMultiplier(this);
            }
            return mul;
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            warmup = read.f();
            targetRotation = read.f();
            currentRotation = read.f();
            targetArcLength = read.f();
            currentArcLength = read.f();
            targetLRadius = read.f();
            currentLRadius = read.f();
            targetRRadius = read.f();
            currentRRadius = read.f();

            shield = read.f();
            broken = read.bool();
        }

        public void retarget(){
            sharedBuildings.clear();
            team.data().buildingTree.intersect(x - range, y - range, range * 2, range * 2, sharedBuildings);

            Vec2 tPos = Tmp.v1.setZero();
            int weight = 0;
            myBuildings.clear();

            for(Building b : sharedBuildings){
                if(b == this || b.dst(this) > range) continue;
                int s = b.block.size * b.block.size;
                tPos.add(b.x * s, b.y * s);
                weight += s;
                myBuildings.add(b);
            }

            if(weight == 0){
                targetArcLength = targetLRadius = targetRRadius = 0;
                return;
            }

            float base = tPos.scl(1f / weight).sub(this).angle(), min = 0, max = 0;
            boolean first = true;

            for(Building b : myBuildings){
                float size = Mathf.sqrt2 * b.block.size * 8f;
                for(int s = 0; s < 4; s++){
                    float d = OlUtils.angleDistSigned(base, angleTo(Tmp.v3.trns(s * 90 - 45, size).add(b.x, b.y)));
                    if(first){
                        min = max = d;
                        first = false;
                    }else{
                        min = Math.min(min, d);
                        max = Math.max(max, d);
                    }
                }
            }

            targetArcLength = Math.min(180f, max - min);
            targetRotation = base + (min + max) / 2f - targetArcLength / 2f;
        }

        @Override
        public double sense(LAccess sensor){
            if(sensor == LAccess.shield) return broken ? 0f : shield;
            return super.sense(sensor);
        }

        @Override
        public boolean shouldConsume(){
            return super.shouldConsume() && (targetArcLength > 0 || targetLRadius > 0 || targetRRadius > 0);
        }

        @Override
        public void updateTile(){
            if(timer(retargetTimer, retargetTime)) retarget();

            if(efficiency > 0){
                boolean wasWorking = warmup > 0f;
                warmup = Mathf.approach(warmup, broken ? 0f : 1f, warmupSpeed * edelta());
                if(!wasWorking && warmup > 0f && !broken) startSound.at(x, y, 1f, startSoundVolume);
                currentRotation = Angles.moveToward(currentRotation, targetRotation, rotateSpeed * edelta());
                currentArcLength = Mathf.approach(currentArcLength, targetArcLength, growSpeed * edelta());

                shield = Mathf.approachDelta(shield, shieldHealth, shieldHeal);

                if(shield == shieldHealth && broken){
                    broken = false;
                    shieldHealEffect.at(x, y);
                }

                float nextL = 0, nextR = 0, center = currentRotation + currentArcLength / 2f;
                for(Building b : myBuildings){
                    if(!b.isValid() || b.dst(this) > range * efficiency) continue;
                    float size = Mathf.sqrt2 * b.block.size * 8f;
                    for(int s = 0; s < 4; s++){
                        Vec2 corner = Tmp.v3.trns(s * 90 - 45, size).add(b.x, b.y);
                        float d = dst(corner), rel = OlUtils.angleDistSigned(center, angleTo(corner));
                        if(rel > currentArcLength / 2f + 1f && rel < currentArcLength / 2f + 91f) nextR = Math.max(nextR, Math.min(d, range * efficiency));
                        else if(rel < -currentArcLength / 2f - 1f && rel > -currentArcLength / 2f - 91f) nextL = Math.max(nextL, Math.min(d, range * efficiency));
                    }
                }

                targetLRadius = nextL;
                targetRRadius = nextR;
                float step = growSpeed * 6f * edelta();
                currentLRadius = Mathf.approach(currentLRadius, targetLRadius, step);
                currentRRadius = Mathf.approach(currentRRadius, targetRRadius, step);

                Groups.bullet.intersect(x - range, y - range, range * 2, range * 2, b -> {
                    if(b.team != Team.derelict) return;
                    float bRel = OlUtils.angleDistSigned(center, angleTo(b)), d = dst(b);
                    if(d < minRange * efficiency || (currentArcLength > 0.01f && d < range * efficiency && Math.abs(bRel) < currentArcLength / 2f) ||
                    (currentRRadius > 0.01f && d < currentRRadius && bRel > currentArcLength / 2f && bRel < currentArcLength / 2f + 90f) ||
                    (currentLRadius > 0.01f && d < currentLRadius && bRel < -currentArcLength / 2f && bRel > -currentArcLength / 2f - 90f)){
                        shield -= b.damage;
                        if(shield > 0){
                            b.absorb();
                        }else{
                            broken = true;
                            b.damage = -shield;
                            shield = 0f;
                            shieldBreakEffect.at(x, y);
                        }
                    }
                });
            }else warmup = Mathf.approachDelta(warmup, 0f, warmupSpeed);
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.f(warmup);
            write.f(targetRotation);
            write.f(currentRotation);
            write.f(targetArcLength);
            write.f(currentArcLength);
            write.f(targetLRadius);
            write.f(currentLRadius);
            write.f(targetRRadius);
            write.f(currentRRadius);

            write.f(shield);
            write.bool(broken);
        }
    }
}