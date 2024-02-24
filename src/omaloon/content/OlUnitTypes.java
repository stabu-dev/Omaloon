package omaloon.content;

import arc.struct.*;
import ent.anno.Annotations.*;
import mindustry.ai.types.*;
import mindustry.content.*;
import mindustry.entities.bullet.ArtilleryBulletType;
import mindustry.entities.bullet.SapBulletType;
import mindustry.gen.*;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.type.*;
import omaloon.ai.MillipedeAI;
import omaloon.gen.MillipedeUnit;
import omaloon.gen.Millipedec;
import omaloon.type.*;

public class OlUnitTypes{
    public static UnitType discovery;
    public static @EntityDef({Unitc.class, Millipedec.class}) UnitType collector;

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

        collector = new GlasmoreUnitType("collector"){{
            aiController = MillipedeAI::new;
            constructor = MillipedeUnit::create;
            health = 200f;
            regenTime = 15f * 60f;
            splittable = true;
            circleTarget = true;
            omniMovement = false;
            angleLimit = 65f;
            segmentLength = 5;
            segmentDamageScl = 8f;
            segmentCast = 8;
            segmentOffset = 7;
            engineSize = -1f;
            maxSegments = 5;
            preventDrifting = true;

            legCount = 1;
            legLength = 8f;
            lockLegBase = true;
            legContinuousMove = true;
            legExtension = -2f;
            legBaseOffset = 3f;
            legMaxLength = 1.1f;
            legMinLength = 0.2f;
            legLengthScl = 0.96f;
            legForwardScl = 1.1f;
            legGroupSize = 3;
            rippleScale = 0.2f;

            legMoveSpace = 1f;
            allowLegStep = true;
            hovering = true;
            legPhysicsLayer = false;


            weapons.add(new Weapon(){{
                x = 0f;
                rotate = false;
                mirror = false;
                reload = 70f;
                //shots = 12;
                shootCone = 90f;
                inaccuracy = 35f;
                xRand = 2f;
                //shotDelay = 0.5f;
                bullet = new SapBulletType(){{
                    color = Pal.thoriumPink;
                    damage = 20f;
                    length = 130f;
                    width = 1f;
                    status = StatusEffects.none;
                }};
            }});

            segWeapSeq.add(new Weapon(){{
                rotate = true;
                mirror = false;
                reload = 60f;
                bullet = new ArtilleryBulletType(5f, 7){{
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
