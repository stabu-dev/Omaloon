package omaloon.content;

import arc.graphics.*;
import arc.math.Interp;
import arc.math.Mathf;
import arc.struct.*;
import ent.anno.Annotations.*;
import mindustry.*;
import mindustry.ai.types.*;
import mindustry.content.*;
import mindustry.entities.bullet.*;
import mindustry.entities.effect.*;
import mindustry.entities.part.*;
import mindustry.entities.pattern.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.meta.*;
import omaloon.ai.*;
import omaloon.entities.effect.*;
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

            Seq<Weapon> weaponSeq = Seq.with(
              new Weapon("omaloon-collector-beam") {{
                  x = 0f;
                  y = 1f;
                  rotate = true;
                  mirror = false;
                  reload = 60f;
                  autoTarget = true;
                  controllable = false;
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
              weaponSeq,
              weaponSeq,
              weaponSeq,
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
            constructor = ElevationMoveUnit::create;

            health = 200;

            weapons.add(new FilterWeapon() {{
                mirror = false;
                x = 0;
                y = 4f;

                bullets = new BulletType[] {
                  new BulletType(4f, 30) {{
                      lifetime = 8f;
                      status = StatusEffects.wet;
                      statusDuration = 60f * 5f;

                      shootEffect = new MultiEffect(
                        Fx.shootSmall,
                        new LumenLiquidEffect(
                          30f, Color.valueOf("363F9A"), Color.valueOf("486ACD"), Color.valueOf("7090EA")
                        ).layer(Layer.effect + 1)
                      );
                  }},
                  new BulletType(4f, 30) {{
                      lifetime = 8f;
                      status = StatusEffects.tarred;
                      statusDuration = 60f * 5f;

                      shootEffect = new MultiEffect(
                        Fx.shootSmall,
                        new LumenLiquidEffect(
                          30f, Color.valueOf("61615B"), Color.valueOf("313131"), Color.valueOf("1D1D23")
                        ).layer(Layer.effect + 1)
                      );
                  }},
                  new BulletType(4f, 30) {{
                      lifetime = 8f;
                      status = OlStatusEffects.dalanied;
                      statusDuration = 60f * 5f;

                      shootEffect = new MultiEffect(
                        Fx.shootSmall,
                        new LumenLiquidEffect(
                          30f, Color.valueOf("3E6067"), Color.valueOf("5E929D"), Color.valueOf("8CDAEA")
                        ).layer(Layer.effect + 1)
                      );
                  }}
                };
				icons = new String[] {
					"status-wet", "status-tarred", "omaloon-dalanied"
				};
                bulletFilter = unit -> {
                    if ((Vars.state.rules.env & Env.groundWater) != 0) return bullets[0];
                    if ((Vars.state.rules.env & Env.groundOil) != 0) return bullets[1];
                    return bullets[2];
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
        centurion = new GlassmoreUnitType("centurion") {{
            constructor = MechUnit::create;
            speed = 0.4f;
            hitSize = 10f;
            health = 400;
            range = 80f;
            weapons.add(new Weapon("omaloon-centurion-weapon") {{
                top = false;

                x = 8.25f;
                y = 1.25f;
                reload = 60f;

                shoot = new ShootPattern() {{
                    shotDelay = 5f;
                    shots = 2;
                }};

                shootSound = Sounds.missile;
                bullet = new BasicBulletType(2f, 10, "missile") {{
                    lifetime = 40f;
                }};
            }});
        }};
        praetorian = new GlassmoreUnitType("praetorian") {{
            constructor = MechUnit::create;
            speed = 0.4f;
            hitSize = 16f;
            health = 1200;
            range = 80f;
            weapons.add(new Weapon("omaloon-praetorian-weapon") {{
                continuous = alwaysContinuous = true;
                top = alternate = false;

                x = 14.75f;
                y = 3.5f;
                shootX = -3f;
                shootY = 6f;

                bullet = new ContinuousFlameBulletType(5) {{
                    colors = new Color[] {Color.valueOf("BC5452"), Color.valueOf("FEB380")};

                    flareColor = Color.valueOf("FEB380");
                    flareInnerLenScl = 0f;
                    flareLength = 10f;
                    flareWidth = 2f;
                }};
            }});
        }};
        //endregion

        //region vegetable
        cilantro = new GlassmoreUnitType("cilantro") {{
            flying = lowAltitude = true;

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
        //TODO: better movement settings
        basil = new GlassmoreUnitType("basil") {{
            flying = true;
            lowAltitude = true;

            speed = 1.7f;
            accel = 0.04f;
            drag = 0.09f;
            engineOffset = 12f;
            rotateSpeed = 2f;
            trailLength = 20;

            constructor = UnitEntity::create;

            weapons.addAll(new Weapon() {{
                mirror = false;
                continuous = alwaysContinuous = true;

                x = 0f;
                y = -3f;
                shootSound = Sounds.torch;

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

                  bullet = new BasicBulletType();
              }},
              new Weapon("omaloon-sage-salvo") {{
                  reload = 60f;
                  x = 10.25f;
                  y = -8f;

                  bullet = new BasicBulletType();
              }}
            );
        }};
        //endregion
    }
}
