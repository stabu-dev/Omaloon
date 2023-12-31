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
            sound = OlSounds.hailRain;

            setBullets(
                    /*new HailStoneBulletType("omaloon-hailstone-giant", 1){{
                        hitEffect = Fx.explosion.layer(Layer.power);
                        hitSound = OlSounds.giantHailstoneHit;
                        hitSoundVolume = 6;
                        despawnEffect = Fx.none;
                        splashDamage = 4000f;
                        splashDamageRadius = 116;
                        fallTime = 200f;
                        hitShake = 40f;
                    }}, 1/1600f,*/

                    new HailStoneBulletType("omaloon-hailstone-big", 1){{
                        hitEffect = Fx.explosion.layer(Layer.power);
                        hitSound = OlSounds.bigHailstoneHit;
                        despawnEffect = Fx.none;
                        splashDamage = 95f;
                        splashDamageRadius = 16;
                    }}, 1/1600f,

                    new HailStoneBulletType("omaloon-hailstone-middle", 2){{
                        hitEffect = Fx.dynamicWave.layer(Layer.power);
                        despawnEffect = OIFx.fallenStone;
                        splashDamage = 10f;
                        splashDamageRadius = 8;
                    }}, 1/12f,

                    new HailStoneBulletType("omaloon-hailstone-small", 5){{
                        hitEffect = Fx.none;
                        despawnEffect = OIFx.fallenStone;
                        splashDamage = 50f;
                        splashDamageRadius = 32;
                    }}, 1f
            );
        }};
    }
}
