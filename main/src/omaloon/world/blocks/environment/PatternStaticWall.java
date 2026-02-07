package omaloon.world.blocks.environment;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import mindustry.world.*;
import mindustry.world.blocks.environment.*;
import omaloon.world.patterns.*;

import static mindustry.Vars.*;

public class PatternStaticWall extends StaticWall implements Patterned{
    public Pattern pattern;
    public boolean drawOnTop = true;
    public boolean isPattern = false;
    public boolean drawParentUnder = false;

    public PatternStaticWall(String name){
        super(name);
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
    public TextureRegion[] icons(){
        if(isPattern && pattern != null) return new TextureRegion[]{pattern.region};
        return super.icons();
    }

    @Override
    public void blockChanged(Tile tile){
        super.blockChanged(tile);
        PatternManager.updateAround(tile, this);
    }

    @Override
    public void drawBase(Tile tile){
        Tile anchor = getAnchorIfComplete(tile);

        if(anchor != null){
            if(!drawOnTop){
                if(drawParentUnder) drawBaseTile(tile);
                else{
                    Draw.rect(region, tile.worldx(), tile.worldy());
                }
                drawSlice(tile, anchor);
            }else{
                drawBaseTile(tile);
                drawSlice(tile, anchor);
            }

            if(!drawOnTop && tile.overlay().wallOre){
                tile.overlay().drawBase(tile);
            }
        }
    }

    protected void drawBaseTile(Tile tile){
        int rx = tile.x / 2 * 2;
        int ry = tile.y / 2 * 2;

        if(Core.atlas.isFound(large) && eq(rx, ry) && Mathf.randomSeed(Point2.pack(rx, ry)) < 0.5 && split.length >= 2 && split[0].length >= 2){
            Draw.rect(split[tile.x % 2][1 - tile.y % 2], tile.worldx(), tile.worldy());
        }else{
            int baseVariants = Math.max(1, variants);
            Draw.rect(variantRegions[Mathf.randomSeed(tile.pos(), 0, baseVariants - 1)], tile.worldx(), tile.worldy());
        }

        if(tile.overlay().wallOre){
            tile.overlay().drawBase(tile);
        }
    }

    protected void drawSlice(Tile tile, Tile anchor){
        int baseVariants = Math.max(1, variants);
        int relX = tile.x - anchor.x;
        int relY = tile.y - anchor.y;
        int vIdx = pattern.variants > 0 ? pattern.variant(anchor.x, anchor.y, pattern.variants) : 0;
        int sliceIdx = baseVariants + pattern.getSliceIndex(relX, relY, vIdx);
        Draw.rect(variantRegions[sliceIdx], tile.worldx(), tile.worldy(), tilesize + 0.01f, tilesize + 0.01f);
    }

    protected Tile getAnchorIfComplete(Tile tile){
        if(tile == null || pattern == null) return null;
        Tile anchor = PatternManager.getAnchor(tile, this);
        if(anchor != null && PatternManager.isPatternComplete(this, anchor)) return anchor;
        return null;
    }

    boolean eq(int rx, int ry){
        return rx < world.width() - 1 && ry < world.height() - 1
        && world.tile(rx + 1, ry).block() == this
        && world.tile(rx, ry + 1).block() == this
        && world.tile(rx, ry).block() == this
        && world.tile(rx + 1, ry + 1).block() == this;
    }

    @Override
    public Pattern getPattern(){
        return pattern;
    }
}