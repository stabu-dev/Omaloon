package omaloon.math;

import arc.*;
import arc.math.*;
import arc.math.geom.*;
import mindustry.*;

public class Physics{
    public static final float horiToVerti = 1f / 48f;

    /**
     * @return the amount of a fluid that flows between 2 tanks.
     */
    public static float fluidFlow(
    float fromPressure, float fromVolume,
    float toPressure, float toVolume,
    float density, float viscosity,
    float timeScl
    ){
        float flow = toVolume * toPressure;
        flow += fromVolume * fromPressure;
        flow /= (fromVolume + toVolume);
        flow -= fromPressure;
        flow *= -1f;
        flow *= fromVolume;
        flow *= density;
        flow /= Math.max(1, viscosity / timeScl);

        return flow;
    }

    public static Vec2 parallax(Vec2 pos, Vec2 reference, float height){
        return pos.lerp(reference, -height / 48 * Vars.renderer.getDisplayScale());
    }

    public static Vec2 parallax(Vec2 pos, float height){
        return parallax(pos, Core.camera.position, height);
    }

    public static float xOffset(float x, float height){
        return (x - Core.camera.position.x) * hMul(height);
    }

    public static float yOffset(float y, float height){
        return (y - Core.camera.position.y) * hMul(height);
    }

    public static float hMul(float height){
        return height * horiToVerti * Vars.renderer.getDisplayScale();
    }

    public static float layerOffset(float x, float y){
        float max = Math.max(Core.camera.width, Core.camera.height);
        return -Mathf.dst(x, y, Core.camera.position.x, Core.camera.position.y) / max / 1000f;
    }
}