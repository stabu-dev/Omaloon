package omaloon.entities;

import arc.graphics.g2d.*;

public class BladeDestroyData{
    public TextureRegion region;
    public TextureRegion outline;
    public float scale;
    public float side;

    public BladeDestroyData(TextureRegion region, TextureRegion outline, float scale, float side){
        this.region = region;
        this.outline = outline;
        this.scale = scale;
        this.side = side;
    }
}
