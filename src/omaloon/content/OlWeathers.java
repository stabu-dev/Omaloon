package omaloon.content;

import arc.graphics.Color;
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
        hailStone = new HailStormWeather("hail-storm"){{
            attrs.set(Attribute.light, -2f);

            drawParticles = inBounceCam = drawNoise = false;
            duration = 15f * Time.toMinutes;
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
                        despawnEffect = OIFx.staticStone;
                        splashDamage = 95f;
                        splashDamageRadius = 12f;

                        canCollideFalling = pierce = true;
                        fallingDamage = 120f;
                        fallingRadius = 30f;
                        minDistanceFallingCollide = 15f;
                        hitFallingEffect = OIFx.bigExplosionStone;
                        hitFallingColor = Color.valueOf("5e9098");
                    }}, 1/1600f,

                    new HailStoneBulletType("omaloon-hailstone-middle", 2){{
                        hitEffect = Fx.dynamicWave.layer(Layer.power);
                        despawnEffect = OIFx.fellStone;
                        splashDamage = 10f;
                        splashDamageRadius = 8f;

                        canCollideFalling = true;
                        fallingDamage = 25f;
                        fallingRadius = 15f;
                        minDistanceFallingCollide = 5f;
                        hitFallingEffect = OIFx.explosionStone;
                        hitFallingColor = Color.valueOf("5e9098");
                    }}, 1/12f,

                    new HailStoneBulletType("omaloon-hailstone-small", 5){{
                        hitEffect = Fx.none;
                        despawnEffect = OIFx.fellStone;
                        splashDamage = 0f;
                        splashDamageRadius = 0;
                    }}, 1f
            );
        }};
    }
}
