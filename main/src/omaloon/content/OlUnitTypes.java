package omaloon.content;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import mindustry.ai.types.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.entities.effect.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.type.unit.*;
import omaloon.ai.*;
import omaloon.annotations.Annotations.*;
import omaloon.entities.*;
import omaloon.entities.abilities.*;
import omaloon.entities.bullet.*;
import omaloon.entities.part.*;
import omaloon.gen.*;
import omaloon.type.*;

public class OlUnitTypes{
    // flying
    public static UnitType cilantro, basil, sage;

    // mech
    public static UnitType legionnaire, centurion, praetorian;

    // lumen
//    public static UnitType lumen;

    // ornitopter
    public static @EntityDef({Unitc.class, Ornithopterc.class}) UnitType effort;

    // millipede
    public static @EntityDef({Unitc.class, Mechc.class, Chainedc.class}) UnitType collector, collectorSegment, collectorTail;

    // core
    public static UnitType discovery;

    public static /*@EntityDef({Unitc.class, Corec.class, FloatMechc.class})*/ UnitType walker;

    public static @EntityDef({Unitc.class, DroneTetherc.class}) UnitType attackDroneAlpha, actionDroneMono;

    public static void load(){
        collector = new GlasmoreUnitType("collector"){
            {
                constructor = ChainedMechUnit::create;
//              segmentAI = u -> new ChainedAI();

                canHeal = true;
                omniMovement = faceTarget = false;

                speed = 0.6f;
                health = 200f;
                hitSize = 4;

                splittable = true;
                killSmallChains = true;
                segmentUnits = 3;

                alwaysCreateOutline = true;

                segmentUnit = collectorSegment = new GlasmoreUnitType("collector-segment"){{
                    constructor = ChainedMechUnit::create;
                    canHeal = true;
                    hidden = true;
                    omniMovement = faceTarget = false;
                    speed = 0.6f;
                    health = 200f;
                    hitSize = 4;
                    mechSideSway = 0.15f;
                    segmentRotationRange = 65f;
                    segmentSpacing = 6f;
                    segmentLayerOffset = -0.001f;
                    segmentUnits = 3;
                    splittable = true;
                    killSmallChains = true;

                    alwaysCreateOutline = true;
                    useUnitCap = false;

                    weapons.addAll(
                    new Weapon("omaloon-collector-launcher"){{
                        mirror = false;
                        rotate = true;
                        x = 0; y = 0.5f;

                        reload = 130f;
                        rotateSpeed = 2.5f;

                        layerOffset = 0.002f;
                    }}
                    );
                }};

                segmentEndUnit = collectorTail = new GlasmoreUnitType("collector-tail"){{
                    constructor = ChainedMechUnit::create;
                    hidden = true;
                    omniMovement = faceTarget = false;
                    physics = true;
                    speed = 0.6f;
                    health = 200f;
                    hitSize = 4;
                    mechSideSway = 0.15f;
                    segmentRotationRange = 65f;
                    segmentSpacing = 5.5f;
                    segmentLayerOffset = -0.001f;
                    segmentUnits = 3;
                    splittable = true;
                    killSmallChains = true;

                    alwaysCreateOutline = true;
                    useUnitCap = false;
                }};

                segmentRotationRange = 55f;
                segmentSpacing = 7f;

                segmentLayerOffset = -0.001f;

                mechSideSway = 0.15f;

                abilities.add(new ConnectChainAbility(){{
                    connectAngle = 65f;
                    maxConnections = 6;
                    pullStrength = 0.3f;
                    connectTime = 0f;

                    // TODO proper ui for this?
                    display = false;
                }});
            }

            @Override
            public void load(){
                super.load();
                if(region.found()) fullIcon = region;

                TextureRegion full = Core.atlas.find(name + "-full-icon");
                if(full.found()) uiIcon = full;
            }
        };

        //region core
        attackDroneAlpha = new GlasmoreUnitType("combat-drone-alpha"){{
            constructor = DroneTetherUnit::create;
            itemCapacity = 0;
            speed = 2.2f;
            accel = 0.08f;
            drag = 0.04f;
            health = 70;
            engineOffset = 4f;
            engineSize = 2;
            hitSize = 9;


            weapons.add(new Weapon(){{
                y = 0f;
                x = 1.5f;
                reload = 20f;
                ejectEffect = Fx.casing1;

                shootCone = 60f;

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
                shootSound = OlSounds.theShoot;
            }});
            shadowElevationScl = 0.4f;
        }};

        actionDroneMono = new GlasmoreUnitType("main-drone-mono"){{
            constructor = DroneTetherUnit::create;
            mineTier = 3;
            itemCapacity = 1;

            speed = 2.2f;
            accel = 0.08f;
            drag = 0.04f;
            health = 70;
            engineOffset = 4f;
            engineSize = 2;

            buildRange = 60f;
            buildSpeed = 1f;
            mineSpeed = 5.5f;
            mineRange = 40;

            hitSize = 9;

            shadowElevationScl = 0.4f;
        }};

        walker = new GlasmoreUnitType("walker"){{
            constructor = MechUnit::create;
            aiController = BuilderAI::new;

            buildRange = range = mineRange = 200f;
            buildSpeed = 1f;

            rotateToBuilding = faceTarget = false;

            speed = 0.5f;
            hitSize = 8f;
            health = 150;
            boostMultiplier = 0.8f;

            mineTier = 3;

            abilities.addAll(
                new DroneAbility(attackDroneAlpha){{
                    name = "omaloon-combat-drone";
//                    droneController = AttackDroneAI::new;
                    spawnTime = 180f;
                    spawnX = 5f;
                    spawnY = 0f;
                    spawnEffect = Fx.spawn;
                    parentizeEffects = true;
//                    anchorPos = new Vec2[]{
//                    new Vec2(12f, 0f),
//                    };
                }},
                new DroneAbility(actionDroneMono){{
                    name = "omaloon-utility-drone";
//                    droneController = UtilityDroneAI::new;
                    spawnTime = 180f;
                    spawnX = -5f;
                    spawnY = 0f;
                    spawnEffect = Fx.spawn;
                    parentizeEffects = true;
//                    anchorPos = new Vec2[]{
//                    new Vec2(-12f, 0f),
//                    };
                }}
            );

            shadowElevationScl = 0.3f;
        }};

        discovery = new GlasmoreUnitType("discovery"){{
            controller = u -> new BuilderAI(true, 500f);
            constructor = UnitEntity::create;
            isEnemy = false;

            lowAltitude = true;
            flying = true;
            mineSpeed = 4.5f;
            mineTier = 1;
            mineItems = Seq.with(OlItems.cobalt);
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

        effort = new GlasmoreUnitType("effort"){{
            constructor = OrnithopterUnit::create;
            aiController = () -> new CowardAI(){
                @Override
                public boolean retarget(){
                    return timer.get(timerTarget, 10);
                }
            };
            lowAltitude = true;
            speed = 2.7f;
            accel = 0.08f;
            engineSize = 0;
            drag = 0.04f;
            flying = true;
            health = 160;
            range = 140f;
            faceTarget = false;
            circleTarget = true;
            forceMultiTarget = true;
            rotateMoveFirst = true;
            rotateSpeed = 8f;
            fallDriftScl = 60f;
            fallSpeed = 0.01f;
            hitSize = 8;

            blades.addAll(
                new Blade(name + "-blade"){{
                    layerOffset = 0f;
                    x = 3f;
                    y = 1.5f;
                    bladeMaxMoveAngle = 35;
                    blurAlpha = 1f;
                }},
                new Blade(name + "-blade"){{
                    layerOffset = 0f;
                    x = 3f;
                    y = -1f;
                    bladeMaxMoveAngle = -35;
                    blurAlpha = 1f;
                }}
            );

            weapons.add(
                new Weapon(){{
                    x = 0; y = 4;
                    shootY = 0;
                    mirror = false;

                    ignoreRotation = true;
                    shootCone = 180f;
                    reload = 30;

                    controllable = true;
                    targetInterval = targetSwitchInterval = 0f;

                    bullet = new BulletType(1f, 10){{
                        lifetime = 2;
                        hitSize = 2;
                    }};
                }
                    @Override
                    public void update(Unit unit, WeaponMount mount) {
                        super.update(unit, mount);
                        if (!unit.dead) unit.elevation = 0.5f + (1f - mount.warmup) / 2f;
                    }
                }
            );
        }};

//        lumen = new GlassmoreUnitType("lumen"){{
//            constructor = UnitEntity::create;
//
//            hitSize = 10f;
//
//            speed = 1.7f;
//            accel = 0.08f;
//            drag = 0.04f;
//            rotateSpeed = 7f;
//
//            flying = true;
//            health = 70;
//
//            range = 0.1f;
//            targetAir = false;
//
//            deathSound = OlSounds.tankBang;
//
//            outlineRegion = atlas.find("omaloon-lumen-outline");
//            alwaysCreateOutline = true;
//
////            weapons.add(new FilterWeapon(){{
////                //TODO: shoot filter bullet on destroy / death
////                name = "omaloon-lumen-sprayer";
////                maxRange = 0.1f;
////                mirror = false;
////                x = 0f;
////                y = 0.25f;
////                rotate = false;
////                layerOffset = -0.01f;
////
////                shootSound = Sounds.none;
////                shootOnDeath = true;
////                shootX = shootY = 0f;
////                shoot = new ShootSpread(30, 1);
////                inaccuracy = 360f;
////                velocityRnd = 0.8f;
////                reload = 30f;
////                recoil = 0f;
////
////                shootCone = 15f;
////
////                bullets = new BulletType[]{
////                    new LiquidBulletType(OlLiquids.glacium){{
////                        //recoil = 0.06f;
////                        killShooter = true;
////
////                        speed = 5f;
////                        drag = 0.2f;
////
////                        shootEffect = Fx.shootSmall;
////
////                        lifetime = 17f;
////
////                        collidesAir = false;
////                        status = OlStatusEffects.glacied;
////                        statusDuration = 60f * 5f;
////
////                        puddleLiquid = OlLiquids.glacium;
////                        puddles = 5;
////                        puddleAmount = 80f;
////                        puddleSize = 8f;
////
////                        despawnHit = true;
////
////                        despawnSound = hitSound = Sounds.splash;
////                    }
////                        @Override
////                        public void init(Bullet b){
////                            if(killShooter && b.owner() instanceof Unit u && !u.dead()){
////                                u.elevation = 0;
////                                u.health = -1;
////                                u.dead = true;
////                                u.type().deathSound = OlSounds.tankBang;
////                                u.kill();
////                            }
////                        }
////                    },
////                    new LiquidBulletType(Liquids.water){{
////                        //recoil = 0.06f;
////                        killShooter = true;
////
////                        speed = 5f;
////                        drag = 0.2f;
////
////                        shootEffect = Fx.shootSmall;
////
////                        lifetime = 17f;
////
////                        collidesAir = false;
////                        status = StatusEffects.wet;
////                        statusDuration = 60f * 5f;
////
////                        puddleLiquid = Liquids.water;
////                        puddles = 5;
////                        puddleAmount = 80f;
////                        puddleSize = 8f;
////
////                        despawnHit = true;
////
////                        despawnSound = hitSound = Sounds.splash;
////                    }
////                        @Override
////                        public void init(Bullet b){
////                            if(killShooter && b.owner() instanceof Unit u && !u.dead()){
////                                u.elevation = 0;
////                                u.health = -1;
////                                u.dead = true;
////                                u.type().deathSound = OlSounds.tankBang;
////                                u.kill();
////                            }
////                        }
////                    },
////                    new LiquidBulletType(Liquids.slag){{
////                        //recoil = 0.06f;
////                        killShooter = true;
////
////                        speed = 5f;
////                        drag = 0.2f;
////
////                        shootEffect = Fx.shootSmall;
////
////                        lifetime = 17f;
////
////                        collidesAir = false;
////                        status = StatusEffects.melting;
////                        statusDuration = 60f * 5f;
////
////                        puddleLiquid = Liquids.slag;
////                        puddles = 5;
////                        puddleAmount = 80f;
////                        puddleSize = 8f;
////
////                        despawnHit = true;
////
////                        despawnSound = hitSound = Sounds.splash;
////                    }
////                        @Override
////                        public void init(Bullet b){
////                            if(killShooter && b.owner() instanceof Unit u && !u.dead()){
////                                u.elevation = 0;
////                                u.health = -1;
////                                u.dead = true;
////                                u.type().deathSound = OlSounds.tankBang;
////                                u.kill();
////                            }
////                        }
////                    },
////                    new LiquidBulletType(Liquids.oil){{
////                        //recoil = 0.06f;
////                        killShooter = true;
////
////                        speed = 5f;
////                        drag = 0.2f;
////
////                        shootEffect = Fx.shootSmall;
////
////                        lifetime = 17f;
////
////                        collidesAir = false;
////                        status = StatusEffects.tarred;
////                        statusDuration = 60f * 5f;
////
////                        puddleLiquid = Liquids.oil;
////                        puddles = 5;
////                        puddleAmount = 80f;
////                        puddleSize = 8f;
////
////                        despawnHit = true;
////
////                        despawnSound = hitSound = Sounds.splash;
////                    }
////                        @Override
////                        public void init(Bullet b){
////                            if(killShooter && b.owner() instanceof Unit u && !u.dead()){
////                                u.elevation = 0;
////                                u.health = -1;
////                                u.dead = true;
////                                u.type().deathSound = OlSounds.tankBang;
////                                u.kill();
////                            }
////                        }
////                    }
////                };
////                icons = new String[]{
////                    "omaloon-filled-with-glacium",
////                    "omaloon-filled-with-water",
////                    "omaloon-filled-with-slag",
////                    "omaloon-filled-with-oil"
////                };
////                tint = unit -> {
////                    if(!unit.dead() && unit.hasEffect(OlStatusEffects.filledWithGlacium)) return OlLiquids.glacium;
////                    if(!unit.dead() && unit.hasEffect(OlStatusEffects.filledWithWater)) return Liquids.water;
////                    if(!unit.dead() && unit.hasEffect(OlStatusEffects.filledWithSlag)) return Liquids.slag;
////                    if(!unit.dead() && unit.hasEffect(OlStatusEffects.filledWithOil)) return Liquids.oil;
////                    return null;
////                };
////                bulletFilter = unit -> {
////                    if(unit.hasEffect(OlStatusEffects.filledWithGlacium)) return bullets[0];
////                    if(unit.hasEffect(OlStatusEffects.filledWithWater)) return bullets[1];
////                    if(unit.hasEffect(OlStatusEffects.filledWithSlag)) return bullets[2];
////                    if(unit.hasEffect(OlStatusEffects.filledWithOil)) return bullets[3];
////                    return new BulletType(0, 0){{
////                        shootEffect = smokeEffect = hitEffect = despawnEffect = Fx.none;
////                    }};
////                };
////            }});
//            weapons.add(new Weapon(){{
//                bullet = new BulletType(){{
//                    killShooter = instantDisappear = true;
//                    hitEffect = shootEffect = smokeEffect = despawnEffect = Fx.none;
//                }
//                    @Override
//                    public void init(Bullet b) {
//                        super.init(b);
//                        if (b.owner() instanceof Unit u) u.elevation = 0;
//                    }
//                };
//            }});
//
//            abilities.add(
//                new TankAbility(OlStatusEffects.filledWithGlacium, new BulletType(){{
//                    killShooter = instantDisappear = true;
//                    hitEffect = shootEffect = smokeEffect = despawnEffect = Fx.none;
//
//                    fragBullets = 30;
//                    fragBullet = new LiquidBulletType(OlLiquids.glacium){{
//                        //recoil = 0.06f;
//                        killShooter = true;
//
//                        speed = 5f;
//                        drag = 0.2f;
//
//                        shootEffect = Fx.shootSmall;
//
//                        lifetime = 17f;
//
//                        collidesAir = false;
//                        status = OlStatusEffects.glacied;
//                        statusDuration = 60f * 5f;
//
//                        puddleLiquid = OlLiquids.glacium;
//                        puddles = 5;
//                        puddleAmount = 80f;
//                        puddleSize = 8f;
//
//                        despawnHit = true;
//
//                        despawnSound = hitSound = Sounds.splash;
//                    }};
//                }}),
//                new TankAbility(OlStatusEffects.filledWithWater, new BulletType(){{
//                    killShooter = instantDisappear = true;
//                    hitEffect = shootEffect = smokeEffect = despawnEffect = Fx.none;
//
//                    fragBullets = 30;
//                    fragBullet = new LiquidBulletType(Liquids.water){{
//                        //recoil = 0.06f;
//                        killShooter = true;
//
//                        speed = 5f;
//                        drag = 0.2f;
//
//                        shootEffect = Fx.shootSmall;
//
//                        lifetime = 17f;
//
//                        collidesAir = false;
//                        status = StatusEffects.wet;
//                        statusDuration = 60f * 5f;
//
//                        puddleLiquid = Liquids.water;
//                        puddles = 5;
//                        puddleAmount = 80f;
//                        puddleSize = 8f;
//
//                        despawnHit = true;
//
//                        despawnSound = hitSound = Sounds.splash;
//                    }};
//                }}),
//                new TankAbility(OlStatusEffects.filledWithSlag, new BulletType(){{
//                    killShooter = instantDisappear = true;
//                    hitEffect = shootEffect = smokeEffect = despawnEffect = Fx.none;
//
//                    fragBullets = 30;
//                    fragBullet = new LiquidBulletType(Liquids.slag){{
//                        //recoil = 0.06f;
//                        killShooter = true;
//
//                        speed = 5f;
//                        drag = 0.2f;
//
//                        shootEffect = Fx.shootSmall;
//
//                        lifetime = 17f;
//
//                        collidesAir = false;
//                        status = StatusEffects.melting;
//                        statusDuration = 60f * 5f;
//
//                        puddleLiquid = Liquids.slag;
//                        puddles = 5;
//                        puddleAmount = 80f;
//                        puddleSize = 8f;
//
//                        despawnHit = true;
//
//                        despawnSound = hitSound = Sounds.splash;
//                    }};
//                }}),
//                new TankAbility(OlStatusEffects.filledWithOil, new BulletType(){{
//                    killShooter = instantDisappear = true;
//                    hitEffect = shootEffect = smokeEffect = despawnEffect = Fx.none;
//
//                    fragBullets = 30;
//                    fragBullet = new LiquidBulletType(Liquids.oil){{
//                        //recoil = 0.06f;
//                        killShooter = true;
//
//                        speed = 5f;
//                        drag = 0.2f;
//
//                        shootEffect = Fx.shootSmall;
//
//                        lifetime = 17f;
//
//                        collidesAir = false;
//                        status = StatusEffects.tarred;
//                        statusDuration = 60f * 5f;
//
//                        puddleLiquid = Liquids.oil;
//                        puddles = 5;
//                        puddleAmount = 80f;
//                        puddleSize = 8f;
//
//                        despawnHit = true;
//
//                        despawnSound = hitSound = Sounds.splash;
//                    }};
//                }})
//            );
//        }};

        //region roman
        legionnaire = new GlasmoreUnitType("legionnaire"){{
            constructor = MechUnit::create;
            speed = 0.5f;
            hitSize = 8f;
            health = 150;

            alwaysCreateOutline = true;

            weapons.add(new Weapon("omaloon-legionnaire-weapon"){{
                shootSound = OlSounds.theShoot;
                top = false;

                layerOffset = -0.001f;
                reload = 35f;
                x = 4.7f;
                y = 0.4f;

                shootCone = 45f;

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

        centurion = new GlasmoreUnitType("centurion"){{
            constructor = MechUnit::create;
            speed = 0.4f;
            hitSize = 9f;
            health = 250;
            range = 50;

            alwaysCreateOutline = true;

            weapons.add(new Weapon("omaloon-centurion-weapon"){{
                shootSound = OlSounds.theShoot;
                mirror = true;
                top = false;

                layerOffset = -0.001f;
                reload = 35f;
                x = 5.75f;
                y = 0.27f;
                shootX = -0.5f;
                shootY = 5.5f;
                recoil = 1.3f;
                inaccuracy = 25;

                shoot.shots = 4;
                shoot.shotDelay = 0.2f;
                velocityRnd = 0.5f;

                shootCone = 45f;

                ejectEffect = Fx.casing1;
                bullet = new BasicBulletType(5.5f, 5){{
                    width = 4f;
                    height = 4f;
                    lifetime = 12f;

                    maxRange = 50;

                    despawnEffect = Fx.hitBulletSmall;
                    hitEffect = Fx.none;
                    hitColor = backColor = trailColor = Color.valueOf("feb380");

                    trailWidth = 0.8f;
                    trailLength = 10;
                }};
            }});
        }};

        praetorian = new GlasmoreUnitType("praetorian"){
            {
                constructor = MechUnit::create;
                speed = 0.3f;
                hitSize = 13f;
                rotateSpeed = 2f;
                health = 400;
                range = 200f;

                targetAir = false;

                alwaysCreateOutline = true;

                Weapon missile;
                weapons.add(missile = new Weapon(""){{
                    mirror = false;

                    x = 7.25f;
                    y = 0f;

                    rotate = true;
                    rotateSpeed = 7f;
                    rotationLimit = 30;

                    reload = 140f;

                    shootY = 0;
                    shootCone = 10f;

                    shake = 1f;

                    recoil = 0f;

                    shootSound = Sounds.shootMissileLarge;
                    bullet = new BulletType(){{
                        keepVelocity = false;
                        collidesAir = false;
                        spawnUnit = new MissileUnitType("praetorian-missile"){{
                            targetAir = false;
                            speed = 4f;
                            lifetime = 50f;
                            drawCell = false;
                            outlineColor = Color.valueOf("2f2f36");

                            missileAccelTime = 1f;
                            accel = drag = 0.1f;
                            rotateSpeed = 1f;

                            weapons.add(new Weapon(){{
                                shootCone = 360f;
                                mirror = false;
                                reload = 1f;
                                shootOnDeath = true;
                                bullet = new ExplosionBulletType(100f, 32f){{
                                    shootEffect = Fx.massiveExplosion;
                                    collidesAir = false;
                                }};
                            }});
                        }};

                        shootEffect = new Effect(10f, e -> {
                            Tmp.v1.trns(e.rotation + 180f, 4f).add(e.x, e.y);

                            Draw.color(Pal.lighterOrange, Pal.lightOrange, e.fin());
                            float w = 1 + 5 * e.fout();
                            Drawf.tri(Tmp.v1.x, Tmp.v1.y, w, 15f * e.fout(), e.rotation + 180);
                            Drawf.tri(Tmp.v1.x, Tmp.v1.y, w, 3f * e.fout(), e.rotation);

                        }).followParent(false);

                        smokeEffect = new Effect(20, e -> {
                            Tmp.v1.trns(e.rotation + 180f, 4f).add(e.x, e.y);

                            Draw.color(Pal.lighterOrange);

                            Angles.randLenVectors(e.id, 10, e.finpow() * 32f, e.rotation + 180f, 10f, (x, y) -> {
                                Fill.circle(Tmp.v1.x + x, Tmp.v1.y + y, e.fout() * 1.5f);
                            });

                        }).followParent(false);
                    }};
                }});
                var copy = missile.copy();
                copy.flip();
                weapons.add(copy);
                missile.recoilTime *= 2f;
                missile.reload *= 2f;
                copy.recoilTime *= 2f;
                copy.reload *= 2f;
                missile.otherSide = 1;
                copy.otherSide = 0;
                for(int i : Mathf.signs){
                    weapons.get(i == 1 ? 0 : 1).parts = Seq.with(new ConstructPart(){{
                        name = "omaloon-praetorian-missile";

                        sclX = i;
                        layerOffset = -0.01f;

                        progress = PartProgress.reload.inv();
                    }});
                }
            }

            // felt like this doesn't need to be in GlasmoreUnitType
            @Override
            public void init(){
                super.init();
                weapons.each(w -> {
                    if(w.otherSide != -1 && !w.mirror) w.mirror = true;
                });
            }
        };
        //endregion

        //region vegetable
        cilantro = new GlasmoreUnitType("cilantro"){{
            flying = lowAltitude = true;
            health = 160;
            hitSize = 8f;

            accel = 0.05f;
            drag = 0.03f;
            rotateSpeed = 10f;
            trailLength = 10;

            constructor = UnitEntity::create;

            weapons.addAll(new Weapon(){{
                mirror = false;

                x = 0;
                y = 1;

                reload = 30;
                shoot.firstShotDelay = 60f;

                shootCone = 45f;

                shootSound = Sounds.shootAlpha;
                bullet = new BasicBulletType(2f, 6, "omaloon-triangle-bullet"){{
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

        basil = new GlasmoreUnitType("basil"){{
            flying = lowAltitude = true;
            health = 280;
            hitSize = 20f;

            drag = 0.09f;
            speed = 1.8f;
            rotateSpeed = 2.5f;
            accel = 0.05f;

            engineOffset = 12f;
            setEnginesMirror(new UnitEngine(5, -10f, 2, -45));

            constructor = UnitEntity::create;

            weapons.addAll(new Weapon(){{
                mirror = false;
                continuous = alwaysContinuous = true;

                x = 0f;
                y = -3f;
                shootSound = Sounds.loopSmelter;

                bullet = new ContinuousFlameBulletType(5){{
                    colors = new Color[]{Color.valueOf("8CA9E8"), Color.valueOf("8CA9E8"), Color.valueOf("D1EFFF")};

                    lifetime = 60f;

                    shootCone = 360f;

                    width = 2.5f;
                    length = 75f;
                    lengthInterp = a -> Interp.smoother.apply(Mathf.slope(a));
                    flareLength = 20f;
                    flareInnerLenScl = flareRotSpeed = 0f;
                    pierceCap = 1;
                    flareColor = Color.valueOf("D1EFFF");

                    hitEffect = new ParticleEffect(){{
                        lifetime = 30f;
                        length = 20f;

                        interp = Interp.pow2Out;

                        colorFrom = Color.valueOf("D1EFFF");
                        colorTo = Color.valueOf("8CA9E8");
                    }};
                }};
            }});
        }};

        sage = new GlasmoreUnitType("sage"){{
            constructor = UnitEntity::create;
            flying = lowAltitude = true;
            health = 850;
            hitSize = 30f;

            speed = 0.8f;
            accel = 0.04f;
            drag = 0.04f;
            rotateSpeed = 1.9f;

            constructor = UnitEntity::create;

            engineOffset = 16f;
            engineSize = 4f;

            range = maxRange = 180f;

            weapons.addAll(
            new Weapon("omaloon-sage-cannon"){{
                mirror = false;
                rotate = true;

                x = 0f;
                y = -9f;

                reload = 120f;
                rotateSpeed = 2f;

                shootSound = Sounds.explosionAfflict;
                bullet = new ArtilleryBulletType(4.5f, 20, "missile"){
                    {
                        lifetime = 40f;
                        homingPower = 0.05f;
                        homingRange = 100f;

                        splashDamage = 20f;
                        splashDamageRadius = 32f;

                        width = height = 12f;
                        shrinkY = 0f;

                        trailWidth = 3f;
                        trailLength = 20;

                        frontColor = Color.valueOf("D1EFFF");
                        backColor = hitColor = trailColor = Color.valueOf("8CA9E8");

                        hitEffect = new MultiEffect(OlFx.hitSage, new WrapEffect(OlFx.lightPillar, backColor, splashDamageRadius), new WrapEffect(OlFx.sageFire, backColor, splashDamageRadius));
                        despawnEffect = new WrapEffect(Fx.dynamicWave, backColor, splashDamageRadius);
                        hitSound = Sounds.blockExplodeFlammable;

                        fragBullets = 1;
                        fragBullet = new LingeringBulletType(5, 32f){{
                            collidesAir = false;

                            lifetime = 180f;

                            activeSound = Sounds.loopFire;

                            trailColor = Color.valueOf("8CA9E8");
                            trailInterval = 999f;
                            despawnEffect = hitEffect = Fx.none;
                        }};
                    }

                    //TODO: maybe it's time for a custom bulletType?
                    @Override
                    public void draw(Bullet b){
                        super.draw(b);
                        drawTrail(b);
                        int sides = 4;
                        float radius = 0f, radiusTo = 15f, innerRadScl = 0.33f,
                        stroke = 5f, innerScl = 0.5f,
                        offsetX = -5f, offsetY = 0f;
                        Color color1 = Color.valueOf("8ca9e8"), color2 = Color.valueOf("d1efff");
                        float progress = b.fslope();
                        float rotation = 45f;
                        float layer = Layer.effect;

                        float z = Draw.z();
                        Draw.z(layer);

                        Tmp.v1.trns(b.rotation(), offsetX, offsetY).add(b.x, b.y);
                        float rx = Tmp.v1.x, ry = Tmp.v1.y, rad = Mathf.lerp(radius, radiusTo, progress);

                        Draw.color(color1);
                        for(int j = 0; j < sides; j++){
                            Drawf.tri(rx, ry, stroke, rad, j * 360f / sides + rotation);
                        }

                        Draw.color(color2);
                        for(int j = 0; j < sides; j++){
                            Drawf.tri(rx, ry, stroke * innerScl, rad * innerRadScl, j * 360f / sides + rotation);
                        }

                        Draw.color();
                        Draw.z(z);
                    }
                };
            }},
            new Weapon("omaloon-sage-weapon"){{
                x = 8.75f;
                y = -4.5f;

                reload = 55f;
                recoilTime = 15f;

                rotate = true;
                rotateSpeed = 4f;

                shoot.shots = 4;
                shoot.shotDelay = 5;

                shootSound = Sounds.shootLaser;
                bullet = new LaserBoltBulletType(3.5f, 18f){{
                    width = 2f;
                    height = 10f;
                    lifetime = 52f;

                    hitColor = backColor = Color.valueOf("8ca9e8");
                    frontColor = Color.valueOf("d1efff");

                    trailEffect = OlFx.sageWeaponTrail;
                    trailInterval = 2.5f;
                    trailRotation = true;

                    shootEffect = OlFx.sageWeaponShoot;
                    smokeEffect = Fx.none;

                    hitEffect = OlFx.sageWeaponHit;
                    despawnEffect = OlFx.sageWeaponHit;

                    status = StatusEffects.shocked;
                    statusDuration = 10f;
                }};
            }}
            );
        }};
    }
    //endregion
}