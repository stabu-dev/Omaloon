package omaloon.world.blocks.environment;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
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
    private static final IntSet claimedTiles = new IntSet();
    // Cache for split-edge textures to avoid re-splitting them every frame.
    private static final ObjectMap<Block, TextureRegion[][]> edgeCache = new ObjectMap<>();

    private static final IntSet validAnchors = new IntSet();
    private static final IntSet invalidAnchors = new IntSet();
    private static long lastFrameId = -1;

    /** The shape of the pattern. Initialized in load(). */
    public Shape shape = new RectanglePatternShape();

    /** The parent floor to draw underneath the pattern. */
    public Block parent = Blocks.stone;

    /** If true, the pattern will draw blended edges with surrounding floors. */
    public boolean drawPatternEdges = true;

    /** If true, the pattern will be drawn on top of overlays. */
    public boolean drawOnTop = false;

    public PatternedFloor(String name){
        super(name);
        variants = 0;
        blendGroup = this.parent;
    }

    @Override
    public void createIcons(MultiPacker packer){
        super.createIcons(packer);
        shape.load();
    }

    /**
     * Memoized check to see if a valid pattern can be formed starting at bottomLeft.
     * This check only validates the floor types, ignoring claimed tiles.
     */
    private boolean hasPatternAt(Tile bottomLeft){
        if(bottomLeft == null) return false;
        int pos = bottomLeft.pos();
        if(validAnchors.contains(pos)) return true;
        if(invalidAnchors.contains(pos)) return false;

        final boolean[] allMatch = {true};
        shape.each((x, y) -> {
            if(!allMatch[0]) return;
            if(shape.get(x, y)){
                Tile other = world.tile(bottomLeft.x + x, bottomLeft.y + y);
                if(other == null || other.floor() != this){
                    allMatch[0] = false;
                }
            }
        });

        if(allMatch[0]){
            validAnchors.add(pos);
        }else{
            invalidAnchors.add(pos);
        }
        return allMatch[0];
    }

    /**
     * Checks if a complete pattern of this floor can be formed starting from the given bottom-left tile.
     * This check ensures that all required tiles exist and are not yet part of another drawn pattern.
     * @return true if the pattern is complete and available to be drawn.
     */
    private boolean isPatternComplete(Tile bottomLeft){
        if(!hasPatternAt(bottomLeft)) return false;

        final boolean[] claimed = {false};
        shape.each((x, y) -> {
            if(claimed[0]) return;
            if(shape.get(x, y)){
                Tile other = world.tile(bottomLeft.x + x, bottomLeft.y + y);
                if(other != null && claimedTiles.contains(other.pos())){
                    claimed[0] = true;
                }
            }
        });

        return !claimed[0];
    }

    /**
     * For a given tile, finds the bottom-left anchor of the pattern it belongs to, if any.
     * It iterates through all possible relative positions within the shape to see if the given
     * tile can be part of a valid, complete pattern.
     * @return The anchor tile, or null if the tile is not part of a complete pattern.
     */
    private Tile findPatternAnchorFor(Tile tile){
        if(tile == null) return null;
        for(int dx = 0; dx < shape.width(); dx++){
            for(int dy = 0; dy < shape.height(); dy++){
                if(shape.get(dx, dy)){
                    Tile potentialAnchor = tile.nearby(-dx, -dy);
                    if(isPatternComplete(potentialAnchor)){
                        return potentialAnchor;
                    }
                }
            }
        }
        return null;
    }

    private void beginDraw(){
        long frameId = Core.graphics.getFrameId();
        if(frameId != lastFrameId){
            claimedTiles.clear();
            edgeCache.clear();
            validAnchors.clear();
            invalidAnchors.clear();
            lastFrameId = frameId;
        }
    }

    private void drawPattern(Tile bottomLeft){
        Mathf.rand.setSeed(bottomLeft.pos());
        Draw.rect(variantRegions[Mathf.randomSeed(bottomLeft.pos(), 0, Math.max(0, variantRegions.length - 1))],
        bottomLeft.worldx() + (shape.width() - 1) * tilesize / 2f,
        bottomLeft.worldy() + (shape.height() - 1) * tilesize / 2f);

        if(drawPatternEdges) drawPatternEdges(bottomLeft);
    }

    @Override
    public void drawBase(Tile tile){
        beginDraw();

        if(claimedTiles.contains(tile.pos())){
            drawEdges(tile);
            drawOverlay(tile);
            return;
        }

        Tile bottomLeft = findPatternAnchorFor(tile);

        if(bottomLeft == null){
            if(parent instanceof Floor p && p.variants > 0){
                Draw.rect(p.variantRegions[p.variant(tile.x, tile.y)], tile.worldx(), tile.worldy());
            }
        }else{
            claimArea(bottomLeft);

            shape.each((x, y) -> {
                if(shape.get(x, y)){
                    Tile other = world.tile(bottomLeft.x + x, bottomLeft.y + y);
                    if(other != null && parent instanceof Floor p && p.variants > 0){
                        Draw.rect(p.variantRegions[p.variant(other.x, other.y)], other.worldx(), other.worldy());
                    }
                }
            });

            if(!drawOnTop){
                drawPattern(bottomLeft);
            }
        }

        drawEdges(tile);
        drawOverlay(tile);
    }

    @Override
    public void drawOverlay(Tile tile){
        if(drawOnTop){
            beginDraw();

            Tile bottomLeft = findPatternAnchorFor(tile);
            if(bottomLeft != null && tile == bottomLeft){
                drawPattern(bottomLeft);
            }
        }

        super.drawOverlay(tile);
    }

    /** Draws blended edges around the entire perimeter of the composite pattern. */
    private void drawPatternEdges(Tile bottomLeft){
        shape.each((x, y) -> {
            if(!shape.get(x, y)) return;

            Tile tileInPattern = world.tile(bottomLeft.x + x, bottomLeft.y + y);
            if(tileInPattern == null) return;

            for(int i = 0; i < 8; i++){
                var point = Geometry.d8[i];
                Tile neighbor = tileInPattern.nearby(point.x, point.y);

                int nx = x + point.x;
                int ny = y + point.y;

                boolean isNeighborInShape = neighbor != null &&
                (nx >= 0 && nx < shape.width()) &&
                (ny >= 0 && ny < shape.height()) &&
                shape.get(nx, ny);

                if(neighbor != null && !isNeighborInShape && doEdge(tileInPattern, neighbor, neighbor.floor())){
                    TextureRegion region = edge(neighbor.floor(), 1 - point.x, 1 - point.y);
                    if(region != null) Draw.rect(region, tileInPattern.worldx(), tileInPattern.worldy());
                }
            }
        });
    }

    /** Re-implementation of private Floor.doEdge. */
    public boolean doEdge(Tile tile, Tile otherTile, Floor other){
        return (other.realBlendId(otherTile) > this.realBlendId(tile) || getEdges(this.parent.asFloor()) == null);
    }

    /** Re-implementation of private Floor.edge. */
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

    /** Claims all tiles in the area for this pattern, preventing others from drawing over it. */
    private void claimArea(Tile bottomLeft){
        if(bottomLeft == null) return;

        shape.each((x, y) -> {
            if(shape.get(x, y)){
                Tile other = world.tile(bottomLeft.x + x, bottomLeft.y + y);
                if(other != null){
                    claimedTiles.add(other.pos());
                }
            }
        });
    }

    @Override
    public boolean updateRender(Tile tile){
        for(int dx = -shape.width() + 1; dx < shape.width(); dx++){
            for(int dy = -shape.height() + 1; dy < shape.height(); dy++){
                Tile other = tile.nearby(dx, dy);
                if(other != null && other.floor() == this){
                    return true;
                }
            }
        }
        return false;
    }
}
