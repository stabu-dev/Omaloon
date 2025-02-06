package omaloon.content;

import arc.graphics.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.meta.*;
import omaloon.entities.bullet.*;
import omaloon.type.weather.*;

public class OlWeathers{
    public static Weather hailStone, aghaniteStorm, wind;

    public static void load(){
        hailStone = new HailStormWeather("hail-storm"){{
            attrs.set(Attribute.light, -2f);

            drawParticles = inBounceCam = drawNoise = false;
            duration = 15f * Time.toMinutes;
            bulletChange = 0.5f;
            soundVol = 0.05f;

            sound = OlSounds.hailRain;

            setBullets(
            //TODO (Maybe this should be added in to the other weather?), Random: Meteor Rain Maybe
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

            new HailStoneBulletType("omaloon-hailstone-big", 3){{
                hitEffect = Fx.explosion.layer(Layer.power);
                hitSound = OlSounds.bigHailstoneHit;
                hitSoundVolume = 0.2f;
                despawnEffect = OlFx.staticStone;
                damage = splashDamage = 95f;
                splashDamageRadius = 40f;

                canCollideFalling = pierce = true;
                fallingDamage = 120f;
                fallingRadius = 30f;
                minDistanceFallingCollide = 15f;
                hitFallingEffect = OlFx.bigExplosionStone;
                hitFallingColor = Color.valueOf("5e9098");
            }}, 1 / 1600f,

            new HailStoneBulletType("omaloon-hailstone-middle", 2){{
                hitEffect = Fx.dynamicWave.layer(Layer.power);
                despawnEffect = OlFx.fellStone;
                damage = splashDamage = 10f;
                splashDamageRadius = 25f;

                canCollideFalling = true;
                fallingDamage = 25f;
                fallingRadius = 15f;
                minDistanceFallingCollide = 5f;
                hitFallingEffect = OlFx.explosionStone;
                hitFallingColor = Color.valueOf("5e9098");
            }}, 1 / 12f,

            new HailStoneBulletType("omaloon-hailstone-small", 5){{
                hitEffect = Fx.none;
                despawnEffect = OlFx.fellStone;
                splashDamage = 0f;
                splashDamageRadius = 0;
            }}, 1f
            );
        }};

        aghaniteStorm = new HailStormWeather("aghanite-storm"){{
            attrs.set(Attribute.light, -2f);

            particleRegion = "omaloon-aghanite-storm-small-3";
            noisePath = "distortAlpha";

            inBounceCam = drawRain = false;
            useWindVector = drawNoise = true;
            noiseColor = Color.valueOf("968B79");
            baseSpeed = 5.4f;
            duration = 15f * Time.toMinutes;
            bulletChange = 0.5f;
            soundVol = 0.05f;

            sound = Sounds.wind;

            setBullets(
            //TODO (Maybe this should be added in to the other weather?), Random: Meteor Rain Maybe
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

            new HailStoneBulletType("omaloon-aghanite-storm-big", 3){{
                speed = 4f;

                hitEffect = Fx.explosion.layer(Layer.power);
                hitSound = OlSounds.bigHailstoneHit;
                hitSoundVolume = 0.2f;
                despawnEffect = OlFx.staticStone;
                damage = splashDamage = 95f;
                splashDamageRadius = 40f;

                canCollideFalling = pierce = true;
                immovable = false;
                fallingDamage = 120f;
                fallingRadius = 30f;
                minDistanceFallingCollide = 15f;
                hitFallingEffect = OlFx.bigExplosionStone;
                hitFallingColor = Color.valueOf("5e9098");
            }}, 1 / 1600f,

            new HailStoneBulletType("omaloon-aghanite-storm-middle", 2){{
                speed = 4f;

                hitEffect = Fx.none;
                despawnEffect = OlFx.fellStoneAghanite;
                damage = splashDamage = 10f;
                splashDamageRadius = 25f;

                canCollideFalling = true;
                immovable = false;
                fallingDamage = 25f;
                fallingRadius = 15f;
                minDistanceFallingCollide = 5f;
                hitFallingEffect = OlFx.explosionStone;
                hitFallingColor = Color.valueOf("5e9098");
            }}, 1 / 12f,

            new HailStoneBulletType("omaloon-aghanite-storm-small", 5){{
                speed = 4f;

                immovable = false;
                hitEffect = Fx.none;
                despawnEffect = OlFx.fellStoneAghanite;
                splashDamage = 0f;
                splashDamageRadius = 0;
            }}, 1f
            );
        }};

        wind = new EffectWeather("wind"){{
            weatherFx = OlFx.windTail;
            particleRegion = "particle";
            sizeMax = 5f;
            sizeMin = 1f;
            density = 1600;
            baseSpeed = 5.4f;
            minAlpha = 0.05f;
            maxAlpha = 0.18f;
            force = 0.1f;

            sound = Sounds.wind2;
            soundVol = 0.8f;

            status = OlStatusEffects.breeze;
            statusDuration = 30f;
            statusGround = false;

            maxSpawn = 2;

            duration = 8f * Time.toMinutes;
        }};
    }
}
