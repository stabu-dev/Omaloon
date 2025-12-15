package omaloon.world.blocks.power;

import arc.func.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.game.*;
import mindustry.game.Teams.*;
import mindustry.graphics.*;
import mindustry.input.*;
import mindustry.world.*;
import mindustry.world.blocks.ConstructBlock.*;
import mindustry.world.blocks.power.*;
import mindustry.world.consumers.*;
import mindustry.world.meta.*;
import omaloon.world.meta.*;

public class AreaGenerator extends ConsumeGenerator{
    public int range = 5;

    /**
     * Damage multiplier applied if the placement check is worked around (e.g. trough editor).
     */
    public float crowdingDamageScale = 0.01f;

    public Effect crowdingEffect = Fx.fire;

    public AreaGenerator(String name){
        super(name);
    }

    @Override
    public boolean canPlaceOn(Tile tile, Team team, int rotation){
        if(range == 0) return true;

        return !checkNearby(tile.x, tile.y, ntile -> {
            if(ntile == null) return false;

            if(ntile.block() == this) return true;

            return ntile.build instanceof ConstructBuild cb && cb.current == this;
        });
    }

    @Override
    public void changePlacementPath(Seq<Point2> points, int rotation){
        Placement.calculateNodes(points, this, rotation, (point, other) -> Math.max(Math.abs(point.x - other.x), Math.abs(point.y - other.y)) <= range + Mathf.ceil(size / 2f) + 1);
    }

    public void checkNearby(int x, int y, Intc2 pos){
        int r = range + Mathf.ceil(size / 2f) + ((size + 1) % 2);
        for(int i = -r + 1; i < r; i++){
            for(int j = -r + 1; j < r; j++){
                pos.get(x + i, y + j);
            }
        }
    }

    public boolean checkNearby(int x, int y, @Nullable Boolf<Tile> pos){
        int r = range + Mathf.ceil(size / 2f) + ((size + 1) % 2);
        for(int i = -r + 1; i < r; i++){
            for(int j = -r + 1; j < r; j++){
                if(pos.get(Vars.world.tile(x + i, y + j))) return true;
            }
        }
        return false;
    }

    @Override
    public void drawOverlay(float x, float y, int rotation){
        if(range > 0) Drawf.dashSquare(Pal.accent, x, y, (range + size / 2f) * Vars.tilesize * 2f);
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid){
        super.drawPlace(x, y, rotation, valid);
        if(!valid && range > 0) checkNearby(x, y, (dx, dy) -> {
            if(Vars.world.tile(dx, dy) != null && Vars.world.tile(dx, dy).block() == this){
                Draw.color(Pal.remove);
                Fill.square(dx * Vars.tilesize, dy * Vars.tilesize, Vars.tilesize / 4f);
                Draw.color();
            }
        });
    }

    @Override
    public void setStats(){
        super.setStats();
        stats.add(OlStats.space, StatValues.squared(range, StatUnit.blocks));
    }

    public class AreaGeneratorBuild extends ConsumeGeneratorBuild{
        public float checkTimer = 0f;
        public float crowdingFactor = 0f;
        public float smoothCrowding = 0f;

        @Override
        public void updateTile(){
            super.updateTile();

            if((checkTimer += Time.delta) >= 20f){
                checkTimer = 0f;
                calculateCrowding();
            }

            smoothCrowding = Mathf.lerpDelta(smoothCrowding, crowdingFactor, 0.05f);

            if(smoothCrowding > 0.001f){
                if(Mathf.chanceDelta(smoothCrowding * 0.15f)){
                    crowdingEffect.at(x + Mathf.range(size * Vars.tilesize / 2f), y + Mathf.range(size * Vars.tilesize / 2f), rotation, Pal.remove);
                }

                damage(maxHealth * smoothCrowding * crowdingDamageScale * Time.delta);

                if(warmup > 0){
                    warmup -= smoothCrowding * 0.05f * Time.delta;
                    if(warmup < 0) warmup = 0;
                }

                updateEfficiencyMultiplier();
            }
        }

        @Override
        public void heal(float amount){
            if(crowdingFactor > 0.01f) return;
            super.heal(amount);
        }

        public void calculateCrowding(){
            int r = range + Mathf.ceil(size / 2f) + ((size + 1) % 2);
            float rangePixel = (range + size / 2f + 1) * Vars.tilesize;
            float maxFactor = 0f;

            for(int i = -r + 1; i < r; i++){
                for(int j = -r + 1; j < r; j++){
                    Tile other = Vars.world.tile(tile.x + i, tile.y + j);

                    if(other != null && other.block() == block && other.build != this && other.build != null){
                        float dist = Math.max(Math.abs(other.build.x - x), Math.abs(other.build.y - y));

                        if(dist < rangePixel){
                            float factor = 1f - (dist / rangePixel);
                            if(factor > maxFactor) maxFactor = factor;
                        }
                    }
                }
            }

            crowdingFactor = maxFactor;
        }

        @Override
        public void updateEfficiencyMultiplier(){
            super.updateEfficiencyMultiplier();

            if(filterItem == null || filterLiquid == null) efficiencyMultiplier = 1f;
            for(Consume cons : block.optionalConsumers){
                if(cons.booster){
                    efficiencyMultiplier *= cons.efficiencyMultiplier(this);
                }else{
                    efficiencyMultiplier += cons.efficiencyMultiplier(this);
                }
            }

            if(smoothCrowding > 0f){
                efficiencyMultiplier *= Math.max(0f, 1f - smoothCrowding);
            }
        }
    }
}