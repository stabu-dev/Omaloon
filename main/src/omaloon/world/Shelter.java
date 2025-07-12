package omaloon.world;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import omaloon.annotations.Annotations.*;
import omaloon.utils.*;

public class Shelter extends GenericPressureBlock{
    public float range = 80;
    public float minRange = 12f;

    public float retargetTime = 10f;

    public float rotateSpeed = 1f;
    public float growSpeed = 1f;
    public float warmupSpeed = 0.014f;

    public Color arcColor = Pal.heal;

    public @Load("@-base") TextureRegion baseRegion;

    private final int retargetTimer = timers++;
    private static final Seq<Building> sharedBuildings = new Seq<>();

    public Shelter(String name){
        super(name);

        update = true;
    }

    @Override
    protected TextureRegion[] icons(){
        return new TextureRegion[]{
            Core.atlas.find(name + "-base"),
            Core.atlas.find(name)
        };
    }

    @Override
    public void init(){
        super.init();

        updateClipRadius(range);
    }

    public class ShelterBuild extends GenericPressureBlockBuild{
        public float warmup;
        public float targetRotation, currentRotation;
        public float targetArcLength, currentArcLength;

        @Override
        public void draw(){
            Draw.rect(baseRegion, x, y);

            Draw.rect(region, x, y, currentRotation + currentArcLength / 2f - 90f);

            drawArc();
        }

        public void drawArc(){
            Draw.z(Layer.shields);
            Draw.color(arcColor);
            Fill.circle(x, y, minRange * warmup);
            Fill.arc(x, y, range * warmup, currentArcLength/360f, currentRotation);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);

            warmup = read.f();

            targetRotation = read.f();
            currentRotation = read.f();

            targetArcLength = read.f();
            currentArcLength = read.f();
        }

        public void retarget(){
            Vec2 tPos = Tmp.v2.setZero();

            sharedBuildings.clear();
            team.data().buildingTree.intersect(
            x - range, y - range,
            range * 2, range * 2,
            sharedBuildings
            );
            int filtered = 0;
            for(Building build : sharedBuildings) {
                if (build.dst(this) > range || build == this) continue;
                tPos.add(
                build.x * build.block.size * build.block.size,
                build.y * build.block.size * build.block.size
                );
                filtered += build.block.size * build.block.size;
            }
            float angle = tPos.scl(1f/Math.max(1f, filtered)).sub(this).angle();
            float distMax = 0, distMin = 0;
            for(Building build : sharedBuildings) {
                if (build.dst(this) > range || build == this) continue;
                float size = Mathf.sqrt2 * build.block.size * 8f;
                boolean sideNegative = false, sidePositive = false;
                for(int s = 0; s < 4; s++) {
                    float angleDist = OlUtils.angleDistSigned(
                    angle,
                    angleTo(Tmp.v3.trns(s * 90 - 45, size).add(build))
                    );
                    if (angleDist < 0) sideNegative = true;
                    if (angleDist > 0) sidePositive = true;
                    distMax = Math.max(distMax, angleDist);
                    distMin = Math.min(distMin, angleDist);
                    if (sideNegative && sidePositive && Tmp.v2.nor().dot(Tmp.v3.set(build).sub(this).nor()) < 0f) {
                        distMax = 179f;
                        distMin = -179f;
                    }
                }
            }

            float totalDist = (distMax - distMin) / 2f;
            if (totalDist > 170f) totalDist = 180f;
            float angleOffset = (distMax + distMin) / 2f;

            targetRotation = angle - totalDist - angleOffset;
            targetArcLength = totalDist * 2f;
        }

        @Override
        public boolean shouldConsume(){
            return super.shouldConsume() && targetArcLength > 0;
        }

        @Override
        public void updateTile(){
            if (timer(retargetTimer, retargetTime)) retarget();

            if (efficiency > 0) {
                warmup = Mathf.approach(warmup, 1f, warmupSpeed * edelta());
                currentRotation = Angles.moveToward(currentRotation, targetRotation, rotateSpeed * edelta());
                currentArcLength = Mathf.approach(currentArcLength, targetArcLength, growSpeed * edelta());

                Groups.bullet.intersect(x - range, y - range, range * 2, range * 2, (Bullet bullet) -> {
                    if (
                        (
                            (
                                dst(bullet) < range * efficiency &&
                                OlUtils.angleDist(currentRotation + currentArcLength / 2f, angleTo(bullet)) < currentArcLength / 2f
                            ) ||
                            dst(bullet) < minRange
                        ) && bullet.team == Team.derelict
                    ) {
                        bullet.absorb();
                    }
                });
            } else warmup = Mathf.approachDelta(warmup, 0f, warmupSpeed);


        }

        @Override
        public void write(Writes write){
            super.write(write);

            write.f(warmup);

            write.f(targetRotation);
            write.f(currentRotation);

            write.f(targetArcLength);
            write.f(currentArcLength);
        }
    }
}
