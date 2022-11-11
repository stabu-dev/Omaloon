package ol.world.blocks.defense;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import mindustry.gen.Building;
import mindustry.world.Tile;
import mindustry.world.meta.Stat;
import ol.graphics.OlGraphics;

import static mindustry.Vars.world;

public class OlJoinWall extends OlWall {
    public boolean damageLink = false;
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
        for(int i = 0; i<3; i++) {
            for(int j = 0; j<3; j++) {
                grid[i][j] = world.tile(i + tile.x - 1, j + tile.y - 1);

                if(grid[i][j] != null) {
                    avail++;
                }
            }
        }

        int index = OlGraphics.getTilingIndex(grid, 1, 1, t -> t != null && t.block() == OlJoinWall.this);
        Draw.rect(avail == 0 ? region : joins[index], tile.worldx(), tile.worldy());
    }

    public class OlJoinWallBuild extends OlWall.olWallBuild {
        @Override
        public void updateTile() {
            super.updateTile();

            if(damageLink) {
                int i = 1;
                float thp = health;

                for(Building b : proximity) {
                    if(b instanceof OlJoinWallBuild w) {
                        thp += w.health;
                        i++;
                    }
                }

                float hp = thp/i;
                for(Building b : proximity) {
                    if(b instanceof OlJoinWallBuild w) {
                        w.health = hp;
                    }
                }
            }
        }
    }
}