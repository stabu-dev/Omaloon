package omaloon.entities.comp;

import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.*;
import mindustry.ai.types.*;
import mindustry.content.*;
import mindustry.core.*;
import mindustry.entities.*;
import mindustry.entities.EntityCollisions.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.*;
import omaloon.annotations.Annotations.*;
import omaloon.gen.*;
import omaloon.type.*;

@SuppressWarnings("unused")
@EntityComponent
abstract class BounceComp implements Unitc, Tankc{
    private static final float maxHop = 0.45f;
    private static final Vec2 heading = new Vec2();

    @Import UnitType type;
    @Import float x, y, elevation, rotation, hitSize, treadTime, dragMultiplier, speedMultiplier;
    @Import Vec2 vel;
    @Import boolean walked;

    transient float hopVel;
    transient float skid;
    transient boolean driven;
    transient float deform, deformVel;
    transient float lastVelX, lastVelY, lastPosX, lastPosY;

    public void hop(float power){
        hopVel = Math.max(hopVel, power);
        skid = 0f;
    }

    public boolean hopping(){
        return elevation > 0f || hopVel > 0f;
    }

    public boolean airborne(){
        return elevation > ((GlasmoreUnitType)type).hopContact;
    }

    public boolean skidding(){
        return skid > 0f;
    }

    public void squash(float amount){
        deform = Math.min(deform + amount, 0.4f);
    }

    public float rollSpeed(){
        return vel.x * Angles.trnsx(rotation, 1f) + vel.y * Angles.trnsy(rotation, 1f);
    }

    void roll(float target, float rate){
        float roll = rollSpeed(), delta = Mathf.approach(roll, target, rate * Time.delta) - roll;
        vel.add(Angles.trnsx(rotation, delta), Angles.trnsy(rotation, delta));
    }

    @Override
    public void update(){
        GlasmoreUnitType type = (GlasmoreUnitType)this.type;

        deformVel += (-deform * type.deformStiffness - deformVel * type.deformDamping) * Time.delta;
        deform += deformVel * Time.delta;

        float bounceSpeed = type.wallBounceSpeed * speed();
        boolean simulated = !Vars.net.client() || isLocal();
        boolean bx = simulated && Math.abs(lastVelX) > bounceSpeed && Math.abs(x - lastPosX) < Math.abs(lastVelX) * Time.delta / 2f && wallAt(Math.signum(lastVelX), 0f);
        boolean by = simulated && Math.abs(lastVelY) > bounceSpeed && Math.abs(y - lastPosY) < Math.abs(lastVelY) * Time.delta / 2f && wallAt(0f, Math.signum(lastVelY));
        if(bx || by){
            float frac = Mathf.clamp(Mathf.len(lastVelX, lastVelY) / speed());
            if(bx) vel.x = -lastVelX * type.wallBounce;
            if(by) vel.y = -lastVelY * type.wallBounce;

            hop(type.wallHop * frac);
            squash(0.25f * frac);

            if(!Vars.headless){
                float nx = bx ? Math.signum(lastVelX) : 0f, ny = by ? Math.signum(lastVelY) : 0f;
                float cx = x + nx * hitSize / 2f, cy = y + ny * hitSize / 2f;
                if(type.bounceWallEffect != null) type.bounceWallEffect.at(cx, cy, Angles.angle(-nx, -ny));
                if(type.bounceWallSound != null) type.bounceWallSound.at(cx, cy, Mathf.random(0.9f, 1.1f), 0.25f * frac);
            }
        }

        float speed = vel.len();
        if(simulated && !bx && !by && speed > bounceSpeed){
            Unit other = Units.closest(team(), x, y, hitSize + 16f, u -> u != self() && u.isGrounded() &&
                within(u, (hitSize + u.hitSize) / 2f + 1f) && (u.x - x) * vel.x + (u.y - y) * vel.y > 0f);

            if(other != null){
                float frac = Mathf.clamp(speed / speed()), nx = (x - other.x), ny = (y - other.y), len = Mathf.len(nx, ny);
                nx /= len;
                ny /= len;
                float dot = vel.x * nx + vel.y * ny;
                other.impulseNet(Tmp.v1.set(nx, ny).scl(dot * mass()));
                vel.sub(nx * dot * 2f, ny * dot * 2f).scl(type.wallBounce);

                hop(type.wallHop * frac);
                squash(0.25f * frac);

                if(!Vars.headless){
                    float cx = x - nx * hitSize / 2f, cy = y - ny * hitSize / 2f;
                    if(type.bounceWallEffect != null) type.bounceWallEffect.at(cx, cy, Angles.angle(nx, ny));
                    if(type.bounceWallSound != null) type.bounceWallSound.at(cx, cy, Mathf.random(0.9f, 1.1f), 0.25f * frac);
                }
            }
        }

        if(skidding() && !airborne()){
            skid -= Time.delta;
            treadTime += Math.max(type.skidSpin * speed() - Math.abs(rollSpeed()), 0f) * Time.delta;
            walked = true;
        }

        if(!airborne()){
            float fx = Angles.trnsx(rotation, 1f), fy = Angles.trnsy(rotation, 1f), side = vel.x * -fy + vel.y * fx;
            side *= Math.min(type.wheelGrip * Time.delta, 1f);
            vel.add(fy * side, -fx * side);

            if(!driven) roll(0f, type.accel * speed());
        }else{
            dragMultiplier *= type.hopDrag;
        }

        driven = false;
        lastVelX = vel.x;
        lastVelY = vel.y;
        lastPosX = x;
        lastPosY = y;
        if(!hopping()) return;

        hopVel -= type.hopGravity * Time.delta;
        elevation = Math.min(elevation + hopVel * Time.delta, maxHop);

        if(elevation <= 0f){
            elevation = 0f;
            float impact = -hopVel;
            hopVel = impact >= type.hopMinBounce ? impact * type.hopRestitution : 0f;
            skid = type.skidTime;
            squash(impact * type.landSquash);

            Tile tile = tileOn();
            if(tile != null && !Vars.headless && type.bounceLandEffect != null){
                type.bounceLandEffect.at(x, y, Mathf.clamp(impact / type.hopMinBounce * 0.15f, 0.3f, 1f), tile.getFloorColor());
            }
        }
    }

    @Replace
    @Override
    public void moveAt(Vec2 vector, float acceleration){
        if(airborne()) return;
        if(!vector.isZero(0.001f)) walked = true;
        driven = true;

        GlasmoreUnitType type = (GlasmoreUnitType)this.type;
        float want = Math.max(vector.x * Angles.trnsx(rotation, 1f) + vector.y * Angles.trnsy(rotation, 1f), -type.wheelReverse * speed()), roll = rollSpeed();
        roll(want, Math.abs(want) > Math.abs(roll) && want * roll >= 0f ? type.wheelAccel * speed() : acceleration * speed());
    }

    @Replace
    @Override
    public void rotateMove(Vec2 vector){
        if(vector.isZero() || airborne()){
            moveAt(vector);
            return;
        }

        moveAt(heading.trns(rotation, vector.len()));
        turn(vector.angle());
    }

    public void reverse(float angle){
        if(airborne()) return;

        moveAt(heading.trns(rotation, -((GlasmoreUnitType)type).wheelReverse * speed()));
        turn(angle);
    }

    void turn(float angle){
        GlasmoreUnitType type = (GlasmoreUnitType)this.type;
        rotation = Angles.moveToward(rotation, angle, type.rotateSpeed * speedMultiplier * Time.delta * Mathf.clamp(Math.abs(rollSpeed()) / (speed() * type.wheelTurnSpeed)));
    }

    boolean wallAt(float dx, float dy){
        SolidPred solid = solidity();
        float dst = hitSize / 2f + 2f;
        return solid != null && solid.solid(World.toTile(x + dx * dst), World.toTile(y + dy * dst));
    }

    @Override
    public void afterRead(){
        if(!(controller() instanceof CommandAI stored)) return;

        UnitController intended = type.createController(self());
        if(intended == null || intended.getClass() == stored.getClass()) return;

        if(!(intended instanceof CommandAI restored)){
            controller(intended);
            return;
        }

        restored.command = stored.command;
        restored.attackTarget = stored.attackTarget;
        restored.readAttackTarget = stored.readAttackTarget;
        restored.targetPos = stored.targetPos == null ? null : stored.targetPos.cpy();
        restored.targetBuild = stored.targetBuild;
        restored.stances.set(stored.stances);

        restored.commandQueue.clear();
        for(Position pos : stored.commandQueue){
            restored.commandQueue.add(pos instanceof Vec2 vec ? vec.cpy() : pos);
        }

        restored.setupLastPos();
        controller(restored);
    }

    @Replace
    @Override
    public boolean isGrounded(){
        return true;
    }

    @Replace
    @Override
    public boolean isFlying(){
        return false;
    }
}