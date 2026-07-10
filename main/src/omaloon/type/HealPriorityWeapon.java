package omaloon.type;

import mindustry.entities.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.blocks.ConstructBlock.*;

public class HealPriorityWeapon extends Weapon{
    public HealPriorityWeapon(String name){
        super(name);
        controllable = false;
        autoTarget = true;
    }

    @Override
    protected Teamc findTarget(Unit unit, float x, float y, float range, boolean air, boolean ground){
        var allyUnit = Units.closest(unit.team, x, y, range, u -> u != unit && u.damaged() && u.checkTarget(air, ground));
        if(allyUnit != null) return allyUnit;

        if(ground){
            var allyBuilding = Units.findAllyTile(unit.team, x, y, range, b -> b.damaged() && !(b instanceof ConstructBuild));
            if(allyBuilding != null) return allyBuilding;
        }

        return super.findTarget(unit, x, y, range, air, ground);
    }

    @Override
    protected boolean checkTarget(Unit unit, Teamc target, float x, float y, float range){
        if(target.team() == unit.team){
            return !(target.within(x, y, range + Math.abs(shootY) + (target instanceof Sized s ? s.hitSize() / 2f : 0f))
            && target instanceof Healthc h && h.damaged() && h.isValid()
            && !(target instanceof ConstructBuild));
        }
        return super.checkTarget(unit, target, x, y, range);
    }
}
