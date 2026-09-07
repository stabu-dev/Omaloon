package omaloon.world.patterns;

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
import omaloon.editor.*;

import static mindustry.Vars.*;

/**
 * Interface implemented by env blocks supporting multi-tile pattern placement, configuration, and rendering.
 * @author stabu_
 */
public interface Patterned{
    /** Deselected: tiles resolve by largest-fit. */
    int patternAuto = -1;
    /** Explicitly bare tile. */
    int patternNone = -2;

    Pattern getPattern();

    default Pattern getPattern(Tile tile){
        if(tile == null) return getPattern();
        int cfg = patternConfig(tile);
        if(cfg == patternNone) return null;
        if(getPattern() instanceof MultiPattern mp && cfg >= 0 && cfg < mp.patterns.size) return mp.get(cfg);
        Pattern auto = PatternManager.getAnchorPattern(tile, this);
        if(auto != null) return auto;
        return getPattern();
    }

    default boolean isPattern(){
        return false;
    }

    default boolean usePatternName(){
        return false;
    }

    default int patternConfig(Tile tile){
        if(tile == null) return patternAuto;
        int raw = this instanceof Block b && b.isOverlay() ? tile.overlayData : tile.extraData;
        return raw == 0 ? patternAuto : raw == 1 ? patternNone : raw - 2;
    }

    default void setPatternConfig(Tile tile, int value){
        if(tile == null) return;
        int raw = value == patternAuto ? 0 : value == patternNone ? 1 : value + 2;
        if(this instanceof Block b && b.isOverlay()){
            tile.overlayData = (byte)raw;
        }else{
            tile.extraData = raw;
        }
    }

    default boolean wholeShape(){
        return false;
    }

    default void setWholeShape(boolean val){}

    default Tile getAnchorIfComplete(Tile tile){
        if(tile == null || getPattern() == null || getPattern(tile) == null) return null;
        Tile anchor = PatternManager.getAnchor(tile, this);
        if(anchor != null){
            if(PatternManager.isPatternComplete(this, anchor)) return anchor;
            PatternManager.updateAround(tile, this);
        }
        return null;
    }

    default TextureRegion[] slicePatternRegions(TextureRegion[] baseRegions, int baseVariants){
        Pattern pattern = getPattern();
        if(pattern == null) return baseRegions;
        Seq<Pattern> list = (pattern instanceof MultiPattern mp) ? mp.patterns : Seq.with(pattern);
        int totalAreaVariants = 0;
        for(Pattern p : list){
            totalAreaVariants += (p.shape.width() * p.shape.height()) * Math.max(1, p.variants);
        }

        TextureRegion[] newRegions = new TextureRegion[baseVariants + totalAreaVariants];
        System.arraycopy(baseRegions, 0, newRegions, 0, baseVariants);

        int idx = baseVariants;
        for(Pattern p : list){
            if(p.region == null || !p.region.found()) continue;
            int tilePixelWidth = p.region.width / p.shape.width();
            int tilePixelHeight = p.region.height / p.shape.height();
            int pVariants = Math.max(1, p.variants);

            for(int v = 0; v < pVariants; v++){
                TextureRegion sourceRegion = (p.variantRegions != null && v < p.variantRegions.length) ? p.variantRegions[v] : p.region;
                TextureRegion[][] slices = sourceRegion.split(tilePixelWidth, tilePixelHeight);

                for(int y = 0; y < p.shape.height(); y++){
                    for(int x = 0; x < p.shape.width(); x++){
                        int textureY = (p.shape.height() - 1) - y;
                        TextureRegion slice = new TextureRegion(slices[x][textureY]);
                        slice.scale = p.region.scale;

                        float halfTexelU = 0.5f / slice.texture.width;
                        float halfTexelV = 0.5f / slice.texture.height;
                        slice.u  += halfTexelU;
                        slice.v  += halfTexelV;
                        slice.u2 -= halfTexelU;
                        slice.v2 -= halfTexelV;

                        newRegions[idx++] = slice;
                    }
                }
            }
        }
        return newRegions;
    }

    default int getSliceIndex(Pattern pattern, int relativeX, int relativeY, int variantIdx){
        int area = pattern.shape.width() * pattern.shape.height();
        int x = relativeX + pattern.shape.anchorX;
        int y = relativeY + pattern.shape.anchorY;
        return (variantIdx * area) + (y * pattern.shape.width()) + x;
    }

