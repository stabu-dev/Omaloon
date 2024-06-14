package omaloon.content;

import arc.graphics.*;
import arc.struct.*;
import ent.anno.Annotations.*;
import mindustry.ai.types.*;
import mindustry.content.*;
import mindustry.entities.bullet.*;
import mindustry.entities.part.*;
import mindustry.entities.pattern.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import omaloon.ai.*;
import omaloon.gen.*;
import omaloon.type.*;

public class OlUnitTypes {
    // flying
    public static UnitType cilantro, basil, sage;

    // mech
    public static UnitType legionnaire;

    // ornitopter
    public static @EntityDef({Unitc.class, Flyingc.class, Ornitopterc.class}) UnitType effort;

    // millipede
    public static @EntityDef({Unitc.class, Millipedec.class, Legsc.class}) UnitType collector;

    // core
    public static UnitType discovery;

    public static @EntityDef({Unitc.class, Dronec.class}) DroneUnitType attackDroneAlpha, actionDroneMono;

    public static @EntityDef({Unitc.class, Mechc.class, Masterc.class}) MasterUnitType beginner;

    public static void load() {
        attackDroneAlpha = new DroneUnitType("combat-drone-alpha") {{
            constructor = UnitEntity::create;
            controller = u -> new AttackDroneAI();

            itemCapacity = 0;
            speed = 2.2f;
            accel = 0.08f;
            drag = 0.04f;
            flying = true;
            health = 70;
            engineOffset = 4f;
            engineSize = 2;
            hitSize = 9;

            isEnemy = false;

            weapons.add(new Weapon(){{
                y = 0f;
                x = 1.5f;
                reload = 20f;
                ejectEffect = Fx.casing1;
                bullet = new BasicBulletType(2.5f, 6){{
                    width = 7f;
                    height = 9f;
                    lifetime = 45f;

                    hitColor = backColor = trailColor = Color.valueOf("feb380");

                    trailWidth = 1.3f;
                    trailLength = 7;

                    shootEffect = Fx.shootSmall;
                    smokeEffect = Fx.shootSmallSmoke;
                    ammoMultiplier = 2;
                }};
                shootSound = Sounds.pew;
            }});
            shadowElevationScl = 0.4f;
        }};
        actionDroneMono = new DroneUnitType("main-drone-mono") {{
            constructor = UnitEntity::create;
            controller = u -> new ActionDroneAI();
            mineTier = 4;
            itemCapacity = 1;

            speed = 2.2f;
            accel = 0.08f;
            drag = 0.04f;
            flying = true;
            health = 70;
            engineOffset = 4f;
            engineSize = 2;

            buildRange = 60f;
            buildSpeed = 1f;
            mineRange = 40;

            hitSize = 9;
            isEnemy = false;

            shadowElevationScl = 0.4f;
        }};
        beginner = new MasterUnitType("beginner") {{
            constructor = MasterMechUnit::create;
            aiController = BuilderAI::new;

            droneConstructTime = 180f;

            attackUnitType = attackDroneAlpha;
            actionUnitType = actionDroneMono;

            actionBuildRange = 200f;

            faceTarget = false;
            canBoost = true;

            speed = 0.5f;
            hitSize = 8f;
            health = 150;
            boostMultiplier = 0.8f;

            mineTier = 3;
            mineRange = 200;

            weapons.add(new Weapon() {{
                controllable = aiControllable = false;
                autoTarget = true;
                minWarmup = 2f;
            }});

            shadowElevationScl = 0.3f;
        }};

        discovery = new GlassmoreUnitType("discovery"){{
            controller = u -> new BuilderAI(true, 500f);
            constructor = UnitEntity::create;
            isEnemy = false;

            lowAltitude = true;
            flying = true;
            mineSpeed = 4.5f;
            mineTier = 2;
            mineItems = Seq.with(OlItems.cobalt, Items.beryllium);
            buildSpeed = 0.3f;
            drag = 0.03f;
            speed = 2f;
            rotateSpeed = 13f;
            accel = 0.1f;
            itemCapacity = 20;
            health = 110f;
            engineOffset = 5f;
            hitSize = 8f;
            alwaysUnlocked = true;
        }};

        //region vegetable
        cilantro = new GlassmoreUnitType("cilantro") {{
            flying = true;

            accel = 0.05f;
            drag = 0.03f;
            rotateSpeed = 10f;
            trailLength = 10;

            constructor = UnitEntity::create;

            weapons.addAll(new Weapon() {{
                mirror = false;

                reload = 60;

                bullet = new BasicBulletType(8f, 41) {{
                    knockback = 4f;
                    width = 25f;
                    hitSize = 7f;
                    height = 20f;
                    shootEffect = Fx.shootBigColor;
                    smokeEffect = Fx.shootSmokeSquareSparse;
                    ammoMultiplier = 1;
                    hitColor = backColor = trailColor = Color.valueOf("D1EFFF");
                    frontColor = Pal.redLight;
                    trailWidth = 6f;
                    trailLength = 3;
                    hitEffect = despawnEffect = Fx.hitSquaresColor;
                    buildingDamageMultiplier = 0.2f;
                }};

                shoot = new ShootSpread(15, 4f);
            }});
        }};
        basil = new GlassmoreUnitType("basil") {{
            flying = true;

            accel = 0.05f;
            drag = 0.03f;
            engineOffset = 13f;
            rotateSpeed = 2f;
            trailLength = 20;

            constructor = UnitEntity::create;

            weapons.addAll(new Weapon() {{
                mirror = false;

                reload = 90;

                bullet = new BasicBulletType(4f, 40, "circle-bullet") {{
                    fragBullet = new BasicBulletType(4f, 40, "circle-bullet");

                    fragAngle = 135f;
                    fragVelocityMax = fragVelocityMin = 1f;
                    fragRandomSpread = 0f;
                    fragSpread = -90f;

                    fragBullets = 2;
                }};
            }});
        }};
        sage = new GlassmoreUnitType("sage") {{
            flying = true;
            lowAltitude = true;

            accel = 0.05f;
            drag = 0.03f;
            engineOffset = 16f;
            engineSize = 6f;
            rotateSpeed = 2f;
            trailLength = 50;
            trailScl = 1f;

            constructor = UnitEntity::create;

            setEnginesMirror(new UnitEngine(10, -14f, 3, -45));

            weapons.addAll(
	              new Weapon("omaloon-sage-salvo") {{
                    reload = 90f;
                    x = 6.5f;
                    y = 1f;
	              }},
	              new Weapon("omaloon-sage-salvo") {{
                    reload = 60f;
                    x = 10.25f;
                    y = -8f;
                }}
            );
        }};
        //endregion

        effort = new OrnitopterUnitType("effort"){{
            constructor = OrnitopterFlyingUnit::create;
            lowAltitude = true;
            speed = 2.7f;
            accel = 0.08f;
            drag = 0.04f;
            flying = true;
            health = 210;
            range = 15 * 8f;
            maxRange = range;
            rotateMoveFirst = true;
            rotateSpeed = 6f;
            fallDriftScl = 60f;

            for(float angle : new float[]{40, -40}){
                blades.addAll(new Blade(name + "-blade"){{
                    x = 6f;
                    y = 2f;
                    bladeMaxMoveAngle = angle;
                    blurAlpha = 1f;
                }});
            }

            parts.add(new RegionPart("-tusk"){{
                layerOffset = -0.001f;
                mirror = true;
                x = 2.7f; y = 8.6f;
                outline = true;
            }});

            weapons.add(new Weapon(name + "-launcher"){{
                layerOffset = 1f;
                mirror = true;
                x = 4.7f; y = 2f;
                reload = 30f;
                shootCone = 60f;
                smoothReloadSpeed = 0.5f;
                shootSound = Sounds.missile;

                bullet = new MissileBulletType(3f, 5f){{
                    width = 6f;
                    height = 6f;
                    shrinkY = 0f;
                    homingRange = 60f;
                    maxRange = 120;
                    splashDamageRadius = 25f;
                    splashDamage = 5f;
                    lifetime = 45f;
                    frontColor = backColor = trailColor = Color.valueOf("feb380");
                    hitEffect = Fx.blastExplosion;
                    despawnEffect = Fx.blastExplosion;
                    weaveScale = 6f;
                    weaveMag = 1f;
                }};
            }});
            hitSize = 16;
        }};

        legionnaire = new GlassmoreUnitType("legionnaire"){{
            constructor = MechUnit::create;
            speed = 0.4f;
            hitSize = 9f;
            health = 180;
            range = 100;
            weapons.add(new Weapon("omaloon-legionnaire-weapon"){{
                shootSound = OlSounds.theShoot;
                mirror = true;
                top = false;

                x = 5.5f;
                y = 0.7f;
                shootX = -0.5f;
                shootY = 5.5f;
                reload = 35f;
                recoil = 0.6f;

                shoot.shots = 3;
                shoot.shotDelay = 4f;

                ejectEffect = Fx.casing1;
                bullet = new BasicBulletType(2.5f, 5){{
                    width = 7f;
                    height = 7f;
                    lifetime = 10f;

                    maxRange = 100;

                    despawnEffect = Fx.hitBulletSmall;
                    hitEffect = Fx.none;
                    hitColor = backColor = trailColor = Color.valueOf("feb380");

                    trailWidth = 1.3f;
                    trailLength = 10;

                    fragOnHit = true;
                    fragBullets = 2;
                    fragRandomSpread = 25f;
                    fragVelocityMin = 0.7f;

                    fragBullet = new BasicBulletType(2.5f, 4.5f) {{
                        width = 4f;
                        height = 4f;
                        lifetime = 25f;

                        despawnEffect = Fx.none;
                        hitEffect = Fx.none;
                        hitColor = backColor = trailColor = Color.valueOf("feb380");

                        trailWidth = 0.8f;
                        trailLength = 10;
                    }};
                }};
            }});
        }};

        collector = new MillipedeUnitType("collector"){{
            constructor = LegsMillipedeUnit::create;
            speed = 0.6f;
            health = 200f;
            regenTime = 15f * 60f;
            splittable = true;
            chainable = true;
            omniMovement = false;
            angleLimit = 65f;
            segmentLength = 5;
            segmentDamageScl = 8f;
            segmentCast = 8;
            segmentOffset = 7.3f;
            maxSegments = 4;
            preventDrifting = true;

            legCount = 2;
            legLength = 8f;
            lockLegBase = true;
            legContinuousMove = true;
            legExtension = -2f;
            legBaseOffset = 3f;
            legMaxLength = 1.1f;
            legMinLength = 0.2f;
            legLengthScl = 0.96f;
            legForwardScl = 0.7f;
            legGroupSize = 2;
            rippleScale = 0.7f;

            legMoveSpace = 2f;
            allowLegStep = true;
            hovering = false;
            legPhysicsLayer = true;

            segWeapSeq.add(new Weapon("omaloon-collector-beam"){{
                top = true;
                rotate = true;
                mirror = false;
                reload = 60f;
                autoTarget = true;
                controllable = false;
                bullet = new ArtilleryBulletType(5f, 7){{
                    maxRange = 40f;
                    collidesTiles = collidesAir = collidesGround = true;
                    width = height = 11f;
                    splashDamage = 25f;
                    splashDamageRadius = 25f;
                    trailColor = hitColor = lightColor = backColor = Pal.thoriumPink;
                    frontColor = Pal.thoriumPink;
                }};
            }});
        }};
    }
}
