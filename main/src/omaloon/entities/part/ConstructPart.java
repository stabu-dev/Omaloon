package omaloon.entities.part;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.entities.part.*;
import mindustry.graphics.*;

public class ConstructPart extends DrawPart{
    public static float currentUnitRotation;

    public String suffix;
    @Nullable
    public String name;

    public PartProgress progress = PartProgress.reload;
    public PartProgress buildProgress = PartProgress.reload;

    public float x, y, rot;
    public float sclX = 1f, sclY = 1f;

    public float layerOffset;
    public float outlineLayerOffset;

    public float moveX, moveY;
    public float buildScale = 0.9f;
    public float buildDarkness = 0.6f;

    public TextureRegion constructRegion, outlineRegion;

    public ConstructPart(String suffix){
        this.suffix = suffix;
    }

    public ConstructPart(){
        this("");
    }

    public void draw(DrawPart.PartParams params){
        float z = Draw.z();

        float buildProg = buildProgress.getClamp(params);
        float slideProg = progress.getClamp(params);

        float curX = x + moveX * (1f - slideProg);
        float curY = y + moveY * (1f - slideProg);
        float curScl = buildScale + (1f - buildScale) * slideProg;
        float curDark = buildDarkness + (1f - buildDarkness) * slideProg;

        float unitRot = currentUnitRotation;
        float targetRot = params.rotation;
        float baseRot = Mathf.slerp(unitRot, targetRot, slideProg);

        float rX = params.x + Angles.trnsx(baseRot - 90f, curX * sclX, curY);
        float rY = params.y + Angles.trnsy(baseRot - 90f, curX * sclX, curY);
        float rR = baseRot + rot - 90f;

        Draw.scl(sclX * curScl, sclY * curScl);
        Draw.z(z + outlineLayerOffset);
        if(outlineRegion.found()){
            float parentAlpha = Draw.getColor().a;
            Draw.color(curDark, curDark, curDark, parentAlpha);
            Draw.rect(outlineRegion, rX, rY, rR);
        }

        Draw.z(z + layerOffset);
        float parentAlpha = Draw.getColor().a;
        Draw.color(curDark, curDark, curDark, parentAlpha);

        if(buildProg < 1f){
            float constructAlpha = buildProg * (buildProg > 0.85f ? Mathf.clamp((1f - buildProg) / 0.15f) : 1f) * parentAlpha;
            float sx = sclX * curScl, sy = sclY * curScl;
            Draw.draw(Draw.z(), () -> {
                Draw.scl(sx, sy);
                Draw.color(curDark, curDark, curDark, parentAlpha);
                Drawf.construct(rX, rY, constructRegion, rR, buildProg, constructAlpha, Time.time);
                Draw.reset();
            });
        }else{
            Draw.rect(constructRegion, rX, rY, rR);
        }

        Draw.z(z);
        Draw.scl(1f, 1f);
        Draw.color();
    }

    public void load(String name){
        if(this.name == null) this.name = name + suffix;

        constructRegion = Core.atlas.find(this.name);
        outlineRegion = Core.atlas.find(this.name + "-outline");
    }
}
