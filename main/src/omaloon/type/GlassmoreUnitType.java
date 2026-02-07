package omaloon.type;

import arc.func.*;
import arc.graphics.*;
import arc.math.geom.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.type.ammo.*;
import mindustry.world.meta.*;
import omaloon.content.*;
import omaloon.gen.*;

public class GlassmoreUnitType extends UnitType{
    private static final Vec2 legOffset = new Vec2();

    public boolean killSmallChains = false;
    public boolean splittable = false;

    public float segmentLayerOffset = 0.001f;

    public GlassmoreUnitType(String name){
        super(name);
        outlineColor = Color.valueOf("2f2f36");
        envDisabled = Env.space;
        ammoType = new ItemAmmoType(OlItems.cobalt);
        researchCostMultiplier = 8f;
    }

    @Override
    public void draw(Unit unit){
        float ground = groundLayer;
        float air = flyingLayer;

        if(unit instanceof Chainedc chain){
            groundLayer += segmentLayerOffset * chain.segment();
            flyingLayer += segmentLayerOffset * chain.segment();
        }

        super.draw(unit);

        groundLayer = ground;
        flyingLayer = air;
    }

    @Override
    public Unit spawn(Team team, float x, float y, float rotation, Cons<Unit> cons){
        Unit unit = super.spawn(team, x, y, rotation, cons);

        if(unit instanceof Chainedc chain && segmentUnit != null){
            UnitType segType = segmentUnit;
            for(int i = 0; i < segmentUnits - 1; i++){
                UnitType type = (i == segmentUnits - 2 && segmentEndUnit != null) ? segmentEndUnit : segType;
                Unit segment = type.create(team);
                segment.add();
                chain.connect(segment);
                chain.head().updateChain();
            }
        }

        return unit;
    }
}