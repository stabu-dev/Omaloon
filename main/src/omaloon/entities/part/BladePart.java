package omaloon.entities.part;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.entities.part.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import omaloon.gen.*;
import omaloon.type.*;

public class BladePart extends DrawPart{
    public String suffix;
    public @Nullable String name;

    public float x = 0f, y = 0f;
    public float bladeSizeScl = 1f, shadeSizeScl = 1f;
    public float bladeMaxMoveAngle = 12f, bladeMinMoveAngle = 0f;
    public float layerOffset = 0.001f;
    public float blurAlpha = 0.9f;
    public boolean mirror = true;

    public TextureRegion bladeRegion, blurRegion, bladeOutlineRegion, shadeRegion;

    public BladePart(String suffix){
        this.suffix = suffix;
    }

    public BladePart(){
        this("");
    }

    @Override
    public void draw(PartParams params){
        Unit unit = GlasmoreUnitType.currentUnit;
        if(!(unit instanceof Ornithopterc ornithopter)) return;

        float z = unit.elevation > 0.5f ? (unit.type().lowAltitude ? Layer.flyingUnitLow : Layer.flyingUnit) : unit.type().groundLayer + Mathf.clamp(unit.hitSize / 4000f, 0, 0.01f);
        Draw.z(z + layerOffset - 0.001f);

        float speedScl = ornithopter.bladeMoveSpeedScl();

        int len = mirror && params.sideOverride == -1 ? 2 : 1;
        float bladeScl = Draw.scl * bladeSizeScl;
        float shadeScl = Draw.scl * shadeSizeScl;

        for(int s = 0; s < len; s++){
            int i = params.sideOverride == -1 ? s : params.sideOverride;
            float sideMultiplier = (i == 0 ? 1f : -1f) * params.sideMultiplier;

            float rx = params.x + Angles.trnsx(params.rotation - 90, x * sideMultiplier, y);
            float ry = params.y + Angles.trnsy(params.rotation - 90, x * sideMultiplier, y);

            long seed = unit.id + this.hashCode();
            float moveAngle = Mathf.randomSeed(seed + (long)Time.time, bladeMaxMoveAngle, -bladeMinMoveAngle);
            float rot = params.rotation - 90 + sideMultiplier * moveAngle;

            if(bladeRegion.found()){
                if(bladeOutlineRegion.found()){
                    Draw.alpha(blurRegion.found() ? 1f - (speedScl / 0.8f) : 1f);
                    Draw.rect(
                    bladeOutlineRegion, rx, ry,
                    bladeOutlineRegion.width * bladeScl * sideMultiplier,
                    bladeOutlineRegion.height * bladeScl,
                    rot
                    );
                }

                Draw.mixcol(Color.white, unit.hitTime);
                Draw.alpha(blurRegion.found() ? 1f - (speedScl / 0.8f) : 1f);
                Draw.rect(
                bladeRegion, rx, ry,
                bladeRegion.width * bladeScl * sideMultiplier,
                bladeRegion.height * bladeScl,
                rot
                );
                Draw.reset();
            }

            if(blurRegion.found()){
                Draw.alpha(speedScl * blurAlpha * (unit.dead() ? speedScl * 0.5f : 1f));
                Draw.rect(
                blurRegion, rx, ry,
                blurRegion.width * bladeScl * sideMultiplier,
                blurRegion.height * bladeScl,
                rot
                );
                Draw.reset();
            }

            if(shadeRegion.found()){
                Draw.alpha(speedScl * blurAlpha * (unit.dead() ? speedScl * 0.5f : 1f));
                Draw.rect(
                shadeRegion, rx, ry,
                shadeRegion.width * shadeScl * sideMultiplier,
                shadeRegion.height * shadeScl,
                rot
                );
                Draw.mixcol(Color.white, unit.hitTime);
                Draw.reset();
            }
        }

        Draw.z(z);
    }

    @Override
    public void load(String name){
        if(this.name == null) this.name = name + suffix;

        bladeRegion = Core.atlas.find(this.name);
        blurRegion = Core.atlas.find(this.name + "-blur");
        bladeOutlineRegion = Core.atlas.find(this.name + "-outline");
        shadeRegion = Core.atlas.find(this.name + "-blur-shade");
    }
}
