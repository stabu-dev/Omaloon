package omaloon.tools.proc;

import arc.files.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import arc.util.serialization.*;
import mindustry.game.*;
import mindustry.world.blocks.*;
import mindustry.world.blocks.environment.*;
import mindustry.world.blocks.legacy.*;
import omaloon.*;
import omaloon.tools.GenAtlas.*;
import omaloon.tools.*;

import java.util.concurrent.*;

import static mindustry.Vars.*;
import static omaloon.tools.Tools.*;
import static omaloon.tools.Tools.init;

public class BlockProcessor implements Processor{
    public static final Pixmap layout = new Pixmap(Base64Coder.decode(
    "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"));

    private static void generateAutotile(Fi path, String name, Fi outputDir) throws java.io.IOException{
        Pixmap image = new Pixmap(path);

        try{
            int width = image.width, height = image.height;

            if(width % 4 != 0 || height % 4 != 0)
                throw new java.io.IOException("Image dimensions are not divisible by 4: " + width + "x" + height);
            if(width != height)
                throw new java.io.IOException("Image is not square: " + width + "x" + height);

            int cellSize = width / 4;
            IntIntMap colorToPosition = new IntIntMap();

            for(int x = 0; x < 4; x++){
                for(int y = 0; y < 4; y++){
                    colorToPosition.put(layout.get(x * 384 / 12, y * 128 / 4),
                    Point2.pack(x * width / 4, y * height / 4));
                }
            }

            int outWidth = width / 4 * 12;
            Pixmap out = new Pixmap(outWidth, height);

            for(int cx = 0; cx < 12; cx++){
                for(int cy = 0; cy < 4; cy++){
                    for(int rx = 0; rx < cellSize; rx++){
                        for(int ry = 0; ry < cellSize; ry++){
                            int point = colorToPosition.get(layout.get(
                            (cx * cellSize + rx) * 384 / (width * 3),
                            (cy * cellSize + ry) * 128 / height), -1);

                            if(point != -1){
                                int sx = Point2.x(point), sy = Point2.y(point);
                                out.set(cx * cellSize + rx, cy * cellSize + ry, image.get(sx + rx, sy + ry));
                            }
                        }
                    }
                }
            }

            for(int i = 0; i < 47; i++){
                int cx = i % 12, cy = i / 12;
                Pixmap cropped = out.crop(cx * cellSize, cy * cellSize, cellSize, cellSize);
                outputDir.child(name + "-" + i + ".png").writePng(cropped);
                cropped.dispose();
            }

            out.dispose();
        }finally{
            image.dispose();
        }
    }

    @Override
    public void process(ExecutorService exec){
        // Standard Blocks
        content.blocks().each(OmaloonMod::isOmaloon, block -> {
            if(block.isAir() || block instanceof ConstructBlock || block instanceof OreBlock || block instanceof LegacyBlock) return;

            submit(exec, block.name, () -> {
                init(block);
                load(block);

                Pixmap shardTeamTop = null;

                if(block.teamRegion.found()){
                    GenRegion teamRegion = conv(block.teamRegion);
                    if(teamRegion.found()){
                        Pixmap teamr = teamRegion.pixmap();
                        for(Team team : Team.all){
                            if(team.hasPalette){
                                Pixmap out = new Pixmap(teamr.width, teamr.height);
                                teamr.each((x, y) -> {
                                    int color = teamr.getRaw(x, y);
                                    int index = color == 0xffffffff ? 0 : color == 0xdcc6c6ff ? 1 : color == 0x9d7f7fff ? 2 : -1;
                                    out.setRaw(x, y, index == -1 ? teamr.getRaw(x, y) : team.palettei[index]);
                                });

                                GenRegion teamSprite = new GenRegion(block.name + "-team-" + team.name, out);
                                teamSprite.relativePath = teamRegion.relativePath;
                                teamSprite.save(true);

                                if(team == Team.sharded){
                                    shardTeamTop = out;
                                }
                            }
                        }
                    }
                }

                TextureRegion[] regions = block.getGeneratedIcons();
                if(regions.length == 0){
                    if(shardTeamTop != null) shardTeamTop.dispose();
                    return;
                }

                Pixmap last = null;
                if(block.outlineIcon){
                    int regionIndex = block.outlinedIcon >= 0 ? block.outlinedIcon : regions.length - 1;
                    GenRegion region = conv(regions[regionIndex]);
                    if(region.found()){
                        Pixmap base = region.pixmap();
                        last = Pixmaps.outline(new PixmapRegion(base), block.outlineColor, block.outlineRadius);

                        if(block.outlinedIcon >= 0){
                            for(int i = block.outlinedIcon + 1; i < regions.length; i++){
                                last.draw(conv(regions[i]).pixmap(), true);
                            }
                        }
                    }
                }

                Pixmap image = null;
                if(regions[0].found()){
                    image = conv(regions[0]).pixmap().copy();

                    for(int i = 1; i < regions.length; i++){
                        TextureRegion region = regions[i];
                        if(i == regions.length - 1 && last != null){
                            image.draw(last, true);
                        }else{
                            image.draw(conv(region).pixmap(), true);
                        }

                        if(conv(region) == block.teamRegions[Team.sharded.id] && shardTeamTop != null){
                            image.draw(shardTeamTop, true);
                        }
                    }

                    if(regions.length == 1 && last != null){
                        image.draw(last, true);
                    }

                    boolean needsFullIcon = regions.length > 1 || shardTeamTop != null || last != null;

                    if(needsFullIcon){
                        String fullIconName = block.name + "-full";
                        if(!atlas.has(fullIconName)){
                            GenRegion fullRegion = new GenRegion(fullIconName, image.copy());
                            fullRegion.relativePath = conv(regions[0]).relativePath;
                            fullRegion.save(true);
                        }
                    }
                }

                if(image != null){
                    String uiIconName = block.name + "-ui";
                    if(!atlas.has(uiIconName)){
                        GenRegion uiRegion = new GenRegion(uiIconName, image);
                        uiRegion.relativePath = "ui";
                        uiRegion.save(true);
                    }else{
                        image.dispose();
                    }
                }

                if(last != null) last.dispose();
                if(shardTeamTop != null) shardTeamTop.dispose();
            });
        });

        // Ore Blocks
        content.blocks().each(OmaloonMod::isOmaloon, block -> {
            if(!(block instanceof OreBlock ore)) return;

            submit(exec, ore.name + "-ore", () -> {
                init(ore);
                load(ore);

                if(ore.variants == 0) return;

                int shadowColor = Color.rgba8888(0, 0, 0, 0.3f);

                for(int i = 0; i < ore.variants; i++){
                    GenRegion baseRegion = conv(ore.variantRegions[i]);
                    if(!baseRegion.found()) continue;

                    Pixmap base = baseRegion.pixmap();
                    Pixmap image = base.copy();

                    int offset = image.width / tilesize - 1;

                    for(int x = 0; x < image.width; x++){
                        for(int y = offset; y < image.height; y++){
                            if(base.getA(x, y - offset) != 0){
                                image.setRaw(x, y, Pixmap.blend(shadowColor, base.getRaw(x, y)));
                            }
                        }
                    }

                    image.draw(base, true);

                    if(i == 0){
                        String fullIconName = ore.name + "-full";
                        if(!atlas.has(fullIconName)){
                            GenRegion fullRegion = new GenRegion(fullIconName, image.copy());
                            fullRegion.relativePath = baseRegion.relativePath;
                            fullRegion.save(true);
                        }

                        String uiIconName = ore.name + "-ui";
                        if(!atlas.has(uiIconName)){
                            GenRegion uiRegion = new GenRegion(uiIconName, image);
                            uiRegion.relativePath = "ui";
                            uiRegion.save(true);
                        }else{
                            image.dispose();
                        }
                    }else{
                        image.dispose();
                    }
                }
            });
        });

        // Autotiles
        content.blocks().each(OmaloonMod::isOmaloon, b -> {
            boolean isAutotile = b instanceof Floor f && f.autotile;
            if(b instanceof StaticWall w && w.autotile)
                isAutotile = true;

            if(!isAutotile)
                return;

            submit(exec, b.name + "-autotile", () -> {
                String rawName = b.name.startsWith(meta.name + "-") ? b.name.substring(meta.name.length() + 1) : b.name;
                Fi blocksDir = spritesDir.child("blocks").child("environment");
                if(!blocksDir.exists())
                    return;

                Fi basePath = blocksDir.child(rawName + "-autotile.png");

                if(basePath.exists()){
                    try{
                        Pixmap image = new Pixmap(basePath);
                        int cellSize = image.width / 4;
                        Pixmap cropped = image.crop(cellSize, cellSize, cellSize, cellSize);

                        Fi iconPath = blocksDir.child(rawName + ".png");
                        if(!iconPath.exists()){
                            iconPath.writePng(cropped);
                        }

                        String uiIconName = b.name + "-ui";
                        if(!atlas.has(uiIconName)){
                            GenRegion uiRegion = new GenRegion(uiIconName, cropped);
                            uiRegion.relativePath = "ui";
                            uiRegion.save(true);
                        }else{
                            cropped.dispose();
                        }
                        image.dispose();

                        generateAutotile(basePath, rawName, blocksDir);
                    }catch(Exception e){
                        Log.err("Failed to autotile: " + b.name, e);
                    }finally{
                        basePath.delete();
                    }
                }else{
                    Log.err("Autotile block '@' not found: @", b.name, basePath.absolutePath());
                }
            });
        });
    }
}