package omaloon.content;

import arc.graphics.*;
import arc.math.*;
import arc.struct.*;
import ent.anno.Annotations.*;
import mindustry.ai.types.*;
import mindustry.content.*;
import mindustry.entities.bullet.*;
import mindustry.entities.effect.*;
import mindustry.entities.part.*;
import mindustry.entities.pattern.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import omaloon.ai.*;
import omaloon.entities.abilities.*;
import omaloon.entities.bullet.*;
import omaloon.entities.part.*;
import omaloon.gen.*;
import omaloon.type.*;

public class OlUnitTypes {
    // flying
    public static UnitType cilantro, basil, sage;

    // mech
    public static UnitType legionnaire, centurion, praetorian;

    // lumen
    public static UnitType lumen;

    // ornitopter
    public static @EntityDef({Unitc.class, Flyingc.class, Ornitopterc.class}) UnitType effort;

    // millipede
    public static @EntityDef({Unitc.class, Millipedec.class, Legsc.class}) UnitType collector;

    // core
    public static UnitType discovery;

    public static @EntityDef({Unitc.class, Dronec.class}) DroneUnitType attackDroneAlpha, actionDroneMono;

    public static @EntityDef({Unitc.class, Mechc.class, Masterc.class}) MasterUnitType walker;

    public static void load() {
        collector = new MillipedeUnitType("collector"){{
            constructor = LegsMillipedeUnit::create;
            aiController = MillipedeAI::new;
            speed = 0.6f;
            health = 200f;
            regenTime = 15f * 60f;
            chainable = true;
            omniMovement = false;
            angleLimit = 65f;
            segmentLength = 5;
            segmentDamageScl = 8f;
            segmentCast = 8;
            segmentOffset = 7.3f;
            maxSegments = 20;
            preventDrifting = true;
            hidden = true;

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
            headLegCount = segmentLegCount = tailLegCount = 2;

            legMoveSpace = 2f;
            allowLegStep = true;
            hovering = false;
            legPhysicsLayer = true;

            Seq<Weapon> weaponSeq = Seq.with(
              new Weapon("omaloon-collector-beam") {{
                  x = 0f;
                  y = 1f;
                  rotate = true;
                  mirror = false;
                  reload = 60f;
                  bullet = new ArtilleryBulletType(5f, 7) {{
                      maxRange = 40f;
                      collidesTiles = collidesAir = collidesGround = true;
                      width = height = 11f;
                      splashDamage = 25f;
                      splashDamageRadius = 25f;
                      trailColor = hitColor = lightColor = backColor = Pal.thoriumPink;
                      frontColor = Pal.thoriumPink;
                  }};
              }}
            );
            segmentWeapons = new Seq[] {
              Seq.with(),
              weaponSeq, weaponSeq, weaponSeq,
              weaponSeq, weaponSeq, weaponSeq,
              weaponSeq, weaponSeq, weaponSeq,
              weaponSeq, weaponSeq, weaponSeq,
              weaponSeq, weaponSeq, weaponSeq,
              weaponSeq, weaponSeq, weaponSeq,
              Seq.with()
            };
        }};

        //region core
        attackDroneAlpha = new DroneUnitType("combat-drone-alpha") {{
            constructor = DroneUnit::create;
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
            constructor = DroneUnit::create;
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
        walker = new MasterUnitType("walker") {{
            constructor = MasterMechUnit::create;
            aiController = BuilderAI::new;

            droneConstructTime = 180f;

            gunUnitType = attackDroneAlpha;
            actionUnitType = actionDroneMono;

            actionBuildRange = 200f;

            rotateToBuilding = faceTarget = false;

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
            isEnemy = hittable = false;

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
        //endregion

        effort = new OrnitopterUnitType("effort"){{
            constructor = OrnitopterFlyingUnit::create;
            aiController = CowardAI::new;
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

            abilities.add(
                new JavelinAbility(20f, 5f, 29f) {{
                    minDamage = 5f;
                    minSpeed = 2;
                    maxSpeed = 4;
                    magX = 0.2f;
                    magY = 0.1f;
                }}
            );

            weapons.add(new Weapon(name + "-launcher"){{
                layerOffset = 1f;
                mirror = true;
                x = 4.7f; y = 2f;
                shootCone = 60f;
                smoothReloadSpeed = 0.5f;
                shootSound = Sounds.missile;

                reload = 50f;

                shoot.shots = 2;
                shoot.shotDelay = 7f;

                bullet = new MissileBulletType(3f, 3f){{
                    width = 5f;
                    height = 4f;
                    shrinkY = 0f;
                    homingRange = 60f;
                    maxRange = 120;
                    splashDamageRadius = 25f;
                    splashDamage = 5f;
                    lifetime = 45f;
                    frontColor = backColor = trailColor = Color.valueOf("feb380");
                    trailChance = 0f;
                    trailInterval = 3f;
                    hitEffect = Fx.blastExplosion;
                    despawnEffect = Fx.blastExplosion;
                    weaveScale = 6f;
                    weaveMag = 1f;
                }};
            }});
            hitSize = 16;
        }};

        lumen = new GlassmoreUnitType("lumen") {{
            constructor = UnitEntity::create;

            hitSize = 10f;

            speed = 1.7f;
            accel = 0.08f;
            drag = 0.04f;

            flying = true;
            range = 20f;
            health = 70;

            weapons.add(new FilterWeapon() {{
                mirror = false;
                x = 0;
                y = 4f;

                shootSound = Sounds.release;
                shoot = new ShootSpread(30, 1);
                inaccuracy = 12f;
                velocityRnd = 0.8f;
                reload = 30f;

                bullets = new BulletType[]{
                  new LiquidBulletType(OlLiquids.glacium){{
                      recoil = 0.06f;
                      killShooter = true;

                      //boilTime = 10f;
                      speed = 2.5f;
                      drag = 0.009f;
                      shootEffect = Fx.shootSmall;
                      lifetime = 27f;
                      collidesAir = false;
                      status = OlStatusEffects.glacied;
                      statusDuration = 60f * 5f;

                      despawnSound = hitSound = Sounds.splash;

                      shootEffect = new MultiEffect(
                        Fx.shootSmall/*,
                        new LumenLiquidEffect(
                          30f, Color.valueOf("363F9A"), Color.valueOf("486ACD"), Color.valueOf("7090EA")
                        ).layer(Layer.effect + 1)*/
                      );
                  }},
                  new LiquidBulletType(Liquids.water){{
                      recoil = 0.06f;
                      killShooter = true;

                      //boilTime = 10f;
                      speed = 2.5f;
                      drag = 0.009f;
                      shootEffect = Fx.shootSmall;
                      lifetime = 27f;
                      collidesAir = false;
                      status = StatusEffects.wet;
                      statusDuration = 60f * 5f;

                      despawnSound = hitSound = Sounds.splash;

                      shootEffect = new MultiEffect(
                        Fx.shootSmall/*,
                        new LumenLiquidEffect(
                          30f, Color.valueOf("61615B"), Color.valueOf("313131"), Color.valueOf("1D1D23")
                        ).layer(Layer.effect + 1)*/
                      );
                  }},
                  new LiquidBulletType(Liquids.slag){{
                      recoil = 0.06f;
                      killShooter = true;

                      //boilTime = 10f;
                      speed = 2.5f;
                      drag = 0.009f;
                      shootEffect = Fx.shootSmall;
                      lifetime = 27f;
                      collidesAir = false;
                      status = StatusEffects.melting;
                      statusDuration = 60f * 5f;

                      despawnSound = hitSound = Sounds.splash;

                      shootEffect = new MultiEffect(
                        Fx.shootSmall/*,
                        new LumenLiquidEffect(
                          30f, Color.valueOf("3E6067"), Color.valueOf("5E929D"), Color.valueOf("8CDAEA")
                        ).layer(Layer.effect + 1)*/
                      );
                  }},
                  new LiquidBulletType(Liquids.oil){{
                      recoil = 0.06f;
                      killShooter = true;

                      //boilTime = 10f;
                      speed = 2.5f;
                      drag = 0.009f;
                      shootEffect = Fx.shootSmall;
                      lifetime = 27f;
                      collidesAir = false;
                      status = StatusEffects.tarred;
                      statusDuration = 60f * 5f;

                      despawnSound = hitSound = Sounds.splash;

                      shootEffect = new MultiEffect(
                        Fx.shootSmall/*,
                        new LumenLiquidEffect(
                          30f, Color.valueOf("3E6067"), Color.valueOf("5E929D"), Color.valueOf("8CDAEA")
                        ).layer(Layer.effect + 1)*/
                      );
                  }}
                };
				icons = new String[] {
                        "omaloon-filled-with-glacium",
                        "omaloon-filled-with-water",
                        "omaloon-filled-with-slag",
                        "omaloon-filled-with-oil"
				};
                bulletFilter = unit -> {
                    if (unit.hasEffect(OlStatusEffects.filledWithGlacium)) return bullets[0];
                    if (unit.hasEffect(OlStatusEffects.filledWithWater)) return bullets[1];
                    if (unit.hasEffect(OlStatusEffects.filledWithSlag)) return bullets[2];
                    if (unit.hasEffect(OlStatusEffects.filledWithOil)) return bullets[3];
                    return new BulletType(0,0){{
                        shootEffect = smokeEffect = hitEffect = despawnEffect = Fx.none;
                    }};
                };
            }});
        }};

        //region roman
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

                shoot.shots = 2;
                shoot.shotDelay = 4f;

                ejectEffect = Fx.casing1;
                bullet = new BasicBulletType(2.5f, 5){{
                    width = 7f;
                    height = 7f;
                    lifetime = 35f;

                    maxRange = 100;

                    despawnEffect = Fx.hitBulletSmall;
                    hitEffect = Fx.none;
                    hitColor = backColor = trailColor = Color.valueOf("feb380");

                    trailWidth = 1.3f;
                    trailLength = 10;
                }};
            }});
        }};
        centurion = new GlassmoreUnitType("centurion") {{
            constructor = MechUnit::create;
            speed = 0.3f;
            hitSize = 16f;
            rotateSpeed = 2f;
            health = 400;
            range = 80f;

            parts.add(new ConstructPart("-can") {{
                y = 3.75f;
                outlineLayerOffset = -0.01f;
                progress = PartProgress.reload.inv();
            }});

            weapons.add(new Weapon("") {{
                x = 0f;
                y = 3.75f;
                reload = 100f;
                mirror = false;

                shootSound = Sounds.missileLarge;
                bullet = new LaunchBulletType(1f, 0) {{
                    sprite = "omaloon-centurion-can";
                    frontColor = Color.white;
                    lifetime = 120f;
                    width = height = 12f;
                    drawSize = 240f;
                    fadeAt = 0.4f;
                    pitch = 0.3f;
                    trailWidth = 4f;
                    trailLength = 5;

                    despawnSound = Sounds.artillery;
                    despawnEffect = new WaveEffect() {{
                        colorFrom = colorTo = Color.valueOf("FEB380");
                        interp = Interp.bounceOut;
                        lifetime = 60f;
                        sizeFrom = sizeTo = 24f;
                    }};
                    hitEffect = Fx.none;

                    shrinkInterp = Interp.smooth;
                    fragInterp = Interp.circleIn;
                    shadowInterp = Interp.circleOut;

                    fragBullets = 3;
                    fragBullet = new BasicBulletType(6f, 10, "omaloon-cross-bullet") {{
                        frontColor = backColor = hitColor = trailColor = Color.valueOf("FEB380");
                        lifetime = 20f;
                        width = height = 12f;
                        shrinkX = shrinkY = 0f;
                        drag = 0.15f;
                        trailWidth = 2f;
                        trailLength = 5;

                        hitSound = despawnSound = Sounds.explosion;
                    }};
                }};
            }});
        }};
        praetorian = new GlassmoreUnitType("praetorian") {{
            constructor = MechUnit::create;
            speed = 0.36f;
            hitSize = 22f;
            rotateMoveFirst = true;
            rotateSpeed = 1.5f;
            health = 1200;
            range = 80f;

            weapons.add(new Weapon("omaloon-praetorian-weapon") {{
                continuous = alwaysContinuous = true;
                top = alternate = false;

                rotate = true;
                rotateSpeed = 5f;
                rotationLimit = 15f;
                shootCone = 45f;
                recoil = 0.3f;

                x = 13.75f;
                y = 2.45f;
                shootX = -3f;
                shootY = 6f;

                shootSound = Sounds.smelter;
                bullet = new ContinuousFlameBulletType(5) {{
                    colors = new Color[] {Color.valueOf("BC5452"), Color.valueOf("FEB380")};

                    length = 80f;

                    flareColor = Color.valueOf("FEB380");
                    lengthInterp = a -> Interp.smoother.apply(Mathf.slope(a));
                    flareInnerLenScl = flareRotSpeed = 0f;
                    flareLength = 17f;
                    flareWidth = 4f;

                    shootEffect = new ParticleEffect() {{
                        line = true;

                        lifetime = 30f;
                        cone = 45f;
                        length = 40f;

                        colorFrom = Pal.missileYellow;
                        colorTo = Pal.missileYellowBack;

                        interp = Interp.pow2Out;
                    }};
                    hitEffect = new ParticleEffect() {{
                        line = true;

                        lifetime = 20f;

                        colorFrom = Pal.missileYellow;
                        colorTo = Pal.missileYellowBack;

                        interp = Interp.sineOut;
                    }};
                }};
            }});
        }};
        //endregion

        //region vegetable
        cilantro = new GlassmoreUnitType("cilantro") {{
            flying = lowAltitude = true;
            hitSize = 8f;

            accel = 0.05f;
            drag = 0.03f;
            rotateSpeed = 10f;
            trailLength = 10;

            constructor = UnitEntity::create;

            weapons.addAll(new Weapon() {{
                mirror = false;

                x = 0;
                y = 1;

                reload = 30;
                shoot.firstShotDelay = 60f;

                shootSound = Sounds.lasershoot;
                bullet = new BasicBulletType(2f, 41, "omaloon-triangle-bullet") {{
                    width = height = 8f;
                    shrinkY = 0f;
                    trailWidth = 2f;
                    trailLength = 5;

                    frontColor = Color.valueOf("D1EFFF");
                    backColor = hitColor = trailColor = Color.valueOf("8CA9E8");

                    chargeEffect = OlFx.shootShockwave;
                    shootEffect = smokeEffect = Fx.none;
                }};
            }});
        }};
        basil = new GlassmoreUnitType("basil") {{
            flying = lowAltitude = true;
            hitSize = 20f;

            drag = 0.09f;
            speed = 1.8f;
            rotateSpeed = 2.5f;
            accel = 0.05f;

            engineOffset = 12f;
            setEnginesMirror(new UnitEngine(5, -10f, 2, -45));

            constructor = UnitEntity::create;

            weapons.addAll(new Weapon() {{
                mirror = false;
                continuous = alwaysContinuous = true;

                x = 0f;
                y = -3f;
                shootSound = Sounds.smelter;

                bullet = new ContinuousFlameBulletType(5) {{
                    colors = new Color[] {Color.valueOf("8CA9E8"), Color.valueOf("8CA9E8"), Color.valueOf("D1EFFF")};

                    lifetime = 60f;

                    shootCone = 360f;

                    width = 2.5f;
                    length = 75f;
                    lengthInterp = a -> Interp.smoother.apply(Mathf.slope(a));
                    flareLength = 20f;
                    flareInnerLenScl = flareRotSpeed = 0f;
                    flareColor = Color.valueOf("D1EFFF");

                    hitEffect = new ParticleEffect() {{
                        lifetime = 30f;
                        length = 20f;

                        interp = Interp.pow2Out;

                        colorFrom = Color.valueOf("D1EFFF");
                        colorTo = Color.valueOf("8CA9E8");
                    }};
                }};
            }});
        }};
        sage = new GlassmoreUnitType("sage") {{
            flying = lowAltitude = true;
            hitSize = 35f;

            speed = 0.8f;
            accel = 0.04f;
            drag = 0.04f;
            rotateSpeed = 1.9f;

            constructor = UnitEntity::create;

            engineOffset = 16f;
            engineSize = 6f;
            setEnginesMirror(new UnitEngine(10, -14f, 3, -45));

            BulletType shootType = new BasicBulletType(2f, 5) {{
                lifetime = 55f;

                width = height = 8f;
                shrinkY = 0f;

                trailWidth = 2f;
                trailLength = 5;

                frontColor = Color.valueOf("D1EFFF");
                backColor = trailColor = Color.valueOf("8CA9E8");

                hitEffect = despawnEffect = OlFx.hitSage;
                hitSound = despawnSound = Sounds.plasmaboom;
            }};

            weapons.addAll(
              new Weapon("omaloon-sage-salvo") {{
                  reload = 90f;
                  rotate = true;
                  rotateSpeed = 12f;
                  x = 6.5f;
                  y = 1f;

                  shoot.firstShotDelay = 40f;

                  shootSound = Sounds.missile;
                  bullet = shootType;
              }},
              new Weapon("omaloon-sage-salvo") {{
                  reload = 90f;
                  rotate = true;
                  rotateSpeed = 14f;
                  x = -10.25f;
                  y = -8f;

                  shootSound = Sounds.missile;
                  bullet = shootType;
              }}
            );
        }};
        //endregion
    }
}
