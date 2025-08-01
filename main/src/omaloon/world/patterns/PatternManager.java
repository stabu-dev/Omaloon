package omaloon.world.patterns;

import arc.struct.*;
import mindustry.world.*;

import static mindustry.Vars.*;

public class PatternManager{
    private static final IntMap<Tile> tileToAnchorMap = new IntMap<>();

    //region Anchors/State
    private static final ObjectMap<Block, IntSet> claimedTiles = new ObjectMap<>();
    private static final ObjectMap<Block, IntSet> validAnchors = new ObjectMap<>();
    private static final ObjectMap<Block, IntSet> invalidAnchors = new ObjectMap<>();
    //endregion

    public static void init(){
        resolveWorld();
    }

    public static void updateAround(Tile tile){
        // As requested: no optimizations. Recalculate the entire world state on any change.
        resolveWorld();
    }

    public static void resolveWorld(){
        tileToAnchorMap.clear();
        claimedTiles.clear();
        validAnchors.clear();
        invalidAnchors.clear();

        for(int y = 0; y < world.height(); y++){
            for(int x = 0; x < world.width(); x++){
                Tile tile = world.tile(x, y);
                if(tile == null || !(tile.floor() instanceof Patterned p)) continue;

                if(claimedTiles.get(p.getParent(), IntSet::new).contains(tile.pos())){
                    continue;
                }

                Tile anchor = findPatternAnchorFor(tile);

                if(anchor != null){
                    Patterned anchorPattern = (Patterned) anchor.floor();
                    claimArea(anchor);

                    anchorPattern.getShape().each((sx, sy) -> {
                        if(anchorPattern.getShape().get(sx, sy)){
                            Tile member = world.tile(anchor.x + sx, anchor.y + sy);
                            if(member != null){
                                tileToAnchorMap.put(member.pos(), anchor);
                            }
                        }
                    });
                }
            }
        }
        for(int i = 0; i < world. tiles. width * world. tiles. height; i++){
            Tile tile = world.tiles.geti(i);
            if(tile != null) renderer.blocks.floor.recacheTile(tile);
        }
    }

    private static Tile findPatternAnchorFor(Tile tile){
        if(!(tile.floor() instanceof Patterned p)) return null;
        for(int dx = 0; dx < p.getShape().width(); dx++){
            for(int dy = 0; dy < p.getShape().height(); dy++){
                if(p.getShape().get(dx, dy)){
                    Tile potentialAnchor = tile.nearby(-dx, -dy);
                    if(isPatternComplete(potentialAnchor)){
                        return potentialAnchor;
                    }
                }
            }
        }
        return null;
    }

    private static boolean isPatternComplete(Tile bottomLeft){
        if(bottomLeft == null || !(bottomLeft.floor() instanceof Patterned p)) return false;
        if(!hasPatternAt(bottomLeft)) return false;

        final boolean[] claimed = {false};
        p.getShape().each((x, y) -> {
            if(claimed[0]) return;
            if(p.getShape().get(x, y)){
                Tile other = world.tile(bottomLeft.x + x, bottomLeft.y + y);
                if(other != null && claimedTiles.get(p.getParent(), IntSet::new).contains(other.pos())){
                    claimed[0] = true;
                }
            }
        });
        return !claimed[0];
    }

    private static boolean hasPatternAt(Tile bottomLeft){
        if(bottomLeft == null || !(bottomLeft.floor() instanceof Patterned p)) return false;
        int pos = bottomLeft.pos();
        if(validAnchors.get(p.getParent(), IntSet::new).contains(pos)) return true;
        if(invalidAnchors.get(p.getParent(), IntSet::new).contains(pos)) return false;

        final boolean[] allMatch = {true};
        p.getShape().each((x, y) -> {
            if(!allMatch[0]) return;
            if(p.getShape().get(x, y)){
                Tile other = world.tile(bottomLeft.x + x, bottomLeft.y + y);
                if(other == null || other.floor() != p){
                    allMatch[0] = false;
                }
            }
        });

        if(allMatch[0]){
            validAnchors.get(p.getParent(), IntSet::new).add(pos);
        }else{
            invalidAnchors.get(p.getParent(), IntSet::new).add(pos);
        }
        return allMatch[0];
    }

    private static void claimArea(Tile bottomLeft){
        if(bottomLeft == null || !(bottomLeft.floor() instanceof Patterned p)) return;
        IntSet set = claimedTiles.get(p.getParent(), IntSet::new);
        p.getShape().each((x, y) -> {
            if(p.getShape().get(x, y)){
                Tile other = world.tile(bottomLeft.x + x, bottomLeft.y + y);
                if(other != null){
                    set.add(other.pos());
                }
            }
        });
    }

    public static Tile getAnchor(Tile tile){
        return tileToAnchorMap.get(tile.pos());
    }
}