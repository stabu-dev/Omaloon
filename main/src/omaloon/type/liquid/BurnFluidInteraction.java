package omaloon.type.liquid;

import arc.math.*;
import arc.struct.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.type.*;
import omaloon.type.*;
import omaloon.world.interfaces.*;

public class BurnFluidInteraction extends FluidInteraction{
    @Override
    public boolean canInteract(Liquid liquid1, Liquid liquid2) {
        return liquid1.blockReactive && liquid2.blockReactive &&
         (
             (liquid1.flammability > 0.3f && liquid2.temperature > 0.7f) ||
             (liquid2.flammability > 0.3f && liquid1.temperature > 0.7f)
         );
    }

    @Override
    public void interaction(HasPressure build){
        if(Mathf.chance(0.1f) && !Vars.net.client()){
            Call.createBullet(Bullets.fireball, Team.derelict, build.toBuilding().x + Mathf.range(4f), build.toBuilding().y + Mathf.range(4f), Mathf.random(360f), Bullets.fireball.damage, 1, 1);
        }
        if(Mathf.chance(0.2)) Fx.fire.at(build.toBuilding().x + Mathf.range(4f), build.toBuilding().y + Mathf.range(4f));
    }

    @Override
    public boolean shouldInteract(HasPressure build){
        Seq<Liquid> flammable = Vars.content.liquids().select(l -> !Mathf.zero(build.getFluid(l), 0.001f) && l.blockReactive && l.flammability > 0.3f);
        Seq<Liquid> highTemp = Vars.content.liquids().select(l -> !Mathf.zero(build.getFluid(l), 0.001f) && l.blockReactive && l.temperature > 0.7f);
        return !flammable.isEmpty() && !highTemp.isEmpty();
    }
}
