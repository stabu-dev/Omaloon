package omaloon.ai.drone;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.geom.*;
import arc.util.*;
import arclibrary.graphics.*;
import mindustry.*;
import mindustry.entities.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.input.*;
import mindustry.type.*;
import mindustry.ui.*;
import omaloon.*;
import omaloon.ai.*;
import omaloon.math.*;
import omaloon.utils.*;

import static mindustry.Vars.*;

/**
 * @author Zelaux
 */
public class AttackDroneAI extends DroneAI{

    public static final float SMOOTH = 30f;
    public static final float POINT_RADIUS = 1f;

    public AttackDroneAI(Unit owner){
        super(owner);
    }

    @Override
    public void updateMovement(){
        float clearOwnerRange = owner.type.range;
        float realRange = clearOwnerRange + unit.type.range;
        if(!unit.hasWeapons() || !isOwnerShooting()){
            rally();
            return;
        }


        posTeam.set(owner.aimX(), owner.aimY());

        float range = unit.type().range;
        float moveRange = range * 0.75f;
        float posToOwnerDst2 = posTeam.dst2(owner);
        float safeOwnerRange = clearOwnerRange - range;


        if(DebugDraw.isDraw()){
            DebugDraw.request(Layer.end, () -> {
                Draw.color(Pal.negativeStat);
                Lines.circle(owner.x, owner.y, clearOwnerRange);
                Draw.color(Pal.health);
                Lines.circle(owner.x, owner.y, realRange);

                Drawf.target(posTeam.x, posTeam.y, 6f, Pal.accent);

                Draw.color(Pal.removeBack);
                Lines.circle(unit.x, unit.y, range);
                Lines.circle(unit.x, unit.y, moveRange);


                Draw.color(Pal.thoriumPink);
                Lines.circle(posTeam.x, posTeam.y, range);
                Lines.circle(posTeam.x, posTeam.y, moveRange);
                Draw.color(Pal.items);
                Lines.circle(owner.x, owner.y, safeOwnerRange);
            });
        }
        if(posToOwnerDst2 < safeOwnerRange * safeOwnerRange){
            moveTo(posTeam, moveRange, SMOOTH);
        }else{
//            float unitToTarget2 = unit.dst2(posTeam);

            boolean isNewPosNear = owner.within(
                Tmp.v1
                    .set(unit)
                    .sub(posTeam)
                    .nor()
                    .scl(moveRange)
                    .add(posTeam),
                clearOwnerRange);
            if(/*unitToTarget2 >= moveRange * moveRange &&*/ isNewPosNear){
                moveTo(posTeam, moveRange, SMOOTH);
            }else{
                Vec2 output = Tmp.v2;
                if(!OlGeometry.intersectionPoint(
                    owner, clearOwnerRange, posTeam, moveRange, Tmp.v1.set(unit).sub(posTeam), output
                )){

                    output
                        .set(posTeam)
                        .sub(owner)
                        .nor()
                        .scl(clearOwnerRange)
                        .add(owner);
                    if(DebugDraw.isDraw()){
                        DebugDraw.request(Layer.end, () -> {
                            DrawText.defaultFont = Fonts.def;
                            Draw.color(Pal.items, 1f);
                            DrawText.drawText(owner.x, owner.y + tilesize * 2, "-^-");
                        });
                    }
                }else if(DebugDraw.isDraw()){
                    float x = output.x;
                    float y = output.y;
                    DebugDraw.request(Layer.end, () -> {
                        DrawText.defaultFont = Fonts.def;
                        Draw.color(Pal.spore, 1f);
                        DrawText.drawText(owner.x, owner.y + tilesize, Tmp.v1.set(x, y).toString());
                    });
                }
                if(DebugDraw.isDraw()){
                    float x = output.x;
                    float y = output.y;
                    DebugDraw.request(Layer.end, () -> {
                        Draw.color(Pal.spore);
                        EFill.polyCircle(x, y, POINT_RADIUS);
                    });
                }
                moveTo(output, POINT_RADIUS, SMOOTH);
            }

        }
        unit.lookAt(posTeam);
        unit.controlWeapons(true, true);
    }

    private boolean isOwnerShooting(){
        if(owner.isShooting()) return true;
        if(!(owner.controller() instanceof Player player)) return false;
        if(player.shooting) return true;
        if(player != Vars.player) return false;

        MobileInput mobile = OmaloonMod.control.input.mobile;
        if(mobile == null) return false;
        if(!Core.settings.getBool("autotarget")) return false;
        UnitType ownerType = owner.type;
        float ownerRange = owner.range();
//        if(!ownerType.canAttack) return false;
        if(mobile.target == null){
            mobile.target = Units.closestTarget(
                owner.team, owner.x, owner.y,
                ownerRange, u -> u.checkTarget(ownerType.targetAir, ownerType.targetGround), u -> ownerType.targetGround);

            if(mobile.target == null) return false;
        }

        //using self unit bulletSpeed
        float bulletSpeed = unit.type.weapons.first().bullet.speed;
        Vec2 intercept = Predict.intercept(owner, mobile.target, bulletSpeed);

//        player.shooting = !boosted;

        owner.aim(player.mouseX = intercept.x, player.mouseY = intercept.y);

        return true;
    }

    @Override
    public Teamc target(float x, float y, float range, boolean air, boolean ground){
        return (!owner.isValid() && !isOwnerShooting()) ? null : posTeam;
    }

    @Override
    public boolean shouldShoot(){
        return isOwnerShooting();
    }

    public void beforeSync(){
        if(owner.controller() != player) return;
        if(owner.isShooting())return;
        player.shooting=isOwnerShooting();//sets aim stuff
    }
}