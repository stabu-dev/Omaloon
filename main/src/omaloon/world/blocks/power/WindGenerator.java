package omaloon.world.blocks.power;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.*;
import arc.math.geom.Vec3;
import arc.struct.*;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.*;
import mindustry.gen.*;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.world.blocks.power.*;
import mindustry.world.meta.*;

import static arc.Core.graphics;
import static arc.Core.input;

public class WindGenerator extends PowerGenerator {
    public float boostWeather = 0.25f;
    public TextureRegion rotatorRegion;
    public float rotateSpeed = 1.0f, speedBoost = 3.0f;

    public WindGenerator(String name){
        super(name);
        flags = EnumSet.of();
        envEnabled = Env.any;
    }

    @Override
    public void load(){
        super.load();
        rotatorRegion = Core.atlas.find(name + "-rotator");
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.remove(generationType);
        stats.add(generationType, powerProduction * 60.0f, StatUnit.powerSecond);
    }


    public class WindGeneratorBuild extends GeneratorBuild {
        public float boost = 0.0f;
        @Override
        public void updateTile(){
            if(enabled){
                boost = Mathf.lerpDelta(boost, !Groups.weather.isEmpty() ? 1.1f : 0.0f, 0.05f);
                productionEfficiency = 1 + (boostWeather * boost);
            }
        }

        @Override
        public void draw(){
            Draw.rect(region, x, y);
            Draw.flush();
            Tmp.m1.set(Draw.proj());
            float rotation = 45;
            Draw.proj().mul(Tmp.m2.setToRotation(Vec3.Y, rotation));
            float alpha = (Time.time % 90) / 90f;
            float rotationOffset = 90 + 90 + 45;
            float pixelSize = 1;

            int width=8;
            for (int i = width / 2; i >= -width / 2; i--) {
                Draw.alpha(1f);
                Draw.rect(rotatorRegion, x - i * pixelSize, y, Time.time % 90 + rotationOffset);
                Draw.alpha(alpha);
                Draw.rect(rotatorRegion, x - i * pixelSize, y, Time.time % 90 - 90 + rotationOffset);
            }
            Draw.proj(Tmp.m1);
            Draw.flush();
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.f(boost);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            boost = read.f();
        }
    }
}