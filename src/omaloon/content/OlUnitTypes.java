package omaloon.content;

import arc.graphics.*;
import arc.struct.*;
import ent.anno.Annotations.*;
import mindustry.ai.types.*;
import mindustry.content.*;
import mindustry.entities.bullet.*;
import mindustry.entities.part.RegionPart;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import omaloon.gen.*;
import omaloon.gen.MechUnit;
import omaloon.type.*;

public class OlUnitTypes{
    //core
    public static @EntityDef({Unitc.class, Flyingc.class}) UnitType discovery;
    //ornitopter
    public static @EntityDef({Unitc.class, Flyingc.class, Ornitopterc.class}) UnitType effort;
    //mech
    public static @EntityDef({Unitc.class, Mechc.class}) UnitType legionnaire;
    //millipede
    public static @EntityDef({Unitc.class, Millipedec.class, Legsc.class}) UnitType collector;

    public static void load(){
        discovery = new GlassmoreUnitType("discovery"){{
            controller = u -> new BuilderAI(true, 500f);
            constructor = UnitEntity::create;
            isEnemy = false;

            lowAltitude = true;
            flying = true;
            mineSpeed = 4.5f;
            mineTier = 3;
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
                layerOffset = -0.01f;
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
