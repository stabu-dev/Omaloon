package omaloon.world.patterns;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.world.*;
import omaloon.world.patterns.shape.*;

import static mindustry.Vars.*;

/**
 * Pairs a geometric {@link Shape} with regions and variants.
 * @author stabu_
 */
public class Pattern{
    public final String name;
    public String localizedName;
    public Shape shape = new RectangleShape();
    public int variants = 0;
    public @Nullable PatternPatch patch;
    /** cells by shape position and rotation */
    public Block[] cells;

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

    public Pattern(String name, Shape shape, @Nullable PatternPatch patch){
        this(name, shape);
        this.patch = patch;
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

    public int area(){
        return shape == null ? 0 : shape.width() * shape.height();
    }

    int[] cellList;

    /** @return shape cells, row by row */
    public int[] cellList(){
        if(cellList == null){
            IntSeq out = new IntSeq();
            for(int y = 0; y < shape.height(); y++){
                for(int x = 0; x < shape.width(); x++){
                    if(shape.get(x, y)) out.add(x, y);
                }
            }
            cellList = out.toArray();
        }
        return cellList;
    }

    public Point2[] offsets(int x, int y, int rotation){
        Seq<Point2> out = new Seq<>();
        shape.each((cx, cy) -> out.add(new Point2(cx - x, cy - y).rotate(rotation)));
        return out.toArray(Point2.class);
    }

    /** @return center offset from anchor */
    public float offsetX(){
        return ((shape.width() - 1) / 2f - shape.anchorX) * tilesize;
    }

    /** @return center offset from anchor */
    public float offsetY(){
        return ((shape.height() - 1) / 2f - shape.anchorY) * tilesize;
    }

    public TextureRegion icon(){
        return region;
    }

    public String localizedName(){
        return localizedName;
    }
}