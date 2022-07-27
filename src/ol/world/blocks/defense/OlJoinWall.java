package ol.world.blocks.defense;

import arc.Core;
import arc.audio.Sound;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Point2;
import arc.struct.BoolSeq;
import arc.struct.OrderedSet;
import arc.struct.Seq;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.gen.Bullet;
import mindustry.world.Tile;
import ol.graphics.OlGraphics;

import static arc.input.KeyCode.x;
import static arc.input.KeyCode.y;
import static mindustry.Vars.tilesize;
import static mindustry.Vars.world;
import static mindustry.ctype.ContentType.team;

public class OlJoinWall extends OlWall {
    TextureRegion[] joins;
    public OlJoinWall(String name) {
        super(name);
    }
    @Override
    public void load(){
        super.load();
        joins = OlGraphics.getRegions(Core.atlas.find(name+"-joins"), 12, 4,32);
    }

    @Override
    public void drawBase(Tile tile){
        Tile[][] grid = new Tile[3][3];
        int avail = 0;
        for(int i = 0; i<3; i++){
            for(int j = 0; j<3; j++){
                grid[i][j] = world.tile(i+tile.x-1, j+tile.y-1);
                if(grid[i][j]!=null){
                    avail++;
                }
            }
        }
        int index = OlGraphics.getTilingIndex(grid,1,1,t-> t !=null && t.block() == OlJoinWall.this);
        if(avail==0){
            Draw.rect(region, tile.worldx(), tile.worldy());
        }else{
            Draw.rect(joins[index], tile.worldx(), tile.worldy());
        }
    }
}