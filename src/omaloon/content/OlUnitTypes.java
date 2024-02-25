package omaloon.content;

import arc.struct.*;
import ent.anno.Annotations.*;
import mindustry.ai.types.*;
import mindustry.content.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import omaloon.gen.*;
import omaloon.type.*;

import static arc.Core.*;

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
            constructor = MillipedeUnit::create;
            health = 200f;
            regenTime = 15f * 60f;
            splittable = true;
            chainable = true;
            circleTarget = true;
            omniMovement = false;
            angleLimit = 65f;
            segmentLength = 5;
            segmentDamageScl = 8f;
            segmentCast = 8;
            segmentOffset = 8;
            engineSize = -1f;
            maxSegments = 5;
            preventDrifting = true;

            legCount = 6;
            legLength = 16f;
            lockLegBase = false;
            legContinuousMove = true;
            legRegion = atlas.find(name + "-leg");

            allowLegStep = true;
            //hovering = true;
            legPhysicsLayer = true;

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
