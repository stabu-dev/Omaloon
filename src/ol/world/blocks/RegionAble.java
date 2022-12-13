package ol.world.blocks;

import arc.Core;
import arc.graphics.g2d.TextureRegion;

//NOT PRESSURE PART
public interface RegionAble {
    String name();

    default TextureRegion loadRegion(String prefix) {
        return loadRegion(prefix, Core.atlas.find("ol-air"));
    }

    default TextureRegion loadRegion(String prefix, TextureRegion def) {
        if(!Core.atlas.has(name() + prefix)) {
            return def;
        }

        return Core.atlas.find(name() + prefix);
    }
}