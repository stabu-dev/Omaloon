package omaloon.world.blocks.power;

import arc.func.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.game.*;
import mindustry.graphics.*;
import mindustry.input.*;
import mindustry.world.*;
import mindustry.world.blocks.power.*;
import mindustry.world.consumers.*;
import mindustry.world.meta.*;
import omaloon.world.meta.*;

public class AreaGenerator extends ConsumeGenerator{
    public int distance = 5;

    public AreaGenerator(String name) {
        super(name);
    }

    @Override
    public boolean canPlaceOn(Tile tile, Team team, int rotation){
        return distance == 0 || !checkNearby(tile.x, tile.y, ntile -> ntile != null && ntile.block() == this);
    }

    // TODO afaik only works on odd sized blocks. Test if it works on even sized blocks, fix if it doesn't.
    @Override
    public void changePlacementPath(Seq<Point2> points, int rotation){
        Placement.calculateNodes(points, this, rotation, (point, other) -> Math.max(Math.abs(point.x - other.x), Math.abs(point.y - other.y)) <= distance + Mathf.ceil(size / 2f) + 1);
    }

    public void checkNearby(int x, int y, Intc2 pos){
        for(int i = -distance - Mathf.ceil(size / 2f) + 1; i < distance + Mathf.ceil(size / 2f) + ((size + 1) % 2); i++) {
            for(int j = -distance - Mathf.ceil(size / 2f) + 1; j < distance + Mathf.ceil(size / 2f) + ((size + 1) % 2); j++) {
                int dx = (x + i);
                int dy = (y + j);

                pos.get(dx, dy);
            }
        }
    }
    public boolean checkNearby(int x, int y, @Nullable Boolf<Tile> pos){
        for(int i = -distance - Mathf.ceil(size / 2f) + 1; i < distance + Mathf.ceil(size / 2f) + ((size + 1) % 2); i++) {
            for(int j = -distance - Mathf.ceil(size / 2f) + 1; j < distance + Mathf.ceil(size / 2f) + ((size + 1) % 2); j++) {
                int dx = (x + i);
                int dy = (y + j);

                if (pos.get(Vars.world.tile(dx, dy))) return true;
            }
        }
        return false;
    }

    @Override
    public void drawOverlay(float x, float y, int rotation){
        if (distance > 0) Drawf.dashSquare(Pal.accent, x, y, (distance + size / 2f) * Vars.tilesize * 2f);
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid){
        super.drawPlace(x, y, rotation, valid);
        if (!valid && distance > 0) checkNearby(x, y, (dx, dy) -> {
            if (Vars.world.tile(dx, dy) != null && Vars.world.tile(dx, dy).block() == this) {
                Draw.color(Pal.remove);
                Fill.square(dx * Vars.tilesize, dy * Vars.tilesize, Vars.tilesize / 4f);
                Draw.color();
            }
        });
    }

    @Override
    public void setStats(){
        super.setStats();
        stats.add(OlStats.space, StatValues.squared(distance, StatUnit.blocks));
    }

    public class AreaGeneratorBuild extends ConsumeGeneratorBuild{
        @Override
        public void updateEfficiencyMultiplier(){
            super.updateEfficiencyMultiplier();
            if (filterItem == null || filterLiquid == null) efficiencyMultiplier = 1f;
            for(Consume cons : block.optionalConsumers) {
                // adds a fixed amount if only optional, scales if booster
                if (cons.booster) {
                    efficiencyMultiplier *= cons.efficiencyMultiplier(this);
                } else {
                    efficiencyMultiplier += cons.efficiencyMultiplier(this);
                }
            }
        }
    }
}
