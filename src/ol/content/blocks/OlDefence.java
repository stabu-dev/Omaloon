package ol.content.blocks;

import mindustry.content.*;
import mindustry.entities.effect.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.draw.*;
import ol.OlVars;
import ol.content.*;
import ol.type.bullets.*;
import ol.graphics.*;
import ol.world.blocks.defense.*;

import static mindustry.type.ItemStack.*;

public class OlDefence {
    public static Block
            //turrets
            freezing,
            //walls
            testJoinWall,
            zariniWall, tungstenWall,
            omaliteAlloyWall,
            omaliteAlloyWallLarge,
    end; //end?

    public static void load() {
        //region Turrets
        freezing = new PowerTurret("freezing") {{
            requirements(Category.turret, with(
                    Items.copper,         20,
                    Items.lead,           50,
                    Items.graphite,       20,
                    OlItems.omaliteAlloy, 25,
                    Items.silicon,        15
            ));

            size = 3;
            range = 275f;
            recoil = 1f;
            health = 1980;
            inaccuracy = 1f;
            rotateSpeed = 3f;
            shootCone = 0.1f;

            shootSound = OlSounds.olShot;
            ammoUseEffect = Fx.none;
            heatColor = OlPal.oLDarkBlue;

            targetAir = false;
            shootEffect = OlFx.blueShot;
            shootY = 10;

            drawer = new DrawTurret("intensified-");
            shootType = new ControlledBulletType(9f, 240f) {{
                shrinkX = 0;
                sprite = OlVars.fullName("sphere");
                shrinkY = 0;
                lifetime = 29f;
                status = StatusEffects.freezing;
                statusDuration = 120f;

                despawnEffect = hitEffect = new ExplosionEffect() {{
                    waveColor = smokeColor = sparkColor = OlPal.oLBlue;
                    waveStroke = 4f;
                    waveRad = 16f;
                    waveLife = 15f;
                    sparks = 5;
                    sparkRad = 16f;
                    sparkLen = 5f;
                    sparkStroke = 4f;
                }};

                frontColor = OlPal.oLBlue;
                backColor = OlPal.oLBlue;

                width = height = 13f;
                collidesTiles = true;

                trailColor = OlPal.oLBlue;

                trailWidth = 5f;
                trailLength = 9;

                trailEffect = Fx.railTrail;
                chargeEffect = OlFx.blueSphere;

                splashDamage = 95f;
                splashDamageRadius = 26f;
                homingPower = 0.4778f;
                homingRange = 275f;
                drag = 0.008f;
            }};

            shoot.firstShotDelay = 60f;
            moveWhileCharging = false;
            chargeSound = OlSounds.olCharge;
            reload = 140f;
            liquidCapacity = 40;

            consumePower(2f);
            consumeLiquid(OlLiquids.liquidOmalite, 44.2f / 60f);

            smokeEffect = Fx.none;
            squareSprite = false;
        }};
        //endregion Turrets
        //region Walls

        int wallHealthMultiplier = 4;

        zariniWall = new OlJoinWall("zarini-wall") {{
            requirements(Category.defense, with(
                    OlItems.zarini, 4,
                    OlItems.grumon, 2
            ));

            health = 100 * wallHealthMultiplier;
            damageScl = 0.50f;
            damageRad = 3;
            size = 1;
        }};

        tungstenWall = new OlJoinWall("tungsten-wall") {{
            requirements(Category.defense, with(
                    Items.tungsten, 5,
                    OlItems.grumon, 1
            ));

            health = 150 * wallHealthMultiplier;
            damageScl = 0.40f;
            damageRad = 3;
            size = 1;
        }};

        omaliteAlloyWall = new OlWall("omalite-alloy-wall") {{
            requirements(Category.defense, with(
                    OlItems.omaliteAlloy, 5,
                    Items.titanium, 2
            ));

            size = 1;
            health = 355 * wallHealthMultiplier;
            status = StatusEffects.freezing;
            statusDuration = 140f;

            flashColor = OlPal.oLDarkBlue;
            dynamicEffect = Fx.freezing;
            dynamicEffectChance = 0.003f;
            drawDynamicLight = canApplyStatus = insulated = true;

            dynamicLightColor = OlPal.oLBlue;
            dynamicLightRadius = 10f;
            dynamicLightOpacity = 0.2f;
            canBurn = false;
        }};

        omaliteAlloyWallLarge = new OlWall("omalite-alloy-wall-large") {{
            requirements(Category.defense, with(
                    OlItems.omaliteAlloy, 24,
                    Items.titanium, 10
            ));

            size = 2;
            health = 355 * 4 * wallHealthMultiplier;
            status = StatusEffects.freezing;
            statusDuration = 140f;

            flashColor = OlPal.oLDarkBlue;
            dynamicEffect = Fx.freezing;
            dynamicEffectChance = 0.004f;

            drawDynamicLight = canApplyStatus = insulated = true;
            dynamicLightColor = OlPal.oLBlue;
            dynamicLightRadius = 10f;
            dynamicLightOpacity = 0.2f;
            canBurn = false;
        }};

        /*testJoinWall = new OlJoinWall("test-joint"){{
            buildVisibility = BuildVisibility.sandboxOnly;
            category = Category.defense;
            health = 900;
            size = 1;
            canBurn = false;
            damageLink = true;
            damageScl = 0.5f;
        }};*/
        //endregion Walls
    }
}
