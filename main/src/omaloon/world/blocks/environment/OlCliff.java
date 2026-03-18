package omaloon.world.blocks.environment;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.game.*;
import mindustry.graphics.*;
import mindustry.world.*;
import omaloon.core.*;

import static arc.Core.*;

public class OlCliff extends Block{
    static {
        Events.run(EventType.Trigger.update, () -> {
            if (input.keyTap(OlBinding.flushCliffs)) OlCliff.flushCliffs();
        });
    }
    public float colorMultiplier = 1.5f;
    public boolean useMapColor = true;
    public TextureRegion[] cliffs;

    public OlCliff(String name){
        super(name);
        breakable = alwaysReplace = false;
        solid = true;
        saveData = true;
        cacheLayer = CacheLayer.walls;
        fillsTile = false;
        hasShadow = false;
    }

    public static void flushCliffs(){
        Vars.world.tiles.eachTile(tile -> {
            if(tile.block() instanceof OlCliff && tile.data == 0){
                for(int i = 0; i < 4; i++){
                    if(tile.nearby(i).block() instanceof CliffHelper) tile.data = (byte)(i + 1);
                }
                if(tile.data == 0) for(int i = 0; i < 4; i++){
                    if(tile.nearby(Geometry.d8edge(i)).block() instanceof CliffHelper) tile.data = (byte)(i + 5);
                }
                for(int i = 0; i < 4; i++){
                    if(
                    tile.nearby(i).block() instanceof CliffHelper &&
                    tile.nearby((i + 1) % 4).block() instanceof CliffHelper
                    ) tile.data = (byte)(i + 9);
                }
                if(tile.data == 0) tile.setBlock(Blocks.air);
            }
        });
        Vars.world.tiles.eachTile(tile -> {
            if(tile.block() instanceof CliffHelper) mindustry.gen.Call.setTile(tile, Blocks.air, Team.derelict, 0);
        });
    }

    @Override
    public void drawBase(Tile tile){
        if(tile.data == 0){
            Draw.color();
            Draw.rect(region, tile.drawx(), tile.drawy());
        }else{
            if(useMapColor) Draw.color(Tmp.c1.set(tile.floor().mapColor).mul(colorMultiplier));
            Draw.rect(cliffs[tile.data - 1], tile.drawx(), tile.drawy());
        }
        Draw.color();
    }

    @Override
    public void load(){
        super.load();
        cliffs = new TextureRegion[12];
        for(int i = 0; i < 12; i++){
            cliffs[i] = Core.atlas.find(name + "-" + (i + 1), "omaloon-cliff-" + (i + 1));
        }
    }

    @Override
    public int minimapColor(Tile tile){
        return Tmp.c1.set(tile.floor().mapColor).mul(1.2f).rgba();
    }
}