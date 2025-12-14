package omaloon.content;

import arc.util.*;
import mindustry.content.*;
import mindustry.type.*;
import mindustry.world.meta.*;
import omaloon.entities.bullet.*;
import omaloon.gen.*;
import omaloon.type.weather.*;

public class OlWeathers{
    public static Weather hailStorm;

    public static void load(){
        hailStorm = new HailStormWeather("hail-storm"){{
            attrs.set(Attribute.light, -2f);

            duration = 15f * Time.toMinutes;
            soundVol = 0.05f;

            spawns = 6;
            spawnChance = 0.5f;

            sound = OlSounds.hailRain;

            addBullet(
                new FallingRockBulletType("omaloon-hailstone-small"){{
                    speed = 0.5f;
                    lifetime = 20f;
                    fallDistance = 120f;

                    variants = 5;

                    hitEffect = Fx.none;
                    despawnEffect = OlFx.fellStone;
                }}, 0f
            );
            addBullet(
                new FallingRockBulletType("omaloon-hailstone-medium"){{
                    speed = 0.5f;
                    lifetime = 30f;
                    fallDistance = 150f;

                    variants = 2;

                    hitEffect = Fx.dynamicWave;
                    despawnEffect = OlFx.fellStone;

                    damage = splashDamage = 10f;
                    splashDamageRadius = 25f;

//                    canCollideFalling = true;
//                    fallingDamage = 25f;
//                    fallingRadius = 15f;
//                    minDistanceFallingCollide = 5f;
//                    hitFallingEffect = OlFx.explosionStone;
//                    hitFallingColor = Color.valueOf("5e9098");
                }}, 1f - 1f / 12f
            );
            addBullet(
                new FallingRockBulletType("omaloon-hailstone-big"){{
                    speed = 0.5f;
                    lifetime = 20f;
                    fallDistance = 120f;

                    variants = 3;

                    hitEffect = Fx.explosion;
                    despawnEffect = OlFx.staticStone;
                    hitSound = OlSounds.bigHailstoneHit;

                    damage = splashDamage = 95f;
                    splashDamageRadius = 40f;


//                    canCollideFalling = pierce = true;
//                    fallingDamage = 120f;
//                    fallingRadius = 30f;
//                    minDistanceFallingCollide = 15f;
//                    hitFallingEffect = OlFx.bigExplosionStone;
//                    hitFallingColor = Color.valueOf("5e9098");
                }}, 1f - 1f / 1600f
            );

//            setBullets(
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

//            new HailStoneBulletType("omaloon-hailstone-big", 3){{
//                hitEffect = Fx.explosion.layer(Layer.power);
//                hitSound = OlSounds.bigHailstoneHit;
//                hitSoundVolume = 0.2f;
//                despawnEffect = OlFx.staticStone;
//                damage = splashDamage = 95f;
//                splashDamageRadius = 40f;
//
//                canCollideFalling = pierce = true;
//                fallingDamage = 120f;
//                fallingRadius = 30f;
//                minDistanceFallingCollide = 15f;
//                hitFallingEffect = OlFx.bigExplosionStone;
//                hitFallingColor = Color.valueOf("5e9098");
//            }}, 1 / 1600f,
//
//            new HailStoneBulletType("omaloon-hailstone-middle", 2){{
//                hitEffect = Fx.dynamicWave.layer(Layer.power);
//                despawnEffect = OlFx.fellStone;
//                damage = splashDamage = 10f;
//                splashDamageRadius = 25f;
//
//                canCollideFalling = true;
//                fallingDamage = 25f;
//                fallingRadius = 15f;
//                minDistanceFallingCollide = 5f;
//                hitFallingEffect = OlFx.explosionStone;
//                hitFallingColor = Color.valueOf("5e9098");
//            }}, 1 / 12f,
//
//            new HailStoneBulletType("omaloon-hailstone-small", 5){{
//                hitEffect = Fx.none;
//                despawnEffect = OlFx.fellStone;
//                splashDamage = 0f;
//                splashDamageRadius = 0;
//            }}, 1f
//            );
        }};
    }
}
