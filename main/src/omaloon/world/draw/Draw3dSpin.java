package omaloon.world.draw;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arclibrary.graphics.*;
import mindustry.gen.*;
import mindustry.world.*;
import mindustry.world.draw.*;

public class Draw3dSpin extends DrawBlock{
    public final Vec2 regionOffset = new Vec2();
    public final Mat3D transformation = new Mat3D();
    public String suffix = "";
    public TextureRegion region;
    public float rotateSpeed = 1f;
    public float startRotationOffset = 90 + 90 + 45;
    public int regionWidth = 8;
    public float pixelSize = 0.125f;

    @Override
    public void load(Block block){
        super.load(block);
        region = Core.atlas.find(block.name + suffix);
    }

    @Override
    public void draw(Building build){
        Draw.flush();
        float realWidth = region.width * region.scl() * Draw.xscl;
        float realHeight = region.height * region.scl() * Draw.yscl;


        float time = build.totalProgress() * rotateSpeed;
        float alpha = Mathf.mod(time, 90) / 90f;
        for(int i = regionWidth / 2; i >= -regionWidth / 2; i--){
            Draw.alpha(1f);
            float drawX = build.x + regionOffset.x - i * pixelSize - realWidth / 2f;
            float drawY = build.y + regionOffset.y - realHeight / 2f;
            Draw3d.rect(transformation, region, drawX, drawY, realWidth, realHeight, time % 90 + startRotationOffset);
            Draw.alpha(alpha);
            Draw3d.rect(transformation, region, drawX, drawY, realWidth, realHeight, time % 90 - 90 + startRotationOffset);
        }
    }
}
