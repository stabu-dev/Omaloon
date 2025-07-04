package omaloon.world.blocks.environment;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.graphics.*;
import mindustry.world.*;
import mindustry.world.blocks.environment.*;
import omaloon.type.shape.*;

import static mindustry.Vars.*;

/**
 * A floor that draws a large texture chunk when a configurable area of this floor is present.
 * <p>
 * For this to work visually, the supplied region's pixel size should correspond to the
 * pattern size (e.g., a 4x2 pattern needs a 128x64 pixel sprite).
 */
public class PatternedFloor extends Floor{
    // A sequence to keep track of tiles that are already part of a drawn pattern for this frame.
    private static final LongSeq claimedTiles = new LongSeq();
    // Cache for split-edge textures to avoid re-splitting them every frame.
    private static final ObjectMap<Block, TextureRegion[][]> edgeCache = new ObjectMap<>();
    private static long lastFrameId = -1;

    /** The shape of the pattern. Initialized in load(). */
    public Shape shape = new RectangleShape();

    public Block parent = Blocks.stone;

    /** If true, the pattern will draw blended edges with surrounding floors. */
    public boolean drawPatternEdges = true;

    public PatternedFloor(String name){
        super(name);
        variants = 0;
        blendGroup = this.parent;
    }

    @Override
    public void createIcons(MultiPacker packer){
        super.createIcons(packer);

        if(shape instanceof CustomPatternShape customShape){
            TextureRegion region = Core.atlas.find(customShape.maskName);
            if(region.found()){
                customShape.buildFromPixmap(Core.atlas.getPixmap(region));
            }else{
                Log.err("Mask texture not found for CustomPatternShape: @", customShape.maskName);
            }
        }
    }

    /**
     * Checks if the given tile is the top-right corner of a complete pattern of this floor.
     * @return true if the WxH area ending at this tile is complete.
     */
    private boolean isPotentialAnchor(Tile tile){
        if(tile == null) return false;

        Tile bottomLeft = tile.nearby(-(shape.width() - 1), -(shape.height() - 1));
        if(bottomLeft == null) return false;

        final boolean[] allMatch = {true};
        shape.each((x, y) -> {
            if(!allMatch[0]) return;
            Tile other = world.tile(bottomLeft.x + x, bottomLeft.y + y);
            if(other == null || other.floor() != this){
                allMatch[0] = false;
            }
        });

        return allMatch[0];
    }

    @Override
    public void drawBase(Tile tile){
        long frameId = Core.graphics.getFrameId();
        if(frameId != lastFrameId){
            claimedTiles.clear();
            edgeCache.clear();
            lastFrameId = frameId;
        }

        if(parent != Blocks.air) parent.drawBase(tile);

        if(isPotentialAnchor(tile)){
            if(!isAreaClaimed(tile)){
                claimArea(tile);

                Tile bottomLeft = tile.nearby(-(shape.width() - 1), -(shape.height() - 1));
                Mathf.rand.setSeed(bottomLeft.pos());

                Draw.rect(variantRegions[Mathf.randomSeed(bottomLeft.pos(), 0, Math.max(0, variantRegions.length - 1))],
                bottomLeft.worldx() + (shape.width() - 1) * tilesize / 2f,
                bottomLeft.worldy() + (shape.height() - 1) * tilesize / 2f);

                if(drawPatternEdges) drawPatternEdges(tile);
            }
        }
    }

    /** Draws blended edges around the entire perimeter of the composite pattern. */
    private void drawPatternEdges(Tile anchor){
        Tile bottomLeft = anchor.nearby(-(shape.width() - 1), -(shape.height() - 1));

        shape.each((x, y) -> {
            Tile tileInPattern = world.tile(bottomLeft.x + x, bottomLeft.y + y);
            if(tileInPattern == null) return;

            for(int i = 0; i < 8; i++){
                var point = Geometry.d8[i];
                Tile neighbor = tileInPattern.nearby(point.x, point.y);

                if(neighbor != null && !claimedTiles.contains(neighbor.pos()) && doEdge(tileInPattern, neighbor, neighbor.floor())){
                    TextureRegion region = edge(neighbor.floor(), 1 - point.x, 1 - point.y);
                    if(region != null) Draw.rect(region, tileInPattern.worldx(), tileInPattern.worldy());
                }
            }
        });
    }

    /** Re-implementation of Floor.doEdge using public APIs to avoid access issues. */
    public boolean doEdge(Tile tile, Tile otherTile, Floor other){
        return (other.realBlendId(otherTile) > this.realBlendId(tile) || getEdges(this.parent.asFloor()) == null);
    }

    /** Re-implementation of Floor.edge, using a cache to get edge textures without protected access. */
    private TextureRegion edge(Floor floor, int rx, int ry){
        TextureRegion[][] edges = getEdges(floor);
        if(edges == null || edges.length <= rx || edges[rx].length <= 2 - ry) return null;
        return edges[rx][2 - ry];
    }

    /** Gets the split-edge regions for a floor, using a cache to avoid re-splitting. */
    private TextureRegion[][] getEdges(Floor floor){
        Block blendBlock = floor.blendGroup;
        if(edgeCache.containsKey(blendBlock)) return edgeCache.get(blendBlock);

        TextureRegion edgeSheet = Core.atlas.find(blendBlock.name + "-edge");
        if(!edgeSheet.found()) return null;

        int size = (int)(tilesize / Draw.scl);
        TextureRegion[][] split = edgeSheet.split(size, size);
        edgeCache.put(blendBlock, split);
        return split;
    }

    /** Checks if another pattern already claims any tile in the prospective area. */
    private boolean isAreaClaimed(Tile anchor){
        Tile bottomLeft = anchor.nearby(-(shape.width() - 1), -(shape.height() - 1));
        if(bottomLeft == null) return true; // Cannot be an anchor if its area is out of bounds

        final boolean[] claimed = {false};
        shape.each((x, y) -> {
            if(claimed[0]) return;
            Tile other = world.tile(bottomLeft.x + x, bottomLeft.y + y);
            if(other != null && claimedTiles.contains(other.pos())){
                claimed[0] = true;
            }
        });
        return claimed[0];
    }

    /** Claims all tiles in the area for this pattern, preventing others from drawing over it. */
    private void claimArea(Tile anchor){
        Tile bottomLeft = anchor.nearby(-(shape.width() - 1), -(shape.height() - 1));
        if(bottomLeft == null) return;

        shape.each((x, y) -> {
            Tile other = world.tile(bottomLeft.x + x, bottomLeft.y + y);
            if(other != null){
                claimedTiles.add(other.pos());
            }
        });
    }

    @Override
    public boolean updateRender(Tile tile){
        for(int dx = 0; dx < shape.width(); dx++){
            for(int dy = 0; dy < shape.height(); dy++){
                if(isPotentialAnchor(tile.nearby(dx, dy))){
                    return true;
                }
            }
        }
        return false;
    }
}

