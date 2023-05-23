package ol.atlas;

import arc.Core;
import arc.func.Boolf;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import mindustry.gen.Building;
import mindustry.world.Block;
import mindustry.world.Tile;
import net.tmmc.util.Geom;
import net.tmmc.util.XBlocks;
import net.tmmc.util.XWorld;

import java.util.HashMap;
import java.util.Map;

/**
 * малює точно як OlJointWall
 */
public class DrawAtlas implements ILayer {
    public Map<String, TextureRegion> cache;
    public Boolf<Tile> boolf;
    public String prefix = "atlas";

    @Override
    public void draw(Block block, Building build) {
        StringBuilder builder = new StringBuilder();
        Geom.each4dAngle(point -> {
            if(boolf.get(XWorld.at(point))) {
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