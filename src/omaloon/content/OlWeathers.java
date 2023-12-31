package omaloon.content;

import arc.util.Time;
import mindustry.content.Fx;
import mindustry.type.Weather;
import mindustry.world.meta.Attribute;
import omaloon.entities.bullet.HailStoneBulletType;
import omaloon.type.weather.BulletWeather;

public class OlWeathers {
    public static Weather hailStone;

    public static void load(){
        hailStone = new BulletWeather("hailStone"){{
            attrs.set(Attribute.light, -2f);

            drawParticles = drawNoise = false;
            duration = 15f * Time.toMinutes;
            inBounceCam = false;
            bulletChange = 0.5f;

            setBullets(
                    new HailStoneBulletType("omaloon-hailstone-big", 1){{
                        hitEffect = Fx.explosion;
                        despawnEffect = Fx.none;
                        splashDamage = 45f;
                        splashDamageRadius = 16;
                    }}, 1/20f,

                    new HailStoneBulletType("omaloon-hailstone-middle", 2){{
                        hitEffect = Fx.explosion;
                        despawnEffect = OIFx.falledStone;
                        splashDamage = 10f;
                        splashDamageRadius = 8;
                    }}, 1/12f,

                    new HailStoneBulletType("omaloon-hailstone-small", 5){{
                        hitEffect = Fx.none;
                        despawnEffect = OIFx.falledStone;
                        splashDamage = 50f;
                        splashDamageRadius = 32;
                    }}, 1/4f

            );

        }};
    }
}
