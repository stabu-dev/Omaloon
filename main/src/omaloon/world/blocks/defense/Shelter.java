package omaloon.world.blocks.defense;

import arc.*;
import arc.audio.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.units.*;
import mindustry.game.*;
import mindustry.game.EventType.*;
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
    public static final float lobeHalfSpan = 45f, fieldOverlap = 4f;
    public static final float maxCrossStep = 40f, crossCooldown = 30f;

    public static final Seq<ShelterBuild> fields = new Seq<>();

    static{
        Events.on(ResetEvent.class, e -> fields.clear());
    }
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
        update = rotate = solid = true;
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
    public void setBars(){
        super.setBars();
        addBar("shield", (ShelterBuild b) -> new Bar("stat.shieldhealth", Pal.accent, () -> Mathf.clamp(b.shield / shieldHealth)).blink(Color.white));
    }

    @Override
    public void setStats(){
        super.setStats();
        stats.add(Stat.shieldHealth, shieldHealth);
        stats.add(Stat.regenerationRate, shieldHeal * 60f, StatUnit.seconds);
    }

    public class ShelterBuild extends GenericPressureBlockBuild{
        /** Floats per impact in {@link #hits}: x, y, start time, strength, ring reach, duration. */
        public static final int hitStride = 6;

        public float shield = shieldHealth;
        public boolean broken = false;

        public float warmup, targetRotation, currentRotation, targetArcLength, currentArcLength;
        public float targetLRadius, currentLRadius, targetRRadius, currentRRadius;
        public float visualScale;
        public float lastCoverageRange = -1f;
        public Seq<Building> myBuildings = new Seq<>();

        public FloatSeq hits = new FloatSeq();
        public float spin, wobble;
        public long drawnFrame = -1;
        public IntFloatMap crossed = new IntFloatMap();

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
        }

        public float fieldInnerRadius(){
            return minRange * visualScale;
        }

        public float fieldOuterRadius(){
            return currentArcLength > 0.01f ? range * visualScale : 0f;
        }

        public float fieldLobeRadius(float current){
            return current > 0.01f ? current * warmup : 0f;
        }

        public float fieldMaxRadius(){
            return Math.max(Math.max(fieldInnerRadius(), fieldOuterRadius()), Math.max(fieldLobeRadius(currentLRadius), fieldLobeRadius(currentRRadius)));
        }

        public void hit(float hx, float hy, float damage, float size){
            float root = Mathf.sqrt(Math.max(damage, 0f));
            float strength = Mathf.clamp(root / Mathf.sqrt(20f), 0.15f, 3f);
            float reach = Mathf.clamp(10f + size * 1.5f + root * 4f, 10f, 120f);
            wobble = Math.max(wobble, Math.min(wobble + strength * 0.25f, 0.8f));

            pruneHits();
            hits.add(hx, hy, Time.time, strength);
            hits.add(reach, 30f + strength * 15f);
        }

        public boolean covers(float px, float py){
            float half = currentArcLength / 2f, ov = fieldOverlap / 2f, lobe = lobeHalfSpan * 2f;
            float rel = OlUtils.angleDistSigned(currentRotation + half, angleTo(px, py)), d = dst(px, py);
            float outer = fieldOuterRadius(), lr = fieldLobeRadius(currentLRadius), rr = fieldLobeRadius(currentRRadius);
            return d < fieldInnerRadius() || (outer > 0f && d < outer && Math.abs(rel) < half + ov) ||
                (rr > 0f && d < rr && rel > half - ov && rel < half + ov + lobe) ||
                (lr > 0f && d < lr && rel < -half + ov && rel > -half - ov - lobe);
        }

        public void cross(Hitboxc e, float strength){
            float dx = e.deltaX(), dy = e.deltaY();
            //a fresh entity's first delta spans from the world's origin, and a teleport is no crossing either
            if((dx == 0f && dy == 0f) || dx * dx + dy * dy > maxCrossStep * maxCrossStep) return;

            float px = e.x(), py = e.y(), qx = px - dx, qy = py - dy;
            boolean in = covers(px, py);
            if(in == covers(qx, qy) || Time.time - crossed.get(e.id(), -999f) < crossCooldown) return;

            for(int i = 0; i < 8; i++){
                float mx = (px + qx) / 2f, my = (py + qy) / 2f;
                if(covers(mx, my) == in){
                    px = mx;
                    py = my;
                }else{
                    qx = mx;
                    qy = my;
                }
            }

            //an edge inside another field is a seam, not an edge!!!
            px = (px + qx) / 2f;
            py = (py + qy) / 2f;
            for(ShelterBuild other : fields){
                if(other != this && other.isAdded() && other.covers(px, py)) return;
            }

            crossed.put(e.id(), Time.time);
            wobble = Math.min(wobble + strength, 3f);
            pruneHits();
            hits.add(px, py, Time.time, strength * 0.6f);
            hits.add(Mathf.clamp(14f + e.hitSize() * 1.5f, 14f, 80f), 40f + strength * 10f);
        }

        public void pruneHits(){
            int kept = 0;
            for(int i = 0; i + hitStride - 1 < hits.size; i += hitStride){
                if(Time.time - hits.items[i + 2] >= hits.items[i + 5]) continue;
                if(kept != i) System.arraycopy(hits.items, i, hits.items, kept, hitStride);
                kept += hitStride;
            }
            hits.size = kept;
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

        public void retarget(float coverage){
            sharedBuildings.clear();
            team.data().buildingTree.intersect(x - coverage, y - coverage, coverage * 2, coverage * 2, sharedBuildings);

            Vec2 tPos = Tmp.v1.setZero();
            int weight = 0;
            myBuildings.clear();

            for(Building b : sharedBuildings){
                int s = b.block.size * b.block.size;
                float size = Mathf.sqrt2 * b.block.size * 8f;
                if(b == this || b.dst(this) > coverage + size) continue;
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
            float coverageScl = Math.max(efficiency, 1f), coverage = range * coverageScl;
            if(timer(retargetTimer, retargetTime) || !Mathf.equal(lastCoverageRange, coverage, Vars.tilesize)){
                retarget(coverage);
                lastCoverageRange = coverage;
            }

            float lastRotation = currentRotation, lastArc = currentArcLength, lastL = currentLRadius, lastR = currentRRadius;

            if(efficiency > 0){
                boolean wasWorking = warmup > 0f;
                warmup = Mathf.approach(warmup, broken ? 0f : 1f, warmupSpeed * edelta());
                if(!wasWorking && warmup > 0f && !broken) startSound.at(x, y, 1f, startSoundVolume);
                currentRotation = Angles.moveToward(currentRotation, targetRotation, rotateSpeed * edelta());
                currentArcLength = Mathf.approach(currentArcLength, targetArcLength, growSpeed * edelta());

                shield = Mathf.approachDelta(shield, shieldHealth, shieldHeal * coverageScl * coverageScl);

                if(shield >= shieldHealth && broken){
                    broken = false;
                    shieldHealEffect.at(x, y);
                }

                float nextL = 0, nextR = 0, center = currentRotation + currentArcLength / 2f;
                for(Building b : myBuildings){
                    float size = Mathf.sqrt2 * b.block.size * 8f;
                    if(!b.isValid() || b.dst(this) > coverage + size) continue;
                    for(int s = 0; s < 4; s++){
                        Vec2 corner = Tmp.v3.trns(s * 90 - 45, size).add(b.x, b.y);
                        float d = dst(corner), rel = OlUtils.angleDistSigned(center, angleTo(corner));
                        if(rel > currentArcLength / 2f + 1f && rel < currentArcLength / 2f + 91f) nextR = Math.max(nextR, Math.min(d, coverage));
                        else if(rel < -currentArcLength / 2f - 1f && rel > -currentArcLength / 2f - 91f) nextL = Math.max(nextL, Math.min(d, coverage));
                    }
                }

                targetLRadius = nextL;
                targetRRadius = nextR;
                float step = growSpeed * 6f * edelta();
                currentLRadius = Mathf.approach(currentLRadius, targetLRadius, step);
                currentRRadius = Mathf.approach(currentRRadius, targetRRadius, step);

                if(!broken){
                    Groups.bullet.intersect(x - coverage, y - coverage, coverage * 2, coverage * 2, b -> {
                        if(b.team != Team.derelict || !b.type.absorbable || b.absorbed || !b.isAdded()) return;
                        if(covers(b.x, b.y)){
                            float damage = b.type.shieldDamage(b);
                            shield -= damage;
                            hit(b.x, b.y, damage, b.hitSize);
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
                }
            }else{
                warmup = Mathf.approachDelta(warmup, 0f, warmupSpeed);
            }

            float turned = OlUtils.angleDistSigned(currentRotation, lastRotation);
            spin = Mathf.lerpDelta(spin, turned / Math.max(Time.delta, 0.001f), 0.15f);
            float pumped = Math.abs(turned) * 0.35f + Math.abs(currentArcLength - lastArc) * 0.25f
                + (Math.abs(currentLRadius - lastL) + Math.abs(currentRRadius - lastR)) * 0.08f;
            wobble = Mathf.approachDelta(Math.min(wobble + pumped, 3f), 0f, 0.025f);

            float targetVisualScale = warmup * efficiency;
            if(visualScale == 0f && targetVisualScale > 0f){
                visualScale = targetVisualScale;
            }else{
                visualScale = Mathf.approach(visualScale, targetVisualScale, warmupSpeed * 2f * Time.delta);
            }

            boolean up = visualScale > 0.001f;
            if(up != fields.contains(this, true)){
                if(up){
                    fields.add(this);
                }else{
                    fields.remove(this, true);
                }
            }

            if(!Vars.headless && up && Core.graphics.getFrameId() - drawnFrame <= 1){
                float reach = fieldMaxRadius() + 16f;
                Units.nearby(x - reach, y - reach, reach * 2f, reach * 2f, u ->
                    cross(u, Mathf.clamp(0.25f + u.hitSize / 25f + u.deltaLen() * 0.08f, 0.25f, 1.2f)));
                Groups.bullet.intersect(x - reach, y - reach, reach * 2f, reach * 2f, b -> {
                    if(b.team != Team.derelict || !b.type.absorbable) cross(b, Mathf.clamp(0.15f + b.hitSize / 25f + b.damage / 120f, 0.15f, 0.75f));
                });

                for(var it = crossed.entries().iterator(); it.hasNext();){
                    if(Time.time - it.next().value > crossCooldown) it.remove();
                }
            }
        }

        @Override
        public void onRemoved(){
            super.onRemoved();
            fields.remove(this, true);
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
