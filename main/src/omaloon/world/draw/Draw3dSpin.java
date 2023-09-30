package omaloon.world.draw;

import arc.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.gl.FrameBuffer;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import arclibrary.graphics.*;
import mindustry.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.*;
import mindustry.world.draw.*;

import static arc.Core.*;

@SuppressWarnings("UnusedReturnValue")
public class Draw3dSpin extends DrawBlock{
    protected static final Mat3D transformation = new Mat3D();
    protected static final FloatSeq transformationQueue = new FloatSeq();
    protected static final Vec2 tmpPixelOffset = new Vec2();
    protected static final Vec2 tmpRotatedBaseOffset = new Vec2();
    private static final Quat tmpQuat1 = new Quat();
    private static final Quat tmpQuat2 = new Quat();
    private static final Mat3D tmpMat1 = new Mat3D();
    private static FrameBuffer shadowBuffer;

    static{
        Events.run(Trigger.postDraw, () -> {
            transformationQueue.size = 0;
        });
    }

    public final Vec2 baseOffset = new Vec2();
    public final Vec3 scale = new Vec3(1, 1, 1);
    public float rotateSpeed = 4f;
    public float startRotationOffset = 90 + 90 + 45;
    public float pixelSize = 0.125f;
    public Floatf<Building> rotationProvider = null;
    public Vec3 axis = Vec3.Y;
    public float rotationAroundAxis = -5.0f;
    public int regionWidth = 6;
    public String suffix = "";
    public String holderSuffix = "";
    public int shadowElevation = Vars.tilesize;
    protected TextureRegion region, holderRegion;

    private static void setScale(float[] val, float x, float y, float z){
        val[0] = x;
        val[5] = y;
        val[10] = z;
        val[15] = 1;
        for(int i = 0; i < 4; i++){
            val[i + 1] = 0;
            val[i + 6] = 0;
            val[i + 11] = 0;
        }
    }

    public float baseRotation(Building building){
        return rotationProvider == null ? 0f : (360 - (rotationProvider.get(building) % 360)) % 360f;
    }

    @SuppressWarnings("unchecked")
    public <T extends Building> Draw3dSpin rotationProvider(Floatf<T> rotationProvider){
        this.rotationProvider = (Floatf<Building>)rotationProvider;
        return this;
    }

    public TextureRegion region(){
        return region;
    }

    @Override
    public void load(Block block){
        super.load(block);
        region = Core.atlas.find(block.name + suffix);
        holderRegion = Core.atlas.find(block.name + holderSuffix);
    }

    @Override
    public void draw(Building build){
        final float startZ = Draw.z();
        try{
            float realWidth = region.width * region.scl() * Draw.xscl;
            float realHeight = region.height * region.scl() * Draw.yscl;
            float baseRotation = baseRotation(build);

            float time = build.totalProgress() * rotateSpeed;
            float mainRotation = time % 90 + startRotationOffset;
            float subRotation = time % 90 - 90 + startRotationOffset;
            Mat baseRotationMatrix;

            setupTransformations:
            {
                transformation.idt();
                float baseCos = Mathf.cosDeg(baseRotation);
                float baseSin = Mathf.sinDeg(baseRotation);
                baseRotationMatrix = Tmp.m1.setToRotation(Vec3.Z, baseCos, baseSin);

                Vec3 tmp = Tmp.v31;
                tmp.set(axis).mul(baseRotationMatrix);
                tmpQuat2.setFromAxis(tmp, rotationAroundAxis);

                tmpQuat1.setFromAxis(tmpQuat2.transform(tmp.set(0, 0, 1)), baseRotation);
                transformation.rotate(tmpQuat2);
                tmp.set(1f, 1f, 1f).sub(scale).mul(baseRotationMatrix);
                Mat3D mat3D = tmpMat1;
                setScale(mat3D.val, 1f - tmp.x * tmp.x, 1f - tmp.y * tmp.y, 1f - tmp.z * tmp.z);

                transformation.mul(mat3D);
                mat3D.idt();
                mat3D.rotate(Vec3.Z, baseRotation);
                transformation.mul(mat3D);
                baseRotationMatrix.scale(1f, -1f);
            }

            float alpha = Mathf.mod(time, 90) / 90f;
            baseRotation = Mathf.mod(baseRotation, 180f);
            final float finalBaseRotation = baseRotation;
            Vec2 pixelOffset = tmpPixelOffset.set(-pixelSize, 0).mul(baseRotationMatrix);
            Vec2 baseOffset = tmpRotatedBaseOffset.set(this.baseOffset).mul(baseRotationMatrix);

            int halfRegionWidth = regionWidth / 2;
            float drawX = build.x + baseOffset.x + pixelOffset.x * halfRegionWidth - realWidth / 2f;
            float drawY = build.y + baseOffset.y + pixelOffset.y * halfRegionWidth - realHeight / 2f;

            shadows:
            {
                int myIndex = transformationQueue.size;
                transformationQueue.addAll(transformation.val);
                Draw.draw(Layer.blockProp + 1, () -> {
                    if(shadowBuffer == null){
                        shadowBuffer = new FrameBuffer(graphics.getWidth(), graphics.getHeight());
                    }
                    Draw.flush();
                    shadowBuffer.resize(graphics.getWidth(), graphics.getHeight());
                    shadowBuffer.begin(Color.clear);
                    Draw.color();

                    System.arraycopy(transformationQueue.items, myIndex, transformation.val, 0, transformation.val.length);
                    Draw3d.rect(transformation, region, drawX - shadowElevation, drawY - shadowElevation, realWidth, realHeight, mainRotation);
                    Lines.stroke(2);
                    Draw.rect(holderRegion, build.x - shadowElevation, build.y - shadowElevation, -finalBaseRotation);
                    Lines.line(build.x, build.y, build.x - shadowElevation, build.y - shadowElevation);
                    Draw.color();
                    shadowBuffer.end();
                    Draw.color(Pal.shadow, Pal.shadow.a);
                    Draw.rect(Draw.wrap(shadowBuffer.getTexture()), camera.position.x, camera.position.y, camera.width, -camera.height);
                    Draw.flush();
                    Draw.color();
                });
            }
            Draw.z(Layer.blockProp + 2);
            float a = Draw.getColor().a;
            Draw.rect(holderRegion, build.x, build.y, -finalBaseRotation);
            Draw.alpha(finalBaseRotation / 180f * a);
            Draw.rect(holderRegion, build.x, build.y, -finalBaseRotation - 180f);
            float localDrawX = drawX, localDrawY = drawY;
            for(int i = halfRegionWidth; i >= -halfRegionWidth; i--){
                Draw.alpha(1f);
                Draw3d.rect(transformation, region, localDrawX, localDrawY, realWidth, realHeight, mainRotation);
                Draw.alpha(alpha);
                Draw3d.rect(transformation, region, localDrawX, localDrawY, realWidth, realHeight, subRotation);
                localDrawX -= pixelOffset.x;
                localDrawY -= pixelOffset.y;
            }
        }finally{
            Draw.z(startZ);
        }
    }
}
