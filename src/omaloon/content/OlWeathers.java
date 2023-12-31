package omaloon.content;

import arc.util.*;
import mindustry.content.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.meta.*;
import omaloon.entities.bullet.*;
import omaloon.type.weather.*;

public class OlWeathers {
    public static Weather hailStone;

    public static void load(){
        hailStone = new HailStormWeather("hailStone"){{
            attrs.set(Attribute.light, -2f);

            drawParticles = drawNoise = false;
            duration = 15f * Time.toMinutes;
            inBounceCam = false;
            bulletChange = 0.5f;
            sound = Sounds.rain;

            setBullets(
                    new HailStoneBulletType("omaloon-hailstone-big", 1){{
                        hitEffect = Fx.explosion.layer(Layer.power);
                        despawnEffect = Fx.none;
                        splashDamage = 45f;
                        splashDamageRadius = 16;
                    }}, 1/700f,

                    new HailStoneBulletType("omaloon-hailstone-middle", 2){{
                        hitEffect = Fx.dynamicWave.layer(Layer.power);
                        despawnEffect = OIFx.falledStone;
                        splashDamage = 10f;
                        splashDamageRadius = 8;
                    }}, 1/12f,

                    new HailStoneBulletType("omaloon-hailstone-small", 5){{
                        hitEffect = Fx.none;
                        despawnEffect = OIFx.falledStone;
                        splashDamage = 50f;
                        splashDamageRadius = 32;
                    }}, 1f
            );
        }};
    }
}
