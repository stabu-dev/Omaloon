package ol.content.blocks;

import arc.graphics.*;

import mindustry.content.*;
import mindustry.entities.bullet.*;
import mindustry.entities.effect.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.draw.*;

import ol.content.*;
import ol.graphics.*;
import ol.type.bullets.*;
import ol.world.blocks.defense.*;

import static mindustry.type.ItemStack.*;

public class OlDeffenceBlocks {
    public static Block
    //turrets
    tau,
    freezing,
    //walls
    grumonModularWall, tungstenModularWall,
    omaliteWall, omaliteWallLarge;
    //end

    public static void load(){
        //turrets
        tau = new PowerTurret("tau"){{
            requirements(Category.turret, empty);
            size = 2;
            scaledHealth = 240;

            range = 200;
            reload = 60;
            recoil = 0;

            shootCone = 0.3f;
            shootSound = OlSounds.piu;
            shootEffect = Fx.none;
            targetGround = false;
            ammoUseEffect = Fx.none;
            ammoPerShot = 1;

            drawer = new DrawTurret("intensified-");
            shootType = new LaserBoltBulletType() {{
                speed = 50;
                lifetime = 3.9f;
                damage = 55;

                backColor = trailColor = Pal.heal;
                frontColor = Color.white;
                despawnEffect = hitEffect = smokeEffect = Fx.none;

                trailEffect = OlFx.tauTrail;

                trailInterval = 0.006f;
                trailParam = 4f;
                trailRotation = true;

                status = StatusEffects.shocked;
            }};

            consumePower(1.2f);
        }};

        freezing = new PowerTurret("freezing") {{
            requirements(Category.turret, empty);
            size = 3;
            range = 275f;
            recoil = 0f;
            health = 1980;
            inaccuracy = 1f;
            rotateSpeed = 3f;
            shootCone = 0.1f;
            shootSound = OlSounds.freezingShot;
            ammoUseEffect = Fx.none;
            heatColor = OlPal.omaloonDarkBlue;
            shootEffect = OlFx.omaloonBlueShot;
            shootY = 10;
            drawer = new DrawTurret("intensified-");
            shootType = new ControlBulletType(5f, 240f) {{
                shrinkX = 0;
                sprite = "ol-sphere";
                shrinkY = 0;
                lifetime = 53f;
                status = StatusEffects.freezing;
                statusDuration = 120f;
                despawnEffect = hitEffect = new ExplosionEffect() {{
                    waveColor = smokeColor = sparkColor = OlPal.omaloonBlue;
                    waveStroke = 4f;
                    waveRad = 16f;
                    waveLife = 15f;
                    sparks = 5;
                    sparkRad = 16f;
                    sparkLen = 5f;
                    sparkStroke = 4f;
                }};
                frontColor = OlPal.omaloonBlue;
                backColor = OlPal.omaloonBlue;
                width = height = 13f;
                collidesTiles = true;
                trailColor = OlPal.omaloonBlue;
                trailWidth = 5f;
                trailLength = 9;
                trailEffect = Fx.railTrail;
                chargeEffect = OlFx.omaloonBlueCharge;
                splashDamage = 95f;
                splashDamageRadius = 26f;
                homingPower = 0.4778f;
                homingRange = 275f;
                drag = 0.008f;
            }};

            shoot.firstShotDelay = 60f;
            moveWhileCharging = false;
            chargeSound = OlSounds.freezingCharge;
            reload = 140f;
            liquidCapacity = 40;

            consumePower(2f);
            consumeLiquid(OlLiquids.liquidOmalite, 44.2f / 60f);
            ammoPerShot = 1;

            smokeEffect = Fx.none;
            squareSprite = false;
        }};
        //end turrets
        //walls
        int wallHealthMultiplier = 4;

        grumonModularWall = new ConnectiveWall("grumon-connective-wall") {{
            requirements(Category.defense, empty);

            health = 100 * wallHealthMultiplier;
            damageScl = 0.50f;
            damageRad = 3;
            size = 1;
        }};

        tungstenModularWall = new ConnectiveWall("tungsten-connective-wall") {{
            requirements(Category.defense, empty);

            health = 150 * wallHealthMultiplier;
            damageScl = 0.40f;
            damageRad = 3;
            size = 1;
        }};

        omaliteWall = new OlWall("omalite-wall"){{
            requirements(Category.defense, empty);

            size = 1;
            health = 355 * wallHealthMultiplier;
            status = StatusEffects.freezing;
            statusDuration = 140f;

            flashColor = OlPal.omaloonDarkBlue;
            dynamicEffect = Fx.freezing;
            dynamicEffectChance = 0.003f;
            drawDynamicLight = canApplyStatus = insulated = true;

            dynamicLightColor = OlPal.omaloonBlue;
            dynamicLightRadius = 10f;
            dynamicLightOpacity = 0.2f;
            canBurn = false;
        }};

        omaliteWallLarge = new OlWall("omalite-wall-large"){{
            requirements(Category.defense, empty);

            size = 2;
            health = 355 * 4 * wallHealthMultiplier;
            status = StatusEffects.freezing;
            statusDuration = 140f;

            flashColor = OlPal.omaloonDarkBlue;
            dynamicEffect = Fx.freezing;
            dynamicEffectChance = 0.004f;

            drawDynamicLight = canApplyStatus = insulated = true;
            dynamicLightColor = OlPal.omaloonBlue;
            dynamicLightRadius = 10f;
            dynamicLightOpacity = 0.2f;
            canBurn = false;
        }};
    }
}
