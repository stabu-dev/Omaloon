package omaloon.tools.proc;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.util.*;

import mindustry.graphics.*;

import omaloon.*;
import omaloon.tools.*;
import omaloon.tools.GenAtlas.*;

import java.util.concurrent.*;

import static mindustry.Vars.*;
import static omaloon.tools.Tools.*;

/**
 * Mirrors the logic from mindustry.tools.Generators#generate("item-icons") but overwrites original regions.
 * @author stabu_
 */
public class StatusEffectProcessor implements Processor{
    @Override
    public void process(ExecutorService exec){
        content.statusEffects().each(OmaloonMod::isOmaloon, effect -> submit(exec, effect.name, () -> {
            String regionName = effect.name;

            GenRegion regionToModify = atlas.find(regionName);
            if(!regionToModify.found()){
                Log.warn("Base region '@' not found for status effect '@'. Skipping modification.", regionName, effect.name);
                return;
            }

            String originalRelativePath = regionToModify.relativePath;

            Pixmap originalPixmap;
            Pixmap baseCopy = null;
            Pixmap tintedPixmap = null;
            Pixmap containerPixmap = null;
            Pixmap outlinedPixmap = null;

            try{
                originalPixmap = regionToModify.pixmap();
                baseCopy = originalPixmap.copy();

                tintedPixmap = baseCopy.copy();
                Color effectColor = effect.color;
                Pixmap finalTintedPixmap = tintedPixmap;
                tintedPixmap.each((x, y) -> {
                    int current = finalTintedPixmap.getRaw(x, y);
                    finalTintedPixmap.setRaw(x, y, Color.muli(current, effectColor.rgba()));
                });

                int containerWidth = tintedPixmap.width + 3 * 2;
                int containerHeight = tintedPixmap.height + 3 * 2;
                containerPixmap = new Pixmap(containerWidth, containerHeight);
                containerPixmap.draw(tintedPixmap, 3, 3, true);

                outlinedPixmap = Pixmaps.outline(new PixmapRegion(containerPixmap), Pal.gray, 3);

                GenRegion finalRegion = new GenRegion(regionName, outlinedPixmap);
                finalRegion.relativePath = originalRelativePath;
                finalRegion.save(true);
            }catch(Exception e){
                Log.err("Failed to modify UI icon for status effect: " + effect.name, e);
                if(outlinedPixmap != null && !outlinedPixmap.isDisposed()){
                    GenRegion potentiallyUpdatedRegion = atlas.find(regionName);
                    if(!potentiallyUpdatedRegion.found() || potentiallyUpdatedRegion.pixmap() != outlinedPixmap){
                        outlinedPixmap.dispose();
                    }
                }
            }finally{
                if(baseCopy != null && !baseCopy.isDisposed()) baseCopy.dispose();
                if(tintedPixmap != null && !tintedPixmap.isDisposed()) tintedPixmap.dispose();
                if(containerPixmap != null && !containerPixmap.isDisposed()) containerPixmap.dispose();
            }
        }));
    }
}