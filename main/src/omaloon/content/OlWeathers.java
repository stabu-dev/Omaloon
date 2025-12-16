package omaloon.content;

import arc.graphics.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.effect.*;
import mindustry.graphics.*;
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

            addBullets(
            new FallingRockBulletType("omaloon-hailstone-small"){{
                speed = 0.5f;
                lifetime = 20f;
                fallDistance = 120f;

                variants = 5;

                hitEffect = Fx.none;
                despawnEffect = OlFx.fellStone;
            }}, 0f,

            new FallingRockBulletType("omaloon-hailstone-middle"){{
                speed = 0.5f;
                lifetime = 30f;
                fallDistance = 150f;

                variants = 2;

                hitEffect = Fx.dynamicWave.layer(Layer.power).wrap(OlLiquids.glacium.color,5f);
                despawnEffect = OlFx.fellStone;

                damage = splashDamage = 10f;
                splashDamageRadius = 25f;

                /*canCollideFalling = true;
                fallingDamage = 25f;
                fallingRadius = 15f;
                minDistanceFallingCollide = 5f;
                hitFallingEffect = OlFx.explosionStone;
                hitFallingColor = Color.valueOf("5e9098");*/
            }}, 1f - 1f / 12f,

            //TODO: Splash violently when fallen on shallow liquid
            new FallingRockBulletType("omaloon-hailstone-big"){{
                speed = 0.5f;
                lifetime = 20f;
                fallDistance = 120f;

                variants = 2;

                hitSize = 12f;

                hitEffect = new MultiEffect(
                Fx.explosion.layer(Layer.power),
                Fx.dynamicWave.wrap(OlLiquids.glacium.color, 16f)
                );

                despawnEffect = OlFx.staticStone;
                hitSound = OlSounds.bigHailstoneHit;

                damage = splashDamage = 95f;
                splashDamageRadius = 40f;


                /*canCollideFalling = pierce = true;
                fallingDamage = 120f;
                fallingRadius = 30f;
                minDistanceFallingCollide = 15f;
                hitFallingEffect = OlFx.bigExplosionStone;
                hitFallingColor = Color.valueOf("5e9098");*/
            }}, 1f - 1f / 1600f

            //TODO: appear only when weather intensity is high
            /*new FallingRockBulletType("omaloon-hailstone-giant"){{
                speed = 1f;
                lifetime = 200f;
                fallDistance = 400f;

                hitSize = 80f;

                spawnSound = OlSounds.giantHailstoneFall;
                hitEffect = new MultiEffect(Fx.massiveExplosion, Fx.dynamicWave.wrap(OlLiquids.glacium.color, 60f));
                despawnEffect = OlFx.staticStone;
                hitSound = OlSounds.giantHailstoneHit;

                damage = splashDamage = 250f;
                splashDamageRadius = 80f;
            }}, 1f - 1f/1000000*/
            );

            //Old School
            /*setBullets(
            new HailStoneBulletType("omaloon-hailstone-giant", 1){{
                hitEffect = Fx.explosion.layer(Layer.power);
                hitSound = OlSounds.giantHailstoneHit;
                hitSoundVolume = 6;
                despawnEffect = Fx.none;
                splashDamage = 4000f;
                splashDamageRadius = 116;
                fallTime = 200f;
                hitShake = 40f;
            }}, 1/1600f,

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
            );*/
        }};
    }
}
