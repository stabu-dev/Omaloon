package omaloon.world.blocks.environment;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import mindustry.graphics.*;
import mindustry.graphics.MultiPacker.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.environment.*;
import omaloon.world.patterns.*;

import static mindustry.Vars.tilesize;

public class PatternOreBlock extends OreBlock implements Patterned{
    public Pattern pattern;
    public boolean drawParentUnder = false;
    public boolean isPattern = false;

    public PatternOreBlock(String name, Item ore){
        super(name, ore);
    }

    @Override
    public void init(){
        super.init();
        /*if(isPattern && pattern != null){
            localizedName = pattern.localizedName();
            description = pattern.description();
        }*/
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
    public void createIcons(MultiPacker packer){
        super.createIcons(packer);
        if(pattern == null) return;

        for(int i = 0; i < Math.max(1, pattern.variants); i++){
            String pName = pattern.name + (pattern.variants > 0 ? (i + 1) : "");
            if(Core.atlas.has(pName)){
                PixmapRegion shadowRegion = Core.atlas.getPixmap(pName);
                Pixmap image = shadowRegion.crop();

                int offset = Math.max(1, image.width / tilesize - 1);
                int shadowColor = Color.rgba8888(0, 0, 0, 0.3f);

                for(int x = 0; x < image.width; x++){
                    for(int y = offset; y < image.height; y++){
                        if(shadowRegion.getA(x, y) == 0 && shadowRegion.getA(x, y - offset) != 0){
                            image.setRaw(x, y, shadowColor);
                        }
                    }
                }

                packer.add(PageType.environment, pName, image);
                image.dispose();
            }
        }
    }

    @Override
    public void floorChanged(Tile tile){
        super.floorChanged(tile);
        PatternManager.updateAround(tile, this);
    }

    @Override
    public void drawBase(Tile tile){
        Tile anchor = getAnchorIfComplete(tile);
        if(anchor != null){
            if(drawParentUnder){
                drawBaseTile(tile);
            }
            int relX = tile.x - anchor.x;
            int relY = tile.y - anchor.y;
            int vIdx = pattern.variants > 0 ? pattern.variant(anchor.x, anchor.y, pattern.variants) : 0;
            int sliceIdx = Math.max(1, variants) + pattern.getSliceIndex(relX, relY, vIdx);

            Draw.rect(variantRegions[sliceIdx], tile.worldx(), tile.worldy(), tilesize + 0.01f, tilesize + 0.01f);
        }else{
            drawBaseTile(tile);
        }
    }

    protected void drawBaseTile(Tile tile){
        int baseVariants = Math.max(1, variants);
        Draw.rect(variantRegions[Mathf.randomSeed(tile.pos(), 0, baseVariants - 1)], tile.worldx(), tile.worldy());
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
