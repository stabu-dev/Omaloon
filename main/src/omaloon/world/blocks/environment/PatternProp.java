package omaloon.world.blocks.environment;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.blocks.environment.*;
import omaloon.editor.*;
import omaloon.world.patterns.*;

import static mindustry.Vars.*;

public class PatternProp extends Prop implements Patterned{
    public Pattern pattern;
    public boolean drawParentUnder = false;
    public boolean isPattern = false;
    public boolean usePatternName = false;
    public boolean placeWholeShape = false;

    public PatternProp(String name){
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
        lastConfig = isPattern ? 0 : -1;
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
                t.button(new TextureRegionDrawable(icons()[0]), Styles.clearNoneTogglei, 32f, () -> {
                    lastConfig = -1;
                    setSelectedName();
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
                        setSelectedName();
                    })
                    .size(50f).tooltip(sub.localizedName())
                    .update(b -> b.setChecked(lastConfig instanceof Integer val && val == idx));
                }else{
                    t.button(sub.name, Styles.flatTogglet, () -> {
                        lastConfig = idx;
                        setSelectedName();
                    })
                    .size(50f).tooltip(sub.localizedName())
                    .update(b -> b.setChecked(lastConfig instanceof Integer val && val == idx));
                }
            }
        }).growX().padBottom(2f).row();

        if(ui.editor.isShown()){
            CheckBox wholeShapeCheck = new CheckBox("@editor.omaloon-whole-shape");
            wholeShapeCheck.update(() -> wholeShapeCheck.setChecked(placeWholeShape));
            wholeShapeCheck.changed(() -> placeWholeShape = wholeShapeCheck.isChecked());
            table.add(wholeShapeCheck).padBottom(2f).row();
        }
        setSelectedName();
    }

    private void setSelectedName(){
        if(lastConfig instanceof Integer idx){
            if(idx == -1){
                localizedName = Core.bundle.get("block." + name + ".name", name);
            }else if(pattern instanceof MultiPattern mp && idx < mp.patterns.size){
                localizedName = mp.patterns.get(idx).localizedName();
            }else{
                localizedName = pattern.localizedName();
            }
        }
    }

    @Override
    public Object getConfig(Tile tile){
        return tile.extraData;
    }

    @Override
    public void editorPicked(Tile tile){
        lastConfig = tile.extraData;
        setSelectedName();
    }

    @Override
    public void onPicked(Tile tile){
        lastConfig = tile.extraData;
        setSelectedName();
    }

    @Override
    public void placeEnded(Tile tile, @Nullable Unit builder, int rotation, @Nullable Object config){
        if(config instanceof Integer i){
            tile.extraData = i;
            if(OlEditorExtension.isWholeShapeActive() && !PatternManager.wholeShapePlacing){
                PatternManager.placeWholeShape(tile, this);
            }
        }
        PatternManager.updateAround(tile, this);
    }

    @Override
    public boolean wholeShape(){
        return placeWholeShape;
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
        Draw.z(layer);
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
        if(tile != null && !isPattern && tile.extraData < 0) return null;
        if(pattern instanceof MultiPattern mp && tile != null && tile.extraData >= 0 && tile.extraData < mp.patterns.size){
            return mp.get(tile.extraData);
        }
        return pattern;
    }
}