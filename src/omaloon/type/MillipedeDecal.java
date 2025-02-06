package omaloon.type;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import mindustry.gen.*;

import static arc.Core.atlas;

public class MillipedeDecal{
    private final static Vec2 v1 = new Vec2();
    public float baseX, baseY, endX, endY;
    public float baseOffset;
    public int segments = 1;
    public Color lineColor = Color.white;
    public float lineWidth = 2f;
    String name;
    public TextureRegion baseRegion, endRegion;
    public TextureRegion[] segmentRegions;

    public MillipedeDecal(String name){
        this.name = name;
    }

    public void load(){
        baseRegion = atlas.find(name + "-base");
        endRegion = atlas.find(name + "-end");
        segmentRegions = new TextureRegion[segments];
        for(int i = 0; i < segmentRegions.length; i++){
            segmentRegions[i] = atlas.find(name + "-" + i);
        }
    }

    public void draw(Unit base, Unit other){
        if(other == null) return;
        for(int s : Mathf.signs){
            v1.trns(base.rotation - 90f, baseX * s, baseY).add(base);
            float bx = v1.x, by = v1.y;
            v1.trns(other.rotation - 90f, endX * s, endY).add(other);
            float ex = v1.x, ey = v1.y;
            float angle = Angles.angle(bx, by, ex, ey);

            Draw.mixcol();
            Draw.color(lineColor);
            Fill.circle(bx, by, lineWidth / 2f);
            Fill.circle(ex, ey, lineWidth / 2f);
            Lines.stroke(lineWidth);
            Lines.line(bx, by, ex, ey, false);

            base.type.applyColor(base);
            v1.trns(angle + 180f, (endRegion.width * Draw.scl * 0.5f) - baseOffset).add(ex, ey);
            ex = v1.x;
            ey = v1.y;
            v1.trns(angle, (baseRegion.width * Draw.scl * 0.5f) - baseOffset).add(bx, by);
            bx = v1.x;
            by = v1.y;

            for(int i = segmentRegions.length - 1; i >= 0; i--){
                TextureRegion r = segmentRegions[i];
                float p = (i + 1f) / (segments + 1f);
                v1.set(bx, by).lerp(ex, ey, p);

                Draw.rect(r, v1.x, v1.y, angle);
            }

            Draw.rect(endRegion, ex, ey, angle + 180f);
            Draw.rect(baseRegion, bx, by, angle);
        }
        Draw.reset();
    }
}
