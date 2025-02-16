package omaloon.ai.drone;

import arc.graphics.g2d.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import omaloon.ai.*;
import omaloon.utils.*;
/**
 * @author Zelaux
 * */
public class AttackDroneAI extends DroneAI{

    public static final float SMOOTH = 30f;

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
        if(DebugDraw.isDraw()){
            DebugDraw.request(Layer.end, () -> {
                Draw.color(Pal.negativeStat);
                Lines.circle(owner.x, owner.y, clearOwnerRange);
                Draw.color(Pal.health);
                Lines.circle(owner.x, owner.y, realRange);

                Drawf.target(posTeam.x, posTeam.y, 6f, Pal.accent);

                Draw.color(Pal.removeBack);
                Lines.circle(unit.x, unit.y, unit.type.range);
            });
        }

        posTeam.set(owner.aimX(), owner.aimY());
        float ownerToPosDst2 = owner.dst2(posTeam);

        unit.lookAt(posTeam);


        float range = unit.type().range;
        float ownerDst2 = owner.dst2(unit);

        if(ownerDst2<clearOwnerRange*clearOwnerRange){
            moveTo(posTeam, range * 0.75f, SMOOTH);
        }else{
            Tmp.v1.set(unit).sub(owner);
            Tmp.v2.set(posTeam).sub(unit);
            var extra=(clearOwnerRange+8);
            if(Tmp.v1.dot(Tmp.v2) < 0 && ownerDst2<extra*extra){
                moveTo(posTeam, range * 0.75f, SMOOTH);
                //TODO change to angle check?
            }else
                moveTo(owner, clearOwnerRange, SMOOTH);
        }
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