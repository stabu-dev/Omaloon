package omaloon.tools.proc;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.util.*;

import mindustry.graphics.*;

import omaloon.*;
import omaloon.tools.GenAtlas.*;
import omaloon.tools.*;

import java.util.concurrent.*;

import static mindustry.Vars.*;
import static omaloon.tools.Tools.*;

/**
 * Mirrors the logic from mindustry.tools.Generators#generate("item-icons") for items, liquids and status effects.
 * @author stabu_
 */
public class ItemProcessor implements Processor{
    @Override
    public void process(ExecutorService exec){
        // Items
        content.items().each(OmaloonMod::isOmaloon, item -> submit(exec, item.name + "-ui", () -> {
            GenRegion baseRegion = atlas.find(item.name);
            if(!baseRegion.found()) return;

            String uiIconName = item.name + "-ui";
            if(atlas.has(uiIconName)) return;

            GenRegion uiRegion = new GenRegion(uiIconName, baseRegion.pixmap().copy());
            uiRegion.relativePath = "ui";
            uiRegion.save(true);
        }));

        // Liquids
        content.liquids().each(OmaloonMod::isOmaloon, liquid -> submit(exec, liquid.name + "-ui", () -> {
            GenRegion baseRegion = atlas.find(liquid.name);
            if(!baseRegion.found()) return;

            String uiIconName = liquid.name + "-ui";
            if(atlas.has(uiIconName)) return;

            GenRegion uiRegion = new GenRegion(uiIconName, baseRegion.pixmap().copy());
            uiRegion.relativePath = "ui";
            uiRegion.save(true);
        }));

        // Status Effects
        content.statusEffects().each(OmaloonMod::isOmaloon, effect -> submit(exec, effect.name + "-ui", () -> {
            GenRegion baseRegion = atlas.find(effect.name);
            if(!baseRegion.found()){
                Log.warn("Base region not found for status effect '@'. Skipping.", effect.name);
                return;
            }

            Pixmap tinted = baseRegion.pixmap().copy();
            tinted.each((x, y) -> tinted.setRaw(x, y, Color.muli(tinted.getRaw(x, y), effect.color.rgba())));

            Pixmap container = new Pixmap(tinted.width + 6, tinted.height + 6);
            container.draw(tinted, 3, 3, true);

            Pixmap finalImage = Pixmaps.outline(new PixmapRegion(container), Pal.gray, 3);

            // Overwrite original region with tinted and outlined version
            GenRegion mainRegion = new GenRegion(baseRegion.name, finalImage);
            mainRegion.relativePath = baseRegion.relativePath;
            mainRegion.save(false);

            String uiIconName = effect.name + "-ui";
            if(!atlas.has(uiIconName)){
                GenRegion uiRegion = new GenRegion(uiIconName, finalImage.copy());
                uiRegion.relativePath = "ui";
                uiRegion.save(true);
            }

            tinted.dispose();
            container.dispose();
            finalImage.dispose();
        }));
    }
}