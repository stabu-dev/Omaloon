package omaloon.tools.proc;

import arc.graphics.*;
import arc.graphics.g2d.*;

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
    }
}