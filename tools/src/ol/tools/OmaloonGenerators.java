package ol.tools;

import arc.*;
import arc.files.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.util.*;
import mindustry.game.*;
import mindustry.world.*;
import mindustry.world.blocks.*;
import mindustry.world.blocks.environment.*;
import mindustry.world.blocks.legacy.*;
import mindustry.world.meta.*;
import mma.tools.*;
import mma.tools.gen.*;
import mma.tools.gen.MindustryImagePacker.*;
import mma.type.*;

import static mindustry.Vars.*;
import static mma.tools.ModImagePacker.processor;
import static mma.tools.gen.MindustryImagePacker.*;

public class OmaloonGenerators extends ModGenerators{
    @Override
    protected void run(){
        super.run();
    }

    @Override
    protected void oreIcons() {
        if (!generateOreIcons)
            return;
        content.blocks().<OreBlock>each(b -> b instanceof OreBlock, ore -> {
            int shadowColor = Color.rgba8888(0, 0, 0, 0.3f);
            for (int i = 0; i < ore.variants; i++) {
                // get base image to draw on
                if (!ore.variantRegions[i].found()){
                    Log.err("HAS NO REGION "+ore.name);
                    return;
                }
                Pixmap base = get(ore.variantRegions[i]);
                Pixmap image = base.copy();
                int offset = image.width / tilesize - 1;
                for (int x = 0; x < image.width; x++) {
                    for (int y = offset; y < image.height; y++) {
                        // draw semi transparent background
                        if (base.getA(x, y - offset) != 0) {
                            image.setRaw(x, y, Pixmap.blend(shadowColor, base.getRaw(x, y)));
                        }
                    }
                }
                image.draw(base, true);
                replace(ore.variantRegions[i], image);
                save(image, "../blocks/environment/" + ore.name + (i + 1));
                save(image, "../editor/editor-" + ore.name + (i + 1));
                save(image, "" + ore.name + "-full");
                save(image, "../ui/block-" + ore.name + "-ui");
            }
        });
    }
}