    default int getSliceOffset(MultiPattern mp, Pattern sub){
        if(mp == null || sub == null) return 0;
        int idx = mp.patterns.indexOf(sub);
        int max = idx >= 0 ? Math.min(idx, mp.patterns.size) : 0;
        int offset = 0;
        for(int i = 0; i < max; i++){
            Pattern p = mp.patterns.get(i);
            offset += (p.shape.width() * p.shape.height()) * Math.max(1, p.variants);
        }
        return offset;
    }

    default int patternVariant(int x, int y, int max){
        return Mathf.randomSeed(Point2.pack(x, y), 0, Math.max(0, max - 1));
    }

    default void drawPatternPlanRegion(BuildPlan plan){
        if(!(this instanceof Block block)) return;
        TextureRegion toDraw = block.variantRegions != null && block.variantRegions.length > 0 ? block.variantRegions[0] : (block.region != null && block.region.found() ? block.region : block.fullIcon);
        Draw.rect(toDraw, plan.drawx(), plan.drawy());
    }

    // TODO: make hollow pattern slice rendering across empty/non-pattern tiles (e.g. arches) work
    default void drawSlice(TextureRegion[] variantRegions, int variants, Tile tile, Tile anchor){
        int relX = tile.x - anchor.x;
        int relY = tile.y - anchor.y;
        Pattern topPattern = getPattern();
        Pattern activePattern = PatternManager.getAnchorPattern(anchor, this);
        if(activePattern == null) activePattern = getPattern(anchor);
        int baseVariants = Math.max(1, variants);
        int offset = (topPattern instanceof MultiPattern mp) ? getSliceOffset(mp, activePattern) : 0;
        int vIdx = activePattern.variants > 0 ? patternVariant(anchor.x, anchor.y, activePattern.variants) : 0;
        int sliceIdx = baseVariants + offset + getSliceIndex(activePattern, relX, relY, vIdx);
        Draw.rect(variantRegions[sliceIdx], tile.worldx(), tile.worldy(), tilesize, tilesize);
    }

    default void showPatternEdit(Table table){
        if(!(this instanceof Block block)) return;
        Pattern pattern = getPattern();
        if(pattern == null) return;

        table.table(t -> {
            if(ui.editor.isShown()){
                t.button(Icon.resize, Styles.clearNoneTogglei, () -> setWholeShape(!wholeShape()))
                .update(b -> b.setChecked(wholeShape()))
                .size(50f).tooltip("@editor.omaloon-whole-shape");
            }

            t.add().growX();

            if(!isPattern()){
                TextureRegion baseIcon = block.fullIcon != null && block.fullIcon.found() ? block.fullIcon : block.uiIcon;
                t.button(new TextureRegionDrawable(baseIcon), Styles.clearNoneTogglei, 32f, () -> {
                    block.lastConfig = block.lastConfig instanceof Integer v && v == patternNone ? patternAuto : patternNone;
                    setSelectedConfig();
                })
                .update(b -> b.setChecked(block.lastConfig instanceof Integer i && i == patternNone))
                .size(50f).tooltip(Core.bundle.get("block." + block.name + ".name", block.name));
            }

            Seq<Pattern> list = pattern instanceof MultiPattern mp ? mp.patterns : Seq.with(pattern);
            for(int i = 0; i < list.size; i++){
                final int idx = i;
                Pattern sub = list.get(i);
                String label = usePatternName() ? sub.localizedName : Core.bundle.get("block." + block.name + ".name", block.name);
                t.button(new TextureRegionDrawable(sub.icon()), Styles.clearNoneTogglei, 32f, () -> {
                    block.lastConfig = block.lastConfig instanceof Integer val && val == idx ? patternAuto : idx;
                    setSelectedConfig();
                })
                .size(50f).tooltip(label)
                .update(b -> b.setChecked(block.lastConfig instanceof Integer val && val == idx));
            }
        }).growX().row();

        setSelectedConfig();
    }

    default void setSelectedConfig(){
        if(!(this instanceof Block block)) return;
        Pattern pattern = getPattern();
        if(block.lastConfig instanceof Integer idx){
            String baseName = Core.bundle.get("block." + block.name + ".name", block.name);
            if(idx < 0 || pattern == null){
                block.localizedName = (idx == patternAuto && pattern != null) ? baseName + " " + Core.bundle.get("pattern.auto") : baseName;
                block.uiIcon.set(block.fullIcon);
            }else{
                block.localizedName = usePatternName() ? ((pattern instanceof MultiPattern mp) ? mp.localizedName(idx) : pattern.localizedName()) : baseName;
                block.uiIcon.set((pattern instanceof MultiPattern mp) ? mp.icon(idx) : pattern.icon());
            }
        }
    }
}