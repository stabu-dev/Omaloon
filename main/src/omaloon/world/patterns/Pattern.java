package omaloon.world.patterns;

import arc.*;
import arc.graphics.g2d.*;
import arc.util.*;
import mindustry.world.*;
import mindustry.world.draw.*;
import omaloon.world.patterns.shape.*;

/**
 * Pairs a geometric {@link Shape} with visual texture regions and variants.
 * @author stabu_
 */
public class Pattern{
    public final String name;
    public String localizedName;
    public Shape shape = new RectangleShape();
    public int variants = 0;
    public @Nullable DrawBlock drawer;

    public TextureRegion region;
    public TextureRegion[] variantRegions;

    public Pattern(String name){
        this.name = name;
        this.localizedName = Core.bundle.get("pattern." + name + ".name", name);
    }

    public Pattern(String name, Shape shape){
        this(name);
        this.shape = shape;
    }

    public Pattern(String name, Shape shape, int variants){
        this(name, shape);
        this.variants = variants;
    }

    public Pattern(String name, Shape shape, DrawBlock drawer){
        this(name, shape);
        this.drawer = drawer;
    }

    public Pattern(String name, Shape shape, DrawBlock drawer, int variants){
        this(name, shape, variants);
        this.drawer = drawer;
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
    }

    public void load(Block block){
        load();
        if(drawer != null) drawer.load(block);
    }

    public TextureRegion icon(){
        return region;
    }

    public String localizedName(){
        return localizedName;
    }
}