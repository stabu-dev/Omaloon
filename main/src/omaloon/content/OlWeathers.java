package omaloon.content;

import arc.graphics.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.effect.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.meta.*;
import omaloon.entities.bullet.*;
import omaloon.gen.*;
import omaloon.type.weather.*;

public class OlWeathers{
    public static Weather hailStorm, aghaniteStorm;

    public static void load(){
        hailStorm = new HailStormWeather("hail-storm"){{
            attrs.set(Attribute.light, -2f);

            rain = true;

            duration = 15f * Time.toMinutes;
            soundVol = 0.05f;

            spawns = 6;
            spawnChance = 0.5f;

            sound = OlSounds.hailRain;

            addBullets(
            new FallingRockBulletType("omaloon-hailstone-small"){{
                speed = 0.5f;
                lifetime = 20f;
                fallHeight = 10f;
                fallDistance = 120f;

                variants = 5;

                hitEffect = Fx.none;
                despawnEffect = OlFx.fellStone;
            }}, 1f,

            new FallingRockBulletType("omaloon-hailstone-medium"){{
                speed = 0.5f;
                lifetime = 30f;
                fallHeight = 11f;
                fallDistance = 120f;

                variants = 2;

                hitEffect = new MultiEffect(
                Fx.dynamicWave.layer(Layer.power).wrap(OlLiquids.glacium.color,5f),
                OlFx.hailStoneSplashSmall
                );
                despawnEffect = OlFx.fellStone;

                damage = splashDamage = 10f;
                splashDamageRadius = 25f;
            }}, 1f / 12f,

            new FallingRockBulletType("omaloon-hailstone-big"){{
                speed = 0.5f;
                lifetime = 20f;
                fallHeight = 15f;
                fallDistance = 150f;

                variants = 2;

                hitSize = 12f;

                hitEffect = OlFx.hailStoneImpact;

                despawnEffect = OlFx.staticStone;
                hitSound = OlSounds.bigHailstoneHit;

                damage = splashDamage = 95f;
                splashDamageRadius = 40f;
            }}, 1f / 1600f,

            new FallingRockBulletType("omaloon-hailstone-giant"){{
                speed = 1f;
                lifetime = 200f;
                fallHeight = 20f;
                fallDistance = 400f;

                hitSize = 80f;

                spawnSound = OlSounds.giantHailstoneFall;
                hitEffect = new MultiEffect(Fx.massiveExplosion, Fx.dynamicWave.wrap(OlLiquids.glacium.color, 60f));
                despawnEffect = OlFx.staticStone;
                hitSound = OlSounds.giantHailstoneHit;

                damage = splashDamage = 250f;
                splashDamageRadius = 80f;
            }}, 1f / 10000000f
            );
        }};

        aghaniteStorm = new HailStormWeather("aghanite-storm"){{
            attrs.set(Attribute.light, -2f);

            duration = 15f * Time.toMinutes;
            soundVol = 0.05f;
            sound = Sounds.wind;

            spawns = 6;
            spawnChance = 0.5f;

            color = Color.valueOf("72665A");
            windDragScaleMin = 0.5f;
            windDragScaleMax = 2f;

            addBullets(
            new FallingRockBulletType("omaloon-aghanite-stone-small"){{
                speed = 2f;
                lifetime = 20f;
                fallHeight = 1f;
                fallDistance = 10f;

                variants = 3;

                hitEffect = Fx.none;
                despawnEffect = OlFx.fellStone;
            }}, 1f,

            new FallingRockBulletType("omaloon-aghanite-stone-medium"){{
                speed = 2f;
                lifetime = 30f;
                fallHeight = 2f;
                fallDistance = 11f;

                variants = 4;

                hitEffect = new MultiEffect(
                //Fx.dynamicWave.layer(Layer.power),
                OlFx.hailStoneSplashSmall
                );
                despawnEffect = OlFx.fellStone;

                damage = splashDamage = 10f;
                splashDamageRadius = 25f;
            }}, 1f / 12f,

            new FallingRockBulletType("omaloon-aghanite-stone-big"){{
                speed = 1f;
                lifetime = 20f;
                fallHeight = 5f;
                fallDistance = 15f;

                variants = 3;

                hitSize = 12f;

                hitEffect = OlFx.hailStoneImpact;

                despawnEffect = OlFx.staticStone;
                hitSound = OlSounds.bigHailstoneHit;

                damage = splashDamage = 95f;
                splashDamageRadius = 40f;
            }}, 1f / 1600f
            );
        }};
    }
}
