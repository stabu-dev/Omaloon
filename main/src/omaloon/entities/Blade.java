package omaloon.entities;

import arc.*;
import arc.graphics.g2d.*;

public class Blade implements Cloneable {
    public final String spriteName;
    public TextureRegion bladeRegion, blurRegion, bladeOutlineRegion, shadeRegion;

    public boolean mirror = true;

    /**
     * Flips the sprite just like weapons if mirror is set to true.
     */
    public boolean flipSprite;

    public float x = 0f, y = 0f;

    public float side = 1f;

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

    public Blade copy() {
        try {
            return (Blade) clone();
        } catch (CloneNotSupportedException nice) {
            throw new RuntimeException("wow awesome java", nice);
        }
    }

    public void load(){
        bladeRegion = Core.atlas.find(spriteName);
        blurRegion = Core.atlas.find(spriteName + "-blur");
        bladeOutlineRegion = Core.atlas.find(spriteName + "-outline");
        shadeRegion = Core.atlas.find(spriteName + "-blur-shade");
    }

    // For mirroring
//    public Blade copy(){
//        return JsonIO.copy(this, new Blade(spriteName));
//    }

    public static class BladeMount{
        public Blade blade;
        public float bladeRotation;

        public BladeMount(Blade blade){
            this.blade = blade;
        }
    }
}