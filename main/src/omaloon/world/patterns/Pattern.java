package omaloon.world.patterns;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import omaloon.type.shape.*;

public class Pattern{
    public final String name;
    public Shape shape = new RectanglePatternShape();
    public int variants = 0;

    public TextureRegion region;
    public TextureRegion[] variantRegions;
    public transient TextureRegion[][][] slicedRegions;

    public Pattern(String name){
        this.name = name;
    }

    public void loadRegion(){
        region = Core.atlas.find(name, name + "1");
        if(variants > 0){
            variantRegions = new TextureRegion[variants];
            for(int i = 0; i < variants; i++){
                variantRegions[i] = Core.atlas.find(name + (i + 1));
            }
        }else{
            variantRegions = new TextureRegion[]{region};
        }
    }

    public void load(){
        loadRegion();
        shape.load();

        if(region != null && region.found()){
            int tilePixelWidth = region.width / shape.width();
            int tilePixelHeight = region.height / shape.height();

            slicedRegions = new TextureRegion[Math.max(1, variants)][][];
            for(int i = 0; i < slicedRegions.length; i++){
                // Arc's split returns regions[column][row]
                slicedRegions[i] = variantRegions[i].split(tilePixelWidth, tilePixelHeight);
                for(var columns : slicedRegions[i]){
                    for(var slice : columns){
                        slice.scale = region.scale;
                    }
                }
            }
        }
    }

    public int getSliceIndex(int relativeX, int relativeY, int variantIdx){
        int area = shape.width() * shape.height();
        return (variantIdx * area) + (relativeY * shape.width()) + relativeX;
    }

    public int variant(int x, int y, int max){
        return Mathf.randomSeed(Point2.pack(x, y), 0, Math.max(0, max - 1));
    }

    public String localizedName(){
        return Core.bundle.get("block." + name + ".name", name);
    }

    public String description(){
        return Core.bundle.get("block." + name + ".description", "");
    }
}
