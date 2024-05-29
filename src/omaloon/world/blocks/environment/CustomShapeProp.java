package omaloon.world.blocks.environment;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.geom.*;
import arc.struct.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.world.*;
import mindustry.world.blocks.environment.*;
import omaloon.struct.*;
import omaloon.type.customshape.*;
import omaloon.utils.*;
import omaloon.world.*;
import omaloon.world.interfaces.*;

import static arc.Core.*;

/**
 * A breakable prop with custom shape.
 * Has a tile defined as center(it's a corner) that is the one further in the tile index.
 * remember, when making the mask it is important to always paint the corner of the sprite with 00000AFF, the other occupied tiles with black and the rest should be transparent.
 * @see Tiles
 */
public class CustomShapeProp extends Prop implements MultiPropI {
    public TextureRegion[] shadows, shapeRegions;

    /**
     * shape of this multiblock represented as offsets from the center.
     * make all offsets connected to eachother in atleas one of the 4 cardinal directions.
     * @apiNote  DO NOT SET MANUALLY
     */
    public Seq<CustomShape> shapes = new Seq<>();

    /**
     * drawing offset for a shape. This variable should be the same size or larger than the shapes Seq!
     * @apiNote Will throw ArrayIndexOutOfBoundsException if too small
     */
    public Vec2[] spriteOffsets;

    public Effect deconstructEffect = Fx.none;

    public CustomShapeProp(String name) {
        super(name);
        customShadow = true;
        alwaysReplace = false;
        breakEffect = Fx.none;
    }

    public static CustomShape createShape(TextureRegion region) {
        PixmapRegion pixmap = Core.atlas.getPixmap(region);
        BitWordList list = new BitWordList(pixmap.width * pixmap.height, BitWordList.WordLength.two);

        OlUtils.readTexturePixels(pixmap, (color, index) -> {
            switch (color) {
                case 2815 -> list.set(index, (byte) 3);
                case 255 -> list.set(index, (byte) 2);
                default -> list.set(index, (byte) 1);
            }
        });

        return new CustomShape(pixmap.width, pixmap.height, list);
    }

    @Override
    public void drawBase(Tile tile) {
        MultiPropGroup multiProp = CustomShapePropProcess.instance.multiProps.find(multiPropGroup -> multiPropGroup.center == tile);
        if (multiProp != null) {
            Draw.z(layer);
            Draw.rect(variantRegions[multiProp.shape],
              tile.worldx() + spriteOffsets[multiProp.shape].x,
              tile.worldy() + spriteOffsets[multiProp.shape].y
            );
        }
    }

    @Override
    public void drawShadow(Tile tile) {
        MultiPropGroup multiProp = CustomShapePropProcess.instance.multiProps.find(multiPropGroup -> multiPropGroup.center == tile);
        if (multiProp != null) {
            Draw.rect(shadows[multiProp.shape],
              tile.worldx() + spriteOffsets[multiProp.shape].x,
              tile.worldy() + spriteOffsets[multiProp.shape].y
            );
        }
    }

    @Override
    public void load(){
        super.load();
        shadows = new TextureRegion[variants];
        shapeRegions = new TextureRegion[variants];
        for (int i = 0; i < variants; i++) {
            shadows[i] = atlas.find(name + (i + 1) + "-shadow");
            shapeRegions[i] = atlas.find(name + "-shape" + (i + 1), "omaloon-shape-err");
            shapes.addUnique(createShape(shapeRegions[i]));
        }
    }

    @Override
    public Runnable removed(MultiPropGroup from) {
        return () -> deconstructEffect.at(
          from.center.worldx() + spriteOffsets[from.shape].x,
          from.center.worldy() + spriteOffsets[from.shape].y
        );
    }

    @Override
    public Seq<CustomShape> shapes() {
        return shapes;
    }
}
