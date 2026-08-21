package omaloon.world.blocks.environment;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.graphics.MultiPacker.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.blocks.environment.*;
import omaloon.editor.*;
import omaloon.world.patterns.*;

import static mindustry.Vars.*;

public class PatternOreBlock extends OreBlock implements Patterned{
    public Pattern pattern;
    public boolean drawParentUnder = false;
    public boolean isPattern = false;
    public boolean usePatternName = false;
    public boolean placeWholeShape = false;

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
    public void createIcons(MultiPacker packer){
        super.createIcons(packer);
        if(pattern == null) return;

        Seq<Pattern> list = (pattern instanceof MultiPattern mp) ? mp.patterns : Seq.with(pattern);
        for(Pattern p : list){
            for(int i = 0; i < Math.max(1, p.variants); i++){
                String pName = p.name + (p.variants > 0 ? (i + 1) : "");
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