package omaloon.world.blocks.environment;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.scene.style.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.graphics.MultiPacker.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.blocks.environment.*;
import omaloon.world.patterns.*;

import static mindustry.Vars.tilesize;

public class PatternOreBlock extends OreBlock implements Patterned{
    public Pattern pattern;
    public boolean drawParentUnder = false;
    public boolean isPattern = false;
    public boolean usePatternName = false;

    public PatternOreBlock(String name, Item ore){
        super(name, ore);
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
            Pattern topPattern = getPattern();
            Pattern activePattern = getPattern(anchor);
            int baseVariants = Math.max(1, variants);
            int offset = (topPattern instanceof MultiPattern mp) ? mp.getSliceOffset(activePattern) : 0;
            int vIdx = activePattern.variants > 0 ? activePattern.variant(anchor.x, anchor.y, activePattern.variants) : 0;
            int sliceIdx = baseVariants + offset + activePattern.getSliceIndex(relX, relY, vIdx);

            Draw.rect(variantRegions[sliceIdx], tile.worldx(), tile.worldy(), tilesize, tilesize);
        }else{
            drawBaseTile(tile);
        }
    }

    protected void drawBaseTile(Tile tile){
        int baseVariants = Math.max(1, variants);
        Draw.rect(variantRegions[Mathf.randomSeed(tile.pos(), 0, baseVariants - 1)], tile.worldx(), tile.worldy());
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
