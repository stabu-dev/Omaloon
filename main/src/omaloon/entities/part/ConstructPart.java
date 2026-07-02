package omaloon.entities.part;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.entities.part.*;
import mindustry.graphics.*;

public class ConstructPart extends DrawPart{
    public String suffix;
    @Nullable
    public String name;

    public PartProgress progress = PartProgress.reload;

    public float x, y, rot;
    public float sclX = 1f, sclY = 1f;

    public float layerOffset;
    public float outlineLayerOffset;

    public float finishTresh = 0.95f;

    public TextureRegion constructRegion, outlineRegion;

    public ConstructPart(String suffix){
        this.suffix = suffix;
    }

    public ConstructPart(){
        this("");
    }

    public void draw(DrawPart.PartParams params){
        float z = Draw.z();

        float dx = params.x + Angles.trnsx(params.rotation - 90f, x, y);
        float dy = params.y + Angles.trnsy(params.rotation - 90f, x, y);
        float dr = params.rotation + rot - 90f;

        float prog = progress.getClamp(params);

        Draw.scl(sclX, sclY);
        Draw.z(z + outlineLayerOffset);
        if(outlineRegion.found()) Draw.rect(outlineRegion, dx, dy, dr);

        Draw.z(z + layerOffset);
        if(prog < finishTresh){
            float sx = sclX, sy = sclY;
            float parentAlpha = Draw.getColor().a;
            float constructAlpha = prog * (prog > finishTresh - 0.15f ? Mathf.clamp((finishTresh - prog) / 0.15f) : 1f) * parentAlpha;

            Draw.draw(Draw.z(), () -> {
                Draw.scl(sx, sy);
                Draw.color(Color.white, parentAlpha);
                Drawf.construct(dx, dy, constructRegion, dr, prog, constructAlpha, Time.time);
                Draw.reset();
            });
        }else{
            Draw.rect(constructRegion, dx, dy, dr);
        }

        Draw.z(z);
        Draw.scl(1f, 1f);
    }

    public void load(String name){
        if(this.name == null) this.name = name + suffix;

        constructRegion = Core.atlas.find(this.name);
        outlineRegion = Core.atlas.find(this.name + "-outline");
    }
}
