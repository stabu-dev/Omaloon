package omaloon.type;

import arc.*;
import arc.graphics.g2d.*;
import mindustry.io.*;

public class Blade{
    public final String spriteName;
    public TextureRegion bladeRegion, blurRegion, bladeOutlineRegion, shadeRegion;

    public float x = 0f, y = 0f;

    public float bladeSizeScl = 1, shadeSizeScl = 1;
    /**
     * Blade max moving distance
     */
    public float bladeMaxMoveAngle = 12;
    /**
     * Blade min moving distance
     */
    public float bladeMinMoveAngle = 0f;

    public float layerOffset = 0.001f;

    public float blurAlpha = 0.9f;

    public Blade(String name){
        this.spriteName = name;
    }

    public static class BladeMount{
        public Blade blade;
        public float bladeRotation;

        public BladeMount(Blade blade){
            this.blade = blade;
        }
    }

    public void load(){
        bladeRegion = Core.atlas.find(spriteName);
        blurRegion = Core.atlas.find(spriteName + "-blur");
        bladeOutlineRegion = Core.atlas.find(spriteName + "-outline");
        shadeRegion = Core.atlas.find(spriteName + "-blur-shade");
    }

    // For mirroring
    public Blade copy(){
        return JsonIO.copy(this, new Blade(spriteName));
    }
}