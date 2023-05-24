package ol.atlas;

import arc.Core;
import arc.func.Boolf;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import mindustry.gen.Building;
import mindustry.graphics.Layer;
import mindustry.world.Block;
import mindustry.world.Tile;
import net.tmmc.util.Geom;
import net.tmmc.util.XBlocks;
import net.tmmc.util.XWorld;

import java.util.HashMap;
import java.util.Map;

/**
 * draws exactly like OlJointWall
 */
public class DrawAtlas implements ILayer {
    public Map<String, TextureRegion> cache;
    public Hand boolf;
    public String prefix = "atlas";

    public interface Hand {
        boolean get(Tile a, Building b);
    }

    @Override
    public void draw(Block block, Building build) {
        StringBuilder builder = new StringBuilder();
        Geom.each4dAngle(point -> {
            if(boolf.get(XWorld.at(point), build)) {
                builder.append('t');
            } else {
                builder.append('f');
            }
        }, Geom.toPoint(build));

        TextureRegion out;
        String id = builder.toString();
        if(cache.containsKey(id)) {
            out = cache.get(id);
        } else {
            out = Core.atlas.find(block.name + '-' + prefix + '-' + id);
            cache.put(id, out);
        }

        Draw.z(Layer.block + 2);
        Draw.rect(out, build.x, build.y, 0);
    }

    @Override
    public void load(Block block) {
        if(block.size > 1) {
            throw new IllegalStateException("DrawAtlas only for 1x1 blocks");
        }

        cache = new HashMap<>();
    }
}