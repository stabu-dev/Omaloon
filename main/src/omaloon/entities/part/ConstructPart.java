package omaloon.entities.part;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.entities.part.*;
import mindustry.graphics.*;

public class ConstructPart extends DrawPart {
    public String suffix;
    @Nullable public String name;

    public PartProgress progress;

    public float x, y, rot;

    public float layerOffset;
    public float outlineLayerOffset;

    public float finishTresh;

    public TextureRegion constructRegion, outlineRegion;

    public ConstructPart(String suffix) {
        this.progress = PartProgress.reload;
        this.finishTresh = 0.95f;
        this.layerOffset = 0f;
        this.outlineLayerOffset = 0f;
        this.suffix = suffix;
    }

    public ConstructPart() {
        this("");
    }

    public void draw(DrawPart.PartParams params) {
        float z = Draw.z();

        float dx = params.x + Angles.trnsx(params.rotation - 90f, this.x, this.y);
        float dy = params.y + Angles.trnsy(params.rotation - 90f, this.x, this.y);
        float dr = params.rotation + rot - 90f;

        float prog = progress.getClamp(params);

        Draw.z(z + outlineLayerOffset);
        if (outlineRegion.found()) Draw.rect(outlineRegion, dx, dy, dr);

        Draw.z(z + layerOffset);
        if (prog < finishTresh) {
            Draw.draw(Draw.z(), () -> Drawf.construct(dx, dy, constructRegion, dr, prog, 1f, Time.time));
        } else {
            Draw.rect(constructRegion, dx, dy, dr);
        }

        Draw.z(z);
    }

    public void load(String name) {
        if (this.name == null) this.name = name + suffix;

        constructRegion = Core.atlas.find(this.name);
        outlineRegion = Core.atlas.find(this.name + "-outline");
    }
}
