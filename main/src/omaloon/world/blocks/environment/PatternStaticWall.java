package omaloon.world.blocks.environment;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.style.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.blocks.environment.*;
import omaloon.world.patterns.*;

import static mindustry.Vars.*;

public class PatternStaticWall extends StaticWall implements Patterned{
    public Pattern pattern;
    public boolean drawOnTop = true;
    public boolean isPattern = false;
    public boolean usePatternName = false;
    public boolean drawParentUnder = false;

    public PatternStaticWall(String name){
        super(name);
        saveData = saveConfig = true;
        editorConfigurable = true;
    }

    @Override
    public void init(){
        super.init();
        if(usePatternName && pattern != null){
            localizedName = pattern.localizedName();
            description = pattern.description();
        }
        lastConfig = -1;
        editorConfigurable = !isPattern || pattern instanceof MultiPattern;
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

            Seq<Pattern> list = (pattern instanceof MultiPattern mp) ? mp.patterns : Seq.with(pattern);
            int totalAreaVariants = 0;
            for(Pattern p : list){
                totalAreaVariants += (p.shape.width() * p.shape.height()) * Math.max(1, p.variants);
            }

            TextureRegion[] newRegions = new TextureRegion[baseVariants + totalAreaVariants];
            System.arraycopy(variantRegions, 0, newRegions, 0, baseVariants);

            int idx = baseVariants;
            for(Pattern p : list){
                int pVariants = Math.max(1, p.variants);
                for(int v = 0; v < pVariants; v++){
                    for(int y = 0; y < p.shape.height(); y++){
                        for(int x = 0; x < p.shape.width(); x++){
                            int textureY = (p.shape.height() - 1) - y;
                            newRegions[idx++] = p.slicedRegions[v][x][textureY];
                        }
                    }
                }
            }
            variantRegions = newRegions;
        }
    }

    @Override
    public void buildEditorConfig(Table table){
        table.table(t -> {
            if(!isPattern){
                t.button(new TextureRegionDrawable(fullIcon), Styles.clearNoneTogglei, 32f, () -> {
                    lastConfig = -1;
                    localizedName = Core.bundle.get("block." + name + ".name", name);
                })
                .update(b -> b.setChecked(lastConfig instanceof Integer i && i == -1))
                .size(50f).tooltip(localizedName);
            }

            Seq<Pattern> list = pattern instanceof MultiPattern mp ? mp.patterns : Seq.with(pattern);
            for(int i = 0; i < list.size; i++){
                final int idx = i;
                Pattern sub = list.get(i);
                if(sub.region != null && sub.region.found()){
                    t.button(new TextureRegionDrawable(sub.region), Styles.clearNoneTogglei, 32f, () -> {
                        lastConfig = idx;
                        localizedName = sub.localizedName();
                    })
                    .size(50f).tooltip(sub.localizedName())
                    .update(b -> b.setChecked(lastConfig instanceof Integer val && val == idx));
                }else{
                    t.button(sub.name, Styles.flatTogglet, () -> {
                        lastConfig = idx;
                        localizedName = sub.localizedName();
                    })
                    .size(50f).tooltip(sub.localizedName())
                    .update(b -> b.setChecked(lastConfig instanceof Integer val && val == idx));
                }
            }
        }).growX().padBottom(2f).row();
    }

    @Override
    public Object getConfig(Tile tile){
        return (int)tile.data;
    }

    @Override
    public void editorPicked(Tile tile){
        lastConfig = (int)tile.data;
    }

    @Override
    public void placeEnded(Tile tile, @Nullable Unit builder, int rotation, @Nullable Object config){
        if(config instanceof Integer i){
            tile.data = i.byteValue();
        }
        PatternManager.updateAround(tile, this);
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
        Pattern topPattern = getPattern();
        Pattern activePattern = getPattern(anchor);
        int offset = (topPattern instanceof MultiPattern mp) ? mp.getSliceOffset(activePattern) : 0;
        int vIdx = activePattern.variants > 0 ? activePattern.variant(anchor.x, anchor.y, activePattern.variants) : 0;
        int sliceIdx = baseVariants + offset + activePattern.getSliceIndex(relX, relY, vIdx);
        Draw.rect(variantRegions[sliceIdx], tile.worldx(), tile.worldy(), tilesize, tilesize);
    }

    protected Tile getAnchorIfComplete(Tile tile){
        if(tile == null || pattern == null || getPattern(tile) == null) return null;
        Tile anchor = PatternManager.getAnchor(tile, this);
        if(anchor != null){
            if(PatternManager.isPatternComplete(this, anchor)) return anchor;
            PatternManager.updateAround(tile, this);
        }
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

    @Override
    public Pattern getPattern(Tile tile){
        if(tile != null && !isPattern && tile.data < 0) return null;
        if(pattern instanceof MultiPattern mp && tile != null && tile.data >= 0 && tile.data < mp.patterns.size){
            return mp.get(tile.data);
        }
        return pattern;
    }
}