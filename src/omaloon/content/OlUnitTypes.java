package omaloon.content;

import arc.graphics.Color;
import arc.struct.*;
import ent.anno.Annotations.*;
import mindustry.ai.types.*;
import mindustry.content.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.gen.MechUnit;
import mindustry.graphics.*;
import mindustry.type.*;
import omaloon.gen.*;
import omaloon.type.*;

public class OlUnitTypes{
    public static @EntityDef({Unitc.class, Flyingc.class}) UnitType discovery;
    public static @EntityDef({Unitc.class, Mechc.class}) UnitType legionnaire;
    public static @EntityDef({Unitc.class, Millipedec.class, Legsc.class}) UnitType collector;

    public static void load(){
        discovery = new GlasmoreUnitType("discovery"){{
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

        legionnaire = new GlasmoreUnitType("legionnaire"){{
            constructor = MechUnit::create;
            speed = 0.4f;
            hitSize = 9f;
            health = 180;
            range = 100;
            weapons.add(new Weapon(){{
                reload = 35f;
                shoot.shots = 3;
                shoot.shotDelay = 4f;
                shootSound = OlSounds.theShoot;
                x = 4.5f;
                y = 3f;
                top = false;
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

                    fragOnHit = true;
                    fragBullets = 2;
                    fragRandomSpread = 25f;
                    fragVelocityMin = 0.7f;
                }};
            }});
        }};

        collector = new GlasmoreUnitType("collector"){{
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

            segWeapSeq.add(new Weapon("collector-beam"){{
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
