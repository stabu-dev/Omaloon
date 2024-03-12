package omaloon.world.blocks.power;

import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.entities.units.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.input.*;
import mindustry.world.*;
import mindustry.world.blocks.power.*;
import mindustry.world.meta.*;
import omaloon.graphics.*;

import static mindustry.Vars.*;

public class WindGenerator extends PowerGenerator{
    public int spacing = 9;
    public float boostWeather = 0.25f;
    public float baseRotateSpeed = 4f;
    public float rotChangeTime = Mathf.random(80.0f, 300.0f);

    public WindGenerator(String name){
        super(name);
        flags = EnumSet.of();
        envEnabled = Env.any;
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.remove(generationType);
        stats.add(generationType, powerProduction * 60.0f, StatUnit.powerSecond);
    }

    @Override
    public void drawOverlay(float x, float y, int rotation){
        if(spacing < 1) return;
        float size = (spacing * 2 + this.size / 2f) * tilesize;
        x -= size / 2f;
        y -= size / 2f;
        Drawm.dashPoly(size / 8f, Pal.accent,
            x, y,
            x + size, y,
            x + size, y + size,
            x, y + size);
    }

    @Override
    public boolean canPlaceOn(Tile tile, Team team, int rotation){
        if(spacing < 1) return true;
        int off = 1 - size % 2;
        for(int x = tile.x - spacing + off; x <= tile.x + spacing; x++){
            for(int y = tile.y - spacing + off; y <= tile.y + spacing; y++){
                Tile t = world.tile(x, y);
                if(t != null && t.block() instanceof WindGenerator s && (s == this || s.intersectsSpacing(t.build.tile, tile))) return false;
            }
        }
        return true;
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list){
        super.drawPlanRegion(plan, list);
        Draw.mixcol();
        int off = 1 - size % 2;
        Tile tile = plan.tile();
        if(spacing < 1 || tile == null) return;
        for(int x = tile.x - spacing + off; x <= tile.x + spacing; x++){
            for(int y = tile.y - spacing + off; y <= tile.y + spacing; y++){
                Tile t = world.tile(x, y);
                if(t != null && t.block() instanceof WindGenerator s && (s == this || s.intersectsSpacing(t.build.tile, tile))){
                    Drawf.selected(t.build, Pal.remove);
                }
            }
        }
    }

    public boolean intersectsSpacing(int sx, int sy, int ox, int oy, int ext){ //TODO untested with larger than 1x1
        if(spacing < 1) return true;
        int spacingOffset = spacing + ext;
        int sizeOffset = 1 - (size & 1);

        return ox >= sx + sizeOffset - spacingOffset && ox <= sx + spacingOffset &&
               oy >= sy + sizeOffset - spacingOffset && oy <= sy + spacingOffset;
    }

    public boolean intersectsSpacing(Tile self, Tile other){
        return intersectsSpacing(self.x, self.y, other.x, other.y, 0);
    }

    @Override
    public void changePlacementPath(Seq<Point2> points, int rotation){
        if(spacing >= 1) Placement.calculateNodes(points, this, rotation, (point, other) -> intersectsSpacing(point.x, point.y, other.x, other.y, 1));
    }

    public class WindGeneratorBuild extends GeneratorBuild{
        public float boost;
        public float targetRotation, nextChangeTime, startTime;
        public float lastRotation = rotation;

        @Override
        public void updateTile(){
            if(enabled){
                boost = Mathf.lerpDelta(boost, !Groups.weather.isEmpty() ? 1.1f : 0.0f, 0.05f);
                productionEfficiency = 1 + (boostWeather * boost);
            }
        }

        //TODO: alignment with the wind direction of the weather
        public float baseRotation(){
            float currentTime = Time.time / baseRotateSpeed;
            if(currentTime > nextChangeTime){
                lastRotation = targetRotation;
                targetRotation = Mathf.random(360);
                startTime = currentTime;
                nextChangeTime = currentTime + rotChangeTime;
            }

            float progress = (currentTime - startTime) / rotChangeTime;
            progress = Mathf.clamp(progress, 0, 1);

            return Mathf.lerp(lastRotation, targetRotation, progress) + this.id() * 10.0f;
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.f(boost);
            write.f(lastRotation);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            boost = read.f();
            lastRotation = read.f();
        }
    }
}