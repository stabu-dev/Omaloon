package omaloon.ai;

import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.core.*;
import mindustry.entities.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.type.*;
import omaloon.gen.*;
import omaloon.type.*;
import omaloon.type.weapons.*;

import static mindustry.Vars.*;

/** Ram runs of {@link RamWeapon} units: charge the target, fly off far enough for another run, back off when stuck. */
public class RamRun{
    static final float stuckTime = 15f, backMin = 20f, backTime = 60f;
    /** angles off the target tried when looking for a spot to fly off to */
    static final float[] awayTurns = {0f, 30f, 60f, 90f};
    private static final Vec2 vec = new Vec2(), dest = new Vec2(), aim = new Vec2();
    private static final Rect box = new Rect(), area = new Rect();
    private static final Seq<Building> builds = new Seq<>();

    public boolean charging;
    /** targets just hit or left, skipped when looking for a new one */
    public final Seq<Teamc> recent = new Seq<>();

    Unit unit;
    Teamc target;
    boolean lined, missed, fresh = true;
    /** back-offs in a row without getting up to speed */
    int backs;
    float stuck, backing, backAngle, chargeRunup;
    /** random run-up length multiplier */
    float reach = 1.2f;
    final Vec2 flyTo = new Vec2(), goal = new Vec2();

    public void update(Unit unit, Teamc target){
        this.unit = unit;
        if(target != this.target){
            this.target = target;
            charging = false;
            backs = 0;
            newRun();
        }

        if(backingOff(unit)) return;

        RamWeapon ram = ram(unit);
        float speed = unit.vel.len(), dst = unit.dst(target), minSpeed = ramSpeed();
        float runup = runup(minSpeed), cone = ram != null ? Math.min(45f, ram.shootCone * 0.6f) : 45f;
        boolean aimed = Angles.angleDist(heading(), unit.angleTo(aim())) <= cone;

        if(!charging && (dst >= runup * reach || (!fresh && unit.within(flyTo, unit.hitSize * 2f)))){
            charging = true;
            lined = false;
            chargeRunup = Math.min(runup, dst);
        }else if(charging){
            lined |= aimed;
            if(lined ? dst < runup * 0.8f && (speed < minSpeed || !aimed || !onCourse()) : dst < chargeRunup * 0.4f){
                charging = false;
                missed = true;
                newRun();
            }
        }

        if(charging){
            go(aim(), false);
        }else{
            if(fresh){
                flyTo.set(away(runup * reach));
                fresh = false;
            }
            go(flyTo, true);
        }
        checkStuck(unit, goal);
    }

    void newRun(){
        reach = Mathf.random(1f, 1.4f);
        fresh = true;
    }

    /** @return a spot with room to turn round and a clear run at the target, on the unit's side first */
    Vec2 away(float length){
        float angle = Angles.angle(target.getX(), target.getY(), unit.x, unit.y);
        float turn = ramSpeed() / (unit.type.rotateSpeed * Mathf.degRad);
        for(float len = length; len >= turn; len -= tilesize){
            for(float off : awayTurns){
                for(int sign : Mathf.signs){
                    point(angle + off * sign, len);
                    if(roomy(vec, turn) && clear(vec.x, vec.y, Angles.angle(vec.x, vec.y, target.getX(), target.getY()), vec.dst(target) - targetRadius(), false)) return vec;
                }
            }
        }

        for(; length > 0f; length -= tilesize){
            point(angle, length);
            if(unit.canPass(World.toTile(vec.x), World.toTile(vec.y))) break;
        }
        return vec;
    }

    void point(float angle, float length){
        vec.trns(angle, length).add(target.getX(), target.getY());
        vec.set(Mathf.clamp(vec.x, 0f, world.unitWidth()), Mathf.clamp(vec.y, 0f, world.unitHeight()));
    }

    boolean roomy(Vec2 point, float radius){
        int cx = World.toTile(point.x), cy = World.toTile(point.y), r = Mathf.ceil(radius / tilesize);
        for(int x = -r; x <= r; x++){
            for(int y = -r; y <= r; y++){
                if(x * x + y * y <= r * r && !unit.canPass(cx + x, cy + y)) return false;
            }
        }
        return true;
    }

    void go(Position point, boolean manoeuvre){
        dest.set(Mathf.clamp(point.getX(), 0f, world.unitWidth()), Mathf.clamp(point.getY(), 0f, world.unitHeight()));
        goal.set(dest);
        if(!clear(unit.angleTo(dest), unit.dst(dest) - (manoeuvre ? 0f : targetRadius()))){
            var result = controlPath.getPathPosition(unit, dest);
            if(result.move) dest.set(result.dest);
        }
        float speed = unit.speed();
        if(manoeuvre){
            float slowest = speed * (unit.type instanceof GlasmoreUnitType type ? type.wheelTurnSpeed : 1f);
            speed = Mathf.lerp(speed, slowest, Mathf.clamp((Angles.angleDist(unit.rotation, unit.angleTo(dest)) - 30f) / 60f));
        }
        unit.movePref(vec.set(dest).sub(unit).setLength(speed));
    }

    public void checkStuck(Unit unit, Position goal){
        this.unit = unit;
        this.goal.set(goal.getX(), goal.getY());
        if(unit instanceof Bouncec b && (b.airborne() || b.skidding())) return;
        boolean going = unit.vel.len() >= unit.speed() * 0.6f;
        if(going) backs = 0;
        if(going || !pressed(unit.rotation, 1)){
            stuck = 0f;
            return;
        }

        stuck += Time.delta;
        if(stuck < stuckTime) return;

        float diff = Mathf.mod(wayOn() - unit.rotation + 180f, 360f) - 180f;
        float turn = Math.abs(diff) > 5f ? Math.signum(diff) : clear(unit.rotation + 90f, unit.hitSize * 2f) ? 1f : -1f;
        backAngle = unit.rotation + turn * 90f;
        backing = backTime;
        backs++;
        stuck = 0f;
    }

    /** @return whether it's backing off or in the air, so nothing else should drive it */
    public boolean backingOff(Unit unit){
        this.unit = unit;
        if(unit instanceof Bouncec b && b.airborne()) return true;
        if(backing <= 0f) return false;

        backing -= Time.delta;
        float way = wayOn(), room = unit.hitSize * 3f;
        boolean done = backing < backTime - backMin && Angles.within(unit.rotation, way, 90f) && clear(unit.rotation, room);
        if(done || backing <= 0f || pressed(unit.rotation + 180f, 0) || !(unit instanceof Bouncec b)){
            backing = 0f;
            return false;
        }

        b.reverse(clear(way, room) ? way : backAngle);
        return true;
    }

    /** @return angle to the next point of the vanilla path to the goal */
    float wayOn(){
        var result = controlPath.getPathPosition(unit, dest.set(goal));
        return unit.angleTo(result.move ? result.dest : goal);
    }

    public boolean finished(Unit unit){
        RamWeapon ram = ram(unit);
        Teamc rammed = ram == null ? null : ram.lastHit(unit);
        boolean done = rammed != null || missed || backs >= 3;
        if(rammed != null){
            ram.clearLastHit(unit);
            charging = false;
            newRun();
            remember(rammed);
        }
        if(done){
            if(target != null) remember(target);
            missed = false;
            backs = 0;
        }
        return done;
    }

    void remember(Teamc target){
        recent.remove(target, true);
        recent.add(target);
        if(recent.size > 12) recent.remove(0);
    }

    public @Nullable Teamc find(Unit unit, float range, boolean air, boolean ground){
        Teamc result = Units.closestEnemy(unit.team, unit.x, unit.y, range, u -> u.checkTarget(air, ground) && !recent.contains(u, true) && inLine(unit, u));
        if(result != null || !ground) return result;

        builds.clear();
        area.setCentered(unit.x, unit.y, range * 2f);
        for(Team enemy : unit.team.data().coreEnemies){
            if(enemy.data().buildingTree != null) enemy.data().buildingTree.intersect(area, builds);
        }
        builds.sort(Structs.comps(Structs.comparingFloat(b -> -UnitSorts.buildingDefault.priority(b)), Structs.comparingFloat(b -> unit.dst(b) - b.hitSize() / 2f)));

        for(Building b : builds){
            if(b.block.targetable && b.isDiscovered(unit.team) && (unit.type.targetUnderBlocks || !b.block.underBullets) &&
                unit.dst(b) - b.hitSize() / 2f <= range && !recent.contains(b, true) && inLine(unit, b)) return b;
        }
        return null;
    }

    /** @return whether only enemy buildings are in the way */
    public boolean inLine(Unit unit, Teamc target){
        Unit lastUnit = this.unit;
        Teamc last = this.target;
        this.unit = unit;
        this.target = target;
        boolean clear = clear(unit.x, unit.y, unit.angleTo(target), unit.dst(target) - targetRadius(), true);
        this.unit = lastUnit;
        this.target = last;
        return clear;
    }

    float half(){
        unit.hitboxTile(box);
        return box.width / 2f;
    }

    /** @param sides 0 to check only the middle, so a wall it slides along doesn't count */
    boolean pressed(float angle, int sides){
        float half = half(), d = half + 2f;
        for(int i = -sides; i <= sides; i++){
            float px = unit.x + Angles.trnsx(angle, d) + Angles.trnsx(angle + 90f, half * i);
            float py = unit.y + Angles.trnsy(angle, d) + Angles.trnsy(angle + 90f, half * i);
            if(!unit.canPass(World.toTile(px), World.toTile(py))) return true;
        }
        return false;
    }

    boolean clear(float angle, float length){
        return clear(unit.x, unit.y, angle, length, false);
    }

    boolean clear(float fromX, float fromY, float angle, float length, boolean rams){
        float side = unit.hitSize / 2f;
        for(int i = -1; i <= 1; i++){
            float ox = fromX + Angles.trnsx(angle + 90f, side * i), oy = fromY + Angles.trnsy(angle + 90f, side * i);
            if(World.raycast(World.toTile(ox), World.toTile(oy),
                World.toTile(ox + Angles.trnsx(angle, length)), World.toTile(oy + Angles.trnsy(angle, length)), (x, y) -> {
                if(unit.canPass(x, y)) return false;
                Building build = world.build(x, y);
                return build != target && !(rams && build != null && build.team != unit.team && build.team != Team.derelict);
            })) return false;
        }
        return true;
    }

    Vec2 aim(){
        return aim.set(Predict.intercept(unit, target, 0f, 0f, false, Math.max(unit.vel.len(), unit.speed() * 0.5f)));
    }

    float heading(){
        return unit.vel.len() > 0.1f ? unit.vel.angle() : unit.rotation;
    }

    boolean onCourse(){
        float speed = unit.vel.len();
        if(speed < 0.1f) return false;

        float rx = aim().x - unit.x, ry = aim.y - unit.y;
        if(rx * unit.vel.x + ry * unit.vel.y <= 0f) return false;
        return Math.abs(rx * unit.vel.y - ry * unit.vel.x) / speed <= half() + targetRadius();
    }

    float ramSpeed(){
        RamWeapon ram = ram(unit);
        return ram != null && ram.minShootVelocity > 0f ? ram.minShootVelocity : unit.speed() * 0.5f;
    }

    float targetRadius(){
        return target instanceof Sized s ? s.hitSize() / 2f : 0f;
    }

    float runup(float hitSpeed){
        float max = unit.speed(), angle = unit.angleTo(aim());
        hitSpeed = Math.min(max, hitSpeed);
        float v0 = Mathf.clamp(unit.vel.x * Angles.trnsx(angle, 1f) + unit.vel.y * Angles.trnsy(angle, 1f), 0f, hitSpeed);

        float accel = max * (unit.type instanceof GlasmoreUnitType type ? type.wheelAccel : unit.type.accel);

        float turnRate = unit.type.rotateSpeed;
        if(unit instanceof Bouncec b && unit.type instanceof GlasmoreUnitType type){
            turnRate *= Mathf.clamp(Math.abs(b.rollSpeed()) / (max * type.wheelTurnSpeed), 0.25f, 1f);
        }
        float turnDist = Math.max(unit.vel.len(), max * 0.25f) * Angles.angleDist(heading(), angle) / Math.max(turnRate, 1f);

        return Math.max(accelDist(v0, hitSpeed, accel, unit.drag), 0f) + turnDist + unit.hitSize / 2f + targetRadius();
    }

    static float accelDist(float from, float to, float accel, float drag){
        float start = 1f - drag * from / accel, end = 1f - drag * to / accel;
        return drag > 0.0001f && end > 0.0001f ?
            accel / (drag * drag) * (float)Math.log(start / end) - (to - from) / drag :
            (to * to - from * from) / (2f * accel);
    }

    static @Nullable RamWeapon ram(Unit unit){
        for(Weapon weapon : unit.type.weapons){
            if(weapon instanceof RamWeapon ram) return ram;
        }
        return null;
    }
}