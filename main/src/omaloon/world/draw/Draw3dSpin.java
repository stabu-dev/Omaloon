package omaloon.world.draw;

import arc.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.gl.FrameBuffer;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import arclibrary.graphics.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.*;
import mindustry.world.draw.*;

import static arc.Core.camera;
import static arc.Core.graphics;

@SuppressWarnings("UnusedReturnValue")
public class Draw3dSpin extends DrawBlock{
    protected static final Mat3D transformation = new Mat3D();
    protected static final Vec2 tmpPixelOffset = new Vec2();
    protected static final Vec2 tmpRotatedBaseOffset = new Vec2();
    private static final Quat tmpQuat1 = new Quat();
    private static final Quat tmpQuat2 = new Quat();
    private static final Mat3D tmpMat1 = new Mat3D();
    public final Vec2 baseOffset = new Vec2();
    public final Vec3 scale = new Vec3(1, 1, 1);
    public float rotateSpeed = 4f;
    public float startRotationOffset = 90 + 90 + 45;
    public float pixelSize = 0.125f;
    public Floatf<Building> rotationProvider = null;
    //region transforms
    public Vec3 axis = Vec3.Y;
    public float rotationAroundAxis = -5.0f;
    //endregion
    //region texture
    public int regionWidth = 6;
    public String suffix = "", holderSuffix = "";
    protected TextureRegion region, holderRegion;
    private static FrameBuffer shadowBuffer = new FrameBuffer();

    private static void setScale(float[] val,float x,float y,float z){
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

    public <T extends Building> Draw3dSpin rotationProvider(Floatf<T> rotationProvider){
        //noinspection unchecked
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
    public void draw(Building build){//TODO fix layering issues
        super.draw(build);
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
            //setting scale for matrix ye Mat3D.scale is broken as other Mat3D methods
            setScale(mat3D.val, 1f-tmp.x*tmp.x,1f-tmp.y*tmp.y,1f-tmp.z*tmp.z);

            transformation.mul(mat3D);
            mat3D.idt();
            mat3D.rotate(Vec3.Z,baseRotation);
            transformation.mul(mat3D);
            baseRotationMatrix.scale(1f, -1f);
        }

        float alpha = Mathf.mod(time, 90) / 90f;
        float a = Draw.getColor().a;
        baseRotation = Mathf.mod(baseRotation, 180f);
        Draw.z(Layer.blockOver);
        Draw.rect(holderRegion, build.x, build.y, -baseRotation);
        Draw.alpha(baseRotation / 180f*a);
        Draw.rect(holderRegion, build.x, build.y, -baseRotation - 180f);
        Draw.alpha(a);
        Vec2 pixelOffset = tmpPixelOffset.set(-pixelSize, 0).mul(baseRotationMatrix);
        Vec2 baseOffset = tmpRotatedBaseOffset.set(this.baseOffset).mul(baseRotationMatrix);

        int halfRegionWidth = regionWidth / 2;
        float drawX = build.x + baseOffset.x + pixelOffset.x * halfRegionWidth - realWidth / 2f;
        float drawY = build.y + baseOffset.y + pixelOffset.y * halfRegionWidth - realHeight / 2f;
        for(int i = halfRegionWidth; i >= -halfRegionWidth; i--){
            Draw.alpha(1f);
            Draw3d.rect(transformation, region, drawX, drawY, realWidth, realHeight, mainRotation);
            Draw.alpha(alpha);
            Draw3d.rect(transformation, region, drawX, drawY, realWidth, realHeight, subRotation);
            drawX -= pixelOffset.x;
            drawY -= pixelOffset.y;
        }

        if (shadowBuffer == null) {
            shadowBuffer = new FrameBuffer(Pixmap.Format.rgba8888, region.width, region.height, false);
        }

        shadowBuffer.resize(graphics.getWidth(), graphics.getHeight());
        shadowBuffer.begin(Color.clear);
        Draw.color(Color.white);
        Draw3d.rect(transformation, region, drawX -8, drawY -8, realWidth, realHeight, mainRotation);
        Lines.stroke(2);
        Draw.rect(holderRegion, build.x -8, build.y -8, -baseRotation);
        Lines.line(build.x, build.y, build.x -8, build.y -8);
        Draw.color();
        shadowBuffer.end();
        Draw.color(Pal.shadow, Pal.shadow.a);
        Draw.z(Layer.blockProp + 1);
        Draw.rect(Draw.wrap(shadowBuffer.getTexture()), camera.position.x, camera.position.y, camera.width, -camera.height);
        Draw.color();
        Draw.reset();
    }
}
