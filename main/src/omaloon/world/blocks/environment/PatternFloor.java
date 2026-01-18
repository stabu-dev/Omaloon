package omaloon.world.blocks.environment;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import mindustry.graphics.*;
import mindustry.graphics.MultiPacker.*;
import mindustry.world.*;
import mindustry.world.blocks.environment.*;
import omaloon.world.patterns.*;

import static mindustry.Vars.*;

public class PatternFloor extends Floor implements Patterned{
    public Pattern pattern;
    public boolean drawPatternEdges = true;
    public boolean drawOnTop = false;
    public boolean isPattern = false;
    public boolean drawParentUnder = false;

    public PatternFloor(String name){
        super(name);
    }

    public PatternFloor(String name, int variants){
        super(name, variants);
    }

    @Override
    public void init(){
        super.init();
        if(isPattern && pattern != null){
            localizedName = pattern.localizedName();
            description = pattern.description();
        }
    }

    @Override
    public TextureRegion[] icons(){
        if(isPattern && pattern != null) return new TextureRegion[]{pattern.region};
        return super.icons();
    }

    @Override
    public void loadIcon(){
        if(isPattern && pattern != null){
            pattern.loadRegion();
            fullIcon = pattern.region;
            uiIcon = fullIcon;
        }else{
            super.loadIcon();
        }
    }

    @Override
    public void load(){
        super.load();
        if(pattern != null){
            pattern.load();
            int baseVariants = Math.max(1, variants);
            int area = pattern.shape.width() * pattern.shape.height();
            int pVariants = Math.max(1, pattern.variants);
            
            TextureRegion[] newRegions = new TextureRegion[baseVariants + area * pVariants];
            System.arraycopy(variantRegions, 0, newRegions, 0, baseVariants);
            
            int idx = baseVariants;
            for(int v = 0; v < pVariants; v++){
                for(int y = 0; y < pattern.shape.height(); y++){
                    for(int x = 0; x < pattern.shape.width(); x++){
                        int textureY = (pattern.shape.height() - 1) - y;
                        newRegions[idx++] = pattern.slicedRegions[v][x][textureY];
                    }
                }
            }
            variantRegions = newRegions;
        }
    }

    @Override
    public int variant(int x, int y){
        return Mathf.randomSeed(Point2.pack(x, y), 0, Math.max(0, variants - 1));
    }

    @Override
    public void drawMain(Tile tile){
        Tile anchor = getAnchorIfComplete(tile);
        if(anchor != null && !drawOnTop){
            if(drawParentUnder){
                Draw.rect(variantRegions[variant(tile.x, tile.y)], tile.worldx(), tile.worldy());
            }
            int relX = tile.x - anchor.x;
            int relY = tile.y - anchor.y;
            int vIdx = pattern.variants > 0 ? pattern.variant(anchor.x, anchor.y, pattern.variants) : 0;
            int sliceIdx = Math.max(1, variants) + pattern.getSliceIndex(relX, relY, vIdx);
            Draw.rect(variantRegions[sliceIdx], tile.worldx(), tile.worldy(), tilesize + 0.01f, tilesize + 0.01f);
        }else{
            super.drawMain(tile);
        }
    }

    @Override
    public void floorChanged(Tile tile){
        super.floorChanged(tile);
        PatternManager.updateAround(tile, this);
    }

    @Override
    public void drawBase(Tile tile){
        super.drawBase(tile);
        
        if(drawOnTop){
            Tile anchor = getAnchorIfComplete(tile);
            if(anchor != null){
                int relX = tile.x - anchor.x;
                int relY = tile.y - anchor.y;
                int vIdx = pattern.variants > 0 ? pattern.variant(anchor.x, anchor.y, pattern.variants) : 0;
                int sliceIdx = Math.max(1, variants) + pattern.getSliceIndex(relX, relY, vIdx);
                Draw.rect(variantRegions[sliceIdx], tile.worldx(), tile.worldy(), tilesize + 0.01f, tilesize + 0.01f);
                
                if(drawPatternEdges) drawEdges(tile);
                drawOverlay(tile);
            }
        }else if(drawPatternEdges){
            drawEdges(tile);
        }
    }

    protected Tile getAnchorIfComplete(Tile tile){
        if(tile == null || pattern == null) return null;
        Tile anchor = PatternManager.getAnchor(tile, this);
        if(anchor != null && PatternManager.isPatternComplete(this, anchor)) return anchor;
        return null;
    }

    @Override
    public Pattern getPattern(){
        return pattern;
    }
}