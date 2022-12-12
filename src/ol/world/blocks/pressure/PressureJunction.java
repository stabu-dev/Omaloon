package ol.world.blocks.pressure;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.struct.Seq;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.graphics.Pal;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.defense.Wall;

import static mindustry.Vars.*;

public class PressureJunction extends Wall implements PressureReplaceable {
    public boolean canExplode = true;
    public Effect boomEffect = Fx.none;
    public boolean noNetDestroy = true;

    public PressureJunction(String name) {
        super(name);

        flashHit = false;
        flashColor = Color.white;

        replaceable = true;
        solid = true;
    }

    @Override
    public boolean canReplace(Block other) {
        return other instanceof PressurePipe;
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        super.drawPlace(x, y, rotation, valid);
        TextureRegion pressureIcon = Core.atlas.find("ol-pressure-icon");

        float dx = x * 8;
        float dy = y * 8;
        float ds = size * 8;

        Draw.rect(pressureIcon, dx, dy + ds, -90);
        Draw.rect(pressureIcon, dx, dy - ds, 90);

        Draw.color(Pal.lightishGray);
        Draw.rect(pressureIcon, dx + ds, dy, 180);
        Draw.rect(pressureIcon, dx - ds, dy, 0);

        Draw.reset();
    }

    @Override
    public boolean canPlaceOn(Tile tile, Team team, int rotation) {
        if(!noNetDestroy) {
            return super.canPlaceOn(tile, team, rotation);
        }

        int tx = (int) (tile.drawx() / 8);
        int ty = (int) (tile.drawy() / 8);

        Building left = world.tile(tx - 1, ty).build;
        Building right = world.tile(tx + 1, ty).build;
        Building bottom = world.tile(tx, ty - 1).build;
        Building top = world.tile(tx, ty + 1).build;

        if(rotation == 0 || rotation == 2) {
            return left instanceof PressureAble || right instanceof PressureAble;
        }

        if(rotation == 1 || rotation == 3) {
            return top instanceof PressureAble || bottom instanceof PressureAble;
        }

        return false;
    }

    public class PressureJunctionBuild extends WallBuild{

        public Building getInvert(Building other) {
            Building left = world.tile(tileX() - 1, tileY()).build;
            Building right = world.tile(tileX() + 1, tileY()).build;

            if(left == other) {
                return right;
            }

            if(right == other) {
                return left;
            }

            Building bottom = world.tile(tileX(), tileY() - 1).build;
            Building top = world.tile(tileX(), tileY() + 1).build;

            if(top == other) {
                return bottom;
            }

            if(bottom == other) {
                return top;
            }

            return null;
        }

        public void netKill() {
            if(!canExplode) {
                return;
            }

            if(boomEffect != null) {
                boomEffect.at(x, y);
            }

            kill();
        }

        public boolean notValid(Building b) {
            return !(b instanceof PressureAble);
        }

        @Override
        public void updateTile() {
            super.updateTile();

            Building left   = world.tile(tileX() - 1, tileY()).build;
            Building right  = world.tile(tileX() + 1, tileY()).build;
            Building bottom = world.tile(tileX(), tileY() - 1).build;
            Building top    = world.tile(tileX(), tileY() + 1).build;

            if(noNetDestroy && notValid(left) && notValid(right) && notValid(bottom) && notValid(top)) {
                kill();
            }
        }
    }
}