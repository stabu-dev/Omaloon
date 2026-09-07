package omaloon.world.blocks.environment;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.blocks.environment.*;
import omaloon.editor.*;
import omaloon.world.patterns.*;

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
        }
        lastConfig = isPattern ? patternAuto : patternNone;
    }

    @Override
    public void loadIcon(){
        super.loadIcon();
        if(isPattern && pattern != null){
            pattern.loadRegion();
            uiIcon = new TextureRegion(pattern.region);
        }else{
            uiIcon = new TextureRegion(fullIcon);
        }
    }

    @Override
    public void load(){
        super.load();
        if(pattern != null){
            pattern.load();
            variantRegions = slicePatternRegions(variantRegions, Math.max(1, variants));
        }
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list){
        drawPatternPlanRegion(plan);
    }

    @Override
    public void buildEditorConfig(Table table){
        showPatternEdit(table);
    }

    @Override
    public Object getConfig(Tile tile){
        return patternConfig(tile);
    }

    @Override
    public void editorPicked(Tile tile){
        lastConfig = patternConfig(tile);
        setSelectedConfig();
    }

    @Override
    public void onPicked(Tile tile){
        lastConfig = patternConfig(tile);
        setSelectedConfig();
    }

    @Override
    public void placeEnded(Tile tile, @Nullable Unit builder, int rotation, @Nullable Object config){
        if(config instanceof Integer i){
            setPatternConfig(tile, i);
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
    public void setWholeShape(boolean val){
        this.placeWholeShape = val;
    }

    @Override
    public boolean usePatternName(){
        return usePatternName;
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
            drawSlice(variantRegions, variants, tile, anchor);
        }else{
            drawBaseTile(tile);
        }
    }

    protected void drawBaseTile(Tile tile){
        int baseVariants = Math.max(1, variants);
        Draw.rect(variantRegions[Mathf.randomSeed(tile.pos(), 0, baseVariants - 1)], tile.worldx(), tile.worldy());
    }

    @Override
    public boolean isPattern(){
        return isPattern;
    }

    @Override
    public Pattern getPattern(){
        return pattern;
    }
}