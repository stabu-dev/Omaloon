package omaloon.ai.drone;

import arc.graphics.g2d.*;
import arc.math.geom.*;
import arc.util.*;
import arclibrary.graphics.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.*;
import omaloon.ai.*;
import omaloon.math.*;
import omaloon.utils.*;

import static mindustry.Vars.tilesize;

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
        if(!unit.hasWeapons()) return;
        if(!isOwnerShooting()){
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
        return owner.isShooting() || owner.controller() instanceof Player player && player.shooting;
    }

    @Override
    public Teamc target(float x, float y, float range, boolean air, boolean ground){
        return (!owner.isValid() && !isOwnerShooting()) ? null : posTeam;
    }

    @Override
    public boolean shouldShoot(){
        return isOwnerShooting();
    }
}