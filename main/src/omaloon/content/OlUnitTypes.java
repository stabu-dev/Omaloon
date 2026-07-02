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
import mindustry.entities.part.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.type.unit.*;
import mindustry.world.meta.*;
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
    public static UnitType lumen;

    // wheel
    public static UnitType splinter;

    // ornitopter
    public static @EntityDef({Unitc.class, Ornithopterc.class}) UnitType effort;

    // millipede
    public static @EntityDef({Unitc.class, Mechc.class, Chainedc.class}) UnitType collector, collectorSegment, collectorTail;

    // core
    public static UnitType discovery;

    public static @EntityDef({Unitc.class, FloatMechc.class, MockBuilderc.class}) UnitType walker;

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

                    weapons.addAll(new Weapon("omaloon-collector-launcher"){{
                        mirror = false;
                        rotate = true;
                        x = 0;
                        y = 0.5f;

                        reload = 330f;
                        rotateSpeed = 2.5f;

                        layerOffset = 0.002f;

                        bullet = new ArtilleryBulletType(2f, 7){{
                            lifetime = 80f;
                            maxRange = 40f;
                            collidesTiles = collidesAir = collidesGround = true;
                            width = height = 11f;
                            splashDamage = 25f;
                            splashDamageRadius = 25f;
                            trailColor = hitColor = lightColor = backColor = Pal.heal;
                            frontColor = Pal.heal;
                            hitSound = Sounds.healWave;

                            shootEffect = OlFx.collectorShoot;

                            hitEffect = new MultiEffect(
                            new WrapEffect(new Effect(300, OlFx.lightPillar::render), Pal.heal, 16f),
                            new WrapEffect(OlFx.collectorHit, Pal.heal, 16f),
                            new WrapEffect(OlFx.collectorWaves, Pal.heal, 16f)
                            );

                            fragBullets = 1;
                            fragBullet = new LingeringBulletType(1, 16f){{
                                lifetime = 300f;

                                healAmount = 1f;

                                activeSound = OlSounds.loopShelter;
                                activeSoundVolume = 0.04f;
                                despawnEffect = hitEffect = Fx.none;
                            }};
                        }};
                    }});
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
            controller = u -> new AttackDroneAI();
            constructor = DroneTetherUnit::create;
            logicControllable = playerControllable = false;
            hidden = true;
            isEnemy = false;

            itemCapacity = 0;
            speed = 2.2f;
            accel = 0.08f;
            drag = 0.04f;
            health = 70;
            engineOffset = 4f;
            engineSize = 2;
            hitSize = 9;
            range = maxRange = 80;
            flying = true;

            shadowElevationScl = 0.4f;

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
        }};

        actionDroneMono = new GlasmoreUnitType("main-drone-mono"){{
            controller = u -> new ActionDroneAI();
            constructor = DroneTetherUnit::create;
            logicControllable = playerControllable = false;
            hidden = true;
            isEnemy = false;

            itemCapacity = 1;
            speed = 2.2f;
            accel = 0.08f;
            drag = 0.04f;
            health = 70;
            engineOffset = 4f;
            engineSize = 2;
            hitSize = 9;
            flying = true;

            buildRange = 60f;
            buildSpeed = 1f;
            mineSpeed = 5.5f;
            mineRange = 40;
            mineTier = 3;

            shadowElevationScl = 0.4f;
        }};

        walker = new GlasmoreUnitType("walker"){{
            constructor = MockBuilderFloatMechUnit::create;
            aiController = BuilderAI::new;

            buildRange = range = mineRange = 200f;
            buildSpeed = 1f;

            rotateToBuilding = faceTarget = false;
            drawMineBeam = false;

            speed = 0.7f;
            hitSize = 8f;
            health = 150;
            boostMultiplier = 0.8f;

            mineTier = 3;

            abilities.addAll(
            new DroneAbility(attackDroneAlpha){{
                name = "omaloon-combat-drone";
//              droneController = AttackDroneAI::new;
                spawnTime = 180f;
                spawnX = 5f;
                spawnY = 0f;
                idleX = 10f;
                idleY = 0f;
                spawnEffect = Fx.spawn;
                parentizeEffects = true;
//              anchorPos = new Vec2[]{
//              new Vec2(12f, 0f),
//              };
            }},
            new DroneAbility(actionDroneMono){{
                name = "omaloon-utility-drone";
//              droneController = UtilityDroneAI::new;
                spawnTime = 180f;
                spawnX = -5f;
                spawnY = 0f;
                idleX = -10f;
                idleY = 0f;
                spawnEffect = Fx.spawn;
                parentizeEffects = true;
//              anchorPos = new Vec2[]{
//              new Vec2(-12f, 0f),
//              };
            }}
            );

            // hidden weapon that can't shoot, but thinks it can so that the unit thinks it can shoot so that the drone thinks it can shoot so that the drone moves to the target so that the drone shoots.
            weapons.add(new Weapon(){{
                mirror = false;
                display = false;
                minWarmup = 2f;
                bullet = new BulletType(){{
                    rangeOverride = 25 * 8f;
                }};
            }});

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
            health = 220;
            flying = true;
            hitSize = 8;
            engineSize = 0;
            speed = 2.7f;
            accel = 0.08f;
            drag = 0.04f;
            rotateMoveFirst = true;
            rotateSpeed = 8f;
            fallSpeed = 0.01f;
            fallDriftScl = 60f;

            range = 1f;
            targetAir = false;
            targetFlags = new BlockFlag[]{BlockFlag.repair, BlockFlag.generator, BlockFlag.turret, null};
            faceTarget = false;
            circleTarget = true;
            forceMultiTarget = true;

            loopSound = moveSound = OlSounds.loopBuzz;
            moveSoundPitchMin = 0.3f;
            moveSoundPitchMax = 1.2f;
            moveSoundVolume = 0.3f;

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
            new Weapon(){
                {
                    x = 0;
                    y = 4;
                    shootY = 0;
                    minShootVelocity = 2f;
                    shootCone = 180f;
                    reload = 0.2f;

                    ignoreRotation = false;

                    targetInterval = targetSwitchInterval = 0f;

                    shootSound = Sounds.none;

                    mirror = false;

                    bullet = new BulletType(1f, 0.7f){{
                        rangeOverride = 100f;
                        inaccuracy = 0;
                        lifetime = 2;
                        hitSize = 1;

                        shootEffect = smokeEffect = despawnEffect = Fx.none;
                        hitEffect = OlFx.scratchMarks;
                        hitSound = OlSounds.scratch;
                        hitSoundVolume = 0.4f;
                    }};
                }

                @Override
                public void update(Unit unit, WeaponMount mount){
                    super.update(unit, mount);
                    if(!unit.dead) unit.elevation = Math.max(0.1f, 1f - mount.warmup);
                }
            }
            );
        }};

        lumen = new GlasmoreUnitType("lumen"){{
            constructor = UnitEntity::create;

            hitSize = 6f;

            speed = 1.7f;
            accel = 0.08f;
            drag = 0.04f;
            rotateSpeed = 7f;

            flying = true;
            health = 70;

            range = 0.1f;
            targetAir = outlines = faceTarget = false;

            weapons.add(new Weapon(){{
                mirror = false;
                bullet = new BulletType(){
                    {
                        shootCone = 360f;
                        shootSound = Sounds.none;
                        killShooter = instantDisappear = shootOnDeath = true;
                        shootEffect = smokeEffect = despawnEffect = Fx.none;
                        hitEffect = Fx.none;
                        rangeOverride = 12f;
                    }

                    @Override
                    public void init(Bullet b){
                        if(killShooter && b.owner() instanceof Unit u && !u.dead()){
                            u.elevation = 0;
                            u.health = -1;
                            u.dead = true;
                            u.type().deathSound = OlSounds.tankBang;
                            u.kill();
                        }
                    }
                };
            }});

            deathExplosionEffect = new MultiEffect(
            Fx.dynamicExplosion,
            OlFx.lumenCarcass
            );

            parts.add(new RegionPart("-sprayer"){{
                outline = false;
                layerOffset = -0.002f;
            }});

            StatusEffect[] statusEffects = {OlStatusEffects.filledWithGlacium, OlStatusEffects.filledWithWater, OlStatusEffects.filledWithSlag, OlStatusEffects.filledWithOil};
            Liquid[] liquids = {OlLiquids.glacium, Liquids.water, Liquids.slag, Liquids.oil};
            float[] damages = {15f, 10f, 25f, 12f};

            for(int k = 0; k < statusEffects.length; k++){
                StatusEffect status = statusEffects[k];
                Liquid liq = liquids[k];
                float dmg = damages[k];

                abilities.add(new TankAbility(status, new BulletType(){{
                    instantDisappear = true;
                    shootEffect = smokeEffect = despawnEffect = Fx.none;
                    splashDamage = dmg;
                    splashDamageRadius = 20f;
                    hitEffect = new WrapEffect(OlFx.lumenSplash, liq.color, splashDamageRadius);

                    if(liq == Liquids.oil){
                        incendAmount = 1;
                        incendSpread = 8f;
                        incendChance = 1f;
                    }

                    fragBullets = 30;
                    fragLifeMin = 0.5f;
                    fragLifeMax = 1.5f;
                    fragBullet = new LiquidBulletType(liq){{
                        speed = 5f;
                        drag = 0.2f;
                        lifetime = 17f;
                        collidesAir = false;
                        statusDuration = 60f * 5f;
                        puddleLiquid = liq;
                        puddles = 5;
                        puddleAmount = 100f;
                        puddleSize = 8f;
                        despawnHit = true;
                        despawnSound = hitSound = Sounds.stepWater;
                    }};
                }}));
            }
        }};

        //region tank
        splinter = new GlasmoreUnitType("splinter"){{
            constructor = TankUnit::create;
            hitSize = 6f;
            speed = 1f;
            omniMovement = false;
        }};

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
                        spawnUnit = new MissileUnitType("praetorian-missile"){
                            {
                                targetAir = false;
                                speed = 4f;
                                lifetime = 50f;
                                health = 250f;
                                drawCell = false;
                                outlineColor = Color.valueOf("2f2f36");
                                useEngineElevation = false;
                                deathSound = Sounds.explosionPlasmaSmall;

                                missileAccelTime = 1f;
                                accel = drag = 0.1f;
                                rotateSpeed = 1f;

                                weapons.add(new Weapon(){{
                                    shootCone = 360f;
                                    mirror = false;
                                    reload = 1f;
                                    shootOnDeath = true;
                                    bullet = new ExplosionBulletType(100f, 32f){{
                                        shootEffect = OlFx.praetorianMissileExplosion;
                                        collidesAir = false;
                                    }};
                                }});
                            }

                            @Override
                            public void update(Unit unit){
                                super.update(unit);
                                if(unit instanceof TimedKillc t){
                                    unit.elevation = Mathf.slope(t.fin());
                                }
                            }
                        };

                        shootEffect = OlFx.praetorianMissileLaunch;
                        smokeEffect = Fx.none;
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
                    int wIndex = i == 1 ? 0 : 1;
                    
                    ConstructPart outerPart = new ConstructPart(){{
                        name = "omaloon-praetorian-missile";
                        sclX = i;
                        layerOffset = -0.009f;
                        weaponIndex = wIndex;
                        moveX = -2.5f;
                        moveY = 0f;
                        progress = PartProgress.reload.inv().compress(0f, 0.5f);
                        buildProgress = PartProgress.constant(1.0f);
                    }};
                    
                    ConstructPart innerPart = new ConstructPart(){{
                        name = "omaloon-praetorian-missile";
                        sclX = i;
                        layerOffset = -0.01f;
                        weaponIndex = wIndex;
                        moveX = -2.5f;
                        moveY = 0f;
                        progress = PartProgress.constant(0f);
                        buildProgress = PartProgress.reload.inv().compress(0.5f, 1.0f);
                    }};

                    weapons.get(wIndex).parts = Seq.with(outerPart, innerPart);
                }
            }

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
                mirror = rotate = false;

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

                bullet = new ContinuousFlameBulletType(5){
                    {
                        colors = new Color[]{Color.valueOf("8CA9E8"), Color.valueOf("8CA9E8"), Color.valueOf("D1EFFF")};

                        lifetime = 60f;

                        shootCone = 360f;

                        width = 2.5f;
                        length = 75f;
                        lengthInterp = a -> a < 0.5f ? Interp.pow2Out.apply(a * 2f) : 1f;
                        flareLength = 20f;
                        flareInnerLenScl = flareRotSpeed = 0f;
                        pierceCap = 1;
                        flareColor = Color.valueOf("D1EFFF");
                        drawFlare = false;
                        flareLayer = Layer.bullet + 0.002f;

                        shootEffect = smokeEffect = despawnEffect = Fx.none;

                        hitEffect = new ParticleEffect(){{
                            lifetime = 30f;
                            length = 20f;

                            interp = Interp.pow2Out;

                            colorFrom = Color.valueOf("D1EFFF");
                            colorTo = Color.valueOf("8CA9E8");
                        }};
                    }

                    @Override
                    public void update(Bullet b){
                        super.update(b);

                        float mult = b.fin(lengthInterp);
                        float realLength = Damage.findLength(b, length * mult, laserAbsorb, pierceCap);

                        if(Mathf.chance(0.3f)){
                            float dist = Mathf.random(realLength);
                            Tmp.v1.trns(b.rotation(), dist);
                            OlFx.sparkTrail.at(b.x + Tmp.v1.x, b.y + Tmp.v1.y, b.rotation());
                        }
                    }

                    @Override
                    public void draw(Bullet b){
                        float timeFade = b.fin() < 0.5f ? b.fin() / 0.5f : 1f - (b.fin() - 0.5f) / 0.5f;
                        for(Color c : colors) c.a = timeFade;
                        flareColor.a = timeFade;

                        super.draw(b);

                        OlFx.helixBeam.render(b.id, flareColor, b.time, b.lifetime, b.rotation(), b.x, b.y, b);

                        Draw.color(flareColor);
                        float z = Draw.z();
                        Draw.z(flareLayer);

                        float len = flareLength * (Mathf.slope(b.fin()) + Mathf.sin(Time.time, oscScl, oscMag));
                        for(int i = 0; i < 4; i++){
                            Drawf.tri(b.x, b.y, flareWidth, len, i * 90 + 45);
                        }
                        Draw.z(z);
                    }
                };
            }});
        }};

        sage = new GlasmoreUnitType("sage"){{
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

                        shootEffect = new MultiEffect(OlFx.sageCannonShoot, OlFx.sageWeaponShoot);
                        hitEffect = despawnEffect = new MultiEffect(
                        OlFx.hitSage,
                        new WrapEffect(OlFx.lightPillar, backColor, splashDamageRadius),
                        new WrapEffect(OlFx.sageFire, backColor, splashDamageRadius),
                        new WrapEffect(OlFx.sageStar, backColor, splashDamageRadius),
                        new WrapEffect(OlFx.sageShockWave, backColor, splashDamageRadius)
                        );
                        hitSound = Sounds.blockExplodeFlammable;

                        fragBullets = 1;
                        fragBullet = new LingeringBulletType(5, 32f){{
                            collidesAir = false;

                            lifetime = 180f;

                            activeSound = Sounds.loopFire;

                            trailColor = Pal.heal;
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

                    trailEffect = OlFx.sparkTrail;
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
        //endregion
    }
}