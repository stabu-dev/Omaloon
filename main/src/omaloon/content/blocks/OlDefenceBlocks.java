package omaloon.content.blocks;

import arc.graphics.*;
import arc.math.*;
import mindustry.content.*;
import mindustry.entities.bullet.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.draw.*;
import omaloon.content.*;
import omaloon.gen.*;
import omaloon.world.blocks.defense.*;
import omaloon.world.consumers.*;
import omaloon.world.meta.*;

import static mindustry.type.ItemStack.with;

public class OlDefenceBlocks{
    public static Block
    //projectors
    smallShelter,
    //turrets
    apex
    ;

    public static void load(){
        smallShelter = new Shelter("small-shelter"){{
            requirements(Category.effect, with(
            OlItems.cobalt, 25,
            OlItems.nickel, 30
            ));
            researchCostMultiplier = 0.3f;
            size = 2;
//            rechargeStandard = 2f;
//            shieldHealth = 260f;
            range = 120f;

            ambientSound = OlSounds.shelter;
            ambientSoundVolume = 0.08f;

            consumePower(0.2f);
            consume(new ConsumeFluid(null, 5f / 60f){{
                continuous = true;
                hasOptimalPressure = true;

                startRange = 15f;
                endRange = 50f;
                efficiencyMultiplier = 2f;
                optimalPressure = 46.5f;

                curve = t -> Math.max(0f, Mathf.slope(t - 0.25f) * 2f - 1f);
            }});

            pressureConfig = new PressureConfig(){{
                acceptsPressure = true;
            }};
        }};
        //endregion
        //region turrets
        apex = new ItemTurret("apex"){{
            requirements(Category.turret, with(
            OlItems.composite, 10,
            OlItems.cobalt, 20
            ));
            outlineColor = Color.valueOf("2f2f36");
            ammo(OlItems.cobalt,
            new BasicBulletType(3f, 9){{
                width = 7f;
                height = 9f;
                lifetime = 20.8f;
                ammoMultiplier = 3;

                shootEffect = Fx.shootSmallColor;
                smokeEffect = Fx.shootSmallSmoke;
                hitEffect = Fx.hitBulletColor;
                despawnEffect = Fx.hitBulletColor;
                hitColor = OlItems.cobalt.color;

                trailWidth = 1.2f;
                trailLength = 10;
                trailColor = OlItems.cobalt.color;

                backColor = OlItems.cobalt.color;
                frontColor = Color.white;

                fragBullet = new BasicBulletType(2.5f, 2.5f){{
                    width = 4f;
                    height = 4f;
                    lifetime = 15f;

                    hitEffect = Fx.hitBulletColor;
                    despawnEffect = Fx.hitBulletColor;
                    hitColor = OlItems.cobalt.color;
                    backColor = OlItems.cobalt.color;

                    trailWidth = 0.8f;
                    trailLength = 5;
                    trailColor = OlItems.cobalt.color;
                }};

                fragOnHit = true;
                fragBullets = 4;
                fragRandomSpread = 45f;
                fragVelocityMin = 0.7f;
            }},
            Items.graphite, new BasicBulletType(4f, 16){{
                width = 7f;
                height = 10f;
                lifetime = 30f;
                ammoMultiplier = 2;
                reloadMultiplier = 0.9f;
                rangeChange = 20f;

                shootEffect = Fx.shootSmall;
                smokeEffect = Fx.shootSmallSmoke;
                hitEffect = Fx.hitBulletColor;
                despawnEffect = Fx.hitBulletColor;
                hitColor = Items.graphite.color;

                trailWidth = 1.5f;
                trailLength = 8;
                trailColor = Items.graphite.color;

                backColor = Items.graphite.color;
                frontColor = Color.white;
                knockback = 0.8f;
            }}
            );

            shootY = 0f;

            shootSound = OlSounds.theShoot;

            drawer = new DrawTurret("gl-");

            reload = 30f;
            range = 100f;

            inaccuracy = 2f;
            rotateSpeed = 10f;
        }};
    }
}
