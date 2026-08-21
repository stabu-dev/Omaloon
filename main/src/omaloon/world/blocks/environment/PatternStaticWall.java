package omaloon.world.blocks.environment;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
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

import static mindustry.Vars.*;

public class PatternStaticWall extends StaticWall implements Patterned{
    public Pattern pattern;
    public boolean drawOnTop = true;
    public boolean isPattern = false;
    public boolean usePatternName = false;
    public boolean drawParentUnder = false;
    public boolean placeWholeShape = false;

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
        }
        lastConfig = isPattern ? 0 : -1;
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
        drawPatternPlanRegion(plan, list);
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
        Tile anchor = getAnchorIfComplete(tile);

        if(anchor != null){
            if(!drawOnTop){
                if(drawParentUnder) drawBaseTile(tile);
                else{
                    Draw.rect(region, tile.worldx(), tile.worldy());
                }
                drawSlice(variantRegions, variants, tile, anchor);
            }else{
                drawBaseTile(tile);
                drawSlice(variantRegions, variants, tile, anchor);
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

    boolean eq(int rx, int ry){
        return rx < world.width() - 1 && ry < world.height() - 1
        && world.tile(rx + 1, ry).block() == this
        && world.tile(rx, ry + 1).block() == this
        && world.tile(rx, ry).block() == this
        && world.tile(rx + 1, ry + 1).block() == this;
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