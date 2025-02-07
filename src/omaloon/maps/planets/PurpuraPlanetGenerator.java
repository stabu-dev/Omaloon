package omaloon.maps.planets;

import arc.graphics.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import arc.util.noise.*;
import mindustry.maps.generators.*;

import static arc.graphics.Color.valueOf;

public class PurpuraPlanetGenerator extends PlanetGenerator{
    public double octaves = 5, persistence = 0.3, scl = 3.0, pow = 1.2, mag = 1;
    public float rotationScl = 360;

    public Color[] colors = new Color[]{
    valueOf("242424"),
    valueOf("413B42"),
    valueOf("7F777E"),
    valueOf("B2B2B2"),
    valueOf("807881"),
    valueOf("4F424D")
    };

    @Override
    public float getHeight(Vec3 position){
        return 0;
    }

    @Override
    public Color getColor(Vec3 position){
        Tmp.v31.set(position).rotate(Vec3.Y, position.y * rotationScl).add(1000f, 0f, 500f);
        double height = Math.pow(Simplex.noise3d(0, octaves, persistence, scl, Tmp.v31.x, Tmp.v31.y, Tmp.v31.z), pow) * mag;
        return Tmp.c1.set(colors[Mathf.clamp((int)(height * colors.length), 0, colors.length - 1)]);
    }
}
