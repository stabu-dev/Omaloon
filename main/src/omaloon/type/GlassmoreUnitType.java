package omaloon.type;

import arc.Core;
import arc.func.Cons;
import arc.func.Intf;
import arc.graphics.*;
import arc.graphics.g2d.Draw;
import arc.math.Mathf;
import arc.math.geom.*;
import arc.util.Tmp;
import mindustry.game.Team;
import mindustry.gen.UnderwaterMovec;
import mindustry.gen.Unit;
import mindustry.type.*;
import mindustry.type.ammo.*;
import mindustry.world.meta.*;
import omaloon.content.*;
import omaloon.gen.Chainedc;

public class GlassmoreUnitType extends UnitType{
    private static final Vec2 legOffset = new Vec2();

    /**
     * When true, chains of this unit that are smaller than the starting chain will die.
     */
    public boolean killSmallChains = false;
    /**
     * When true, a chain of this unit will split in two when one of the segments dies instead of the entire chain dying.
     */
    public boolean splittable = false;

    public Intf<Unit> segmentRegion;

    public GlassmoreUnitType(String name){
        super(name);
        outlineColor = Color.valueOf("2f2f36");
        envDisabled = Env.space;
        ammoType = new ItemAmmoType(OlItems.cobalt);
        researchCostMultiplier = 8f;

//        abilities.add(new HailShieldAbility(){{
//            regen = 0.01f;
//            regenBroken = 0.05f;
//            layerOffset = 1f;
//            breakEffect = new WrapEffect(Fx.unitShieldBreak, Pal.heal, 1f);
//        }});
    }

    @Override
    public void drawBody(Unit unit) {
        if (!(unit instanceof Chainedc)) {
            super.drawBody(unit);
            return;
        }

        applyColor(unit);

        if(unit instanceof UnderwaterMovec){
            Draw.alpha(1f);
            Draw.mixcol(unit.floorOn().mapColor.write(Tmp.c1).mul(0.9f), 1f);
        }

        Draw.rect(segmentRegions[segmentRegion.get(unit)], unit.x, unit.y, unit.rotation - 90);

        Draw.reset();
    }

    @Override
    public void drawCell(Unit unit) {
        if (!(unit instanceof Chainedc)) {
            super.drawCell(unit);
            return;
        }

        applyColor(unit);

        Draw.color(cellColor(unit));
        Draw.rect(segmentCellRegions[segmentRegion.get(unit)], unit.x, unit.y, unit.rotation - 90);
        Draw.reset();
    }

    @Override
    public void drawOutline(Unit unit) {
        if (!(unit instanceof Chainedc)) {
            super.drawOutline(unit);
            return;
        }

        Draw.reset();

        if(Core.atlas.isFound(segmentOutlineRegions[segmentRegion.get(unit)])){
            applyColor(unit);
            applyOutlineColor(unit);
            Draw.rect(segmentOutlineRegions[segmentRegion.get(unit)], unit.x, unit.y, unit.rotation - 90);
            Draw.reset();
        }
    }

    @Override
    public void init() {
        super.init();

        if (region == null) {
            if (sample instanceof Chainedc) segmentRegion = unit -> Mathf.clamp(((Chainedc) unit).segment(), 0, segmentRegions.length - 1);
        }
    }

    @Override
    public Unit spawn(Team team, float x, float y, float rotation, Cons<Unit> cons) {
        Unit unit = super.spawn(team, x, y, rotation, cons);

        if (unit instanceof Chainedc chain) {
            for(int i = 0; i < segmentUnits - 1; i++) {
                Unit segment = create(team);
                segment.add();
                chain.connect(segment);
                chain.head().updateChain();
            }
        }

        return unit;
    }

    //    @Override
//    public void draw(Unit unit){
//        if(unit.inFogTo(Vars.player.team())) return;
//
//        boolean isPayload = !unit.isAdded();
//
//        FloatMechc floatMech = unit instanceof FloatMechc ? (FloatMechc)unit : null;
//        Mechc mech = unit instanceof Mechc ? (Mechc)unit : null;
//        float z = isPayload ? Draw.z() : unit.elevation > 0.5f ? (lowAltitude ? Layer.flyingUnitLow : Layer.flyingUnit) : groundLayer + Mathf.clamp(hitSize / 4000f, 0, 0.01f);
//
//        if(unit.controller().isBeingControlled(player.unit())){
//            drawControl(unit);
//        }
//
//        if(!isPayload && (unit.isFlying() || shadowElevation > 0)){
//            Draw.z(Math.min(Layer.darkness, z - 1f));
//            drawShadow(unit);
//        }
//
//        Draw.z(z - 0.02f);
//
//        if(floatMech != null){
//            drawMech(floatMech);
//
//            //side
//            legOffset.trns(floatMech.baseRotation(), 0f, Mathf.lerp(Mathf.sin(floatMech.walkExtend(true), 2f / Mathf.PI, 1) * mechSideSway, 0f, unit.elevation));
//
//            //front
//            legOffset.add(Tmp.v1.trns(floatMech.baseRotation() + 90, 0f, Mathf.lerp(Mathf.sin(floatMech.walkExtend(true), 1f / Mathf.PI, 1) * mechFrontSway, 0f, unit.elevation)));
//
//            unit.trns(legOffset.x, legOffset.y);
//        }
//
//        if(mech != null){
//            drawMech(mech);
//
//            //side
//            legOffset.trns(mech.baseRotation(), 0f, Mathf.lerp(Mathf.sin(mech.walkExtend(true), 2f / Mathf.PI, 1) * mechSideSway, 0f, unit.elevation));
//
//            //front
//            legOffset.add(Tmp.v1.trns(mech.baseRotation() + 90, 0f, Mathf.lerp(Mathf.sin(mech.walkExtend(true), 1f / Mathf.PI, 1) * mechFrontSway, 0f, unit.elevation)));
//
//            unit.trns(legOffset.x, legOffset.y);
//        }
//
//        if(unit instanceof Tankc){
//            drawTank((Unit & Tankc)unit);
//        }
//
//        if(unit instanceof Legsc && !isPayload){
//            drawLegs((Unit & Legsc)unit);
//        }
//
//        Draw.z(Math.min(z - 0.01f, Layer.bullet - 1f));
//
//        if(unit instanceof Payloadc){
//            drawPayload((Unit & Payloadc)unit);
//        }
//
//        drawSoftShadow(unit);
//
//        Draw.z(z);
//
//        if(unit instanceof Crawlc c){
//            drawCrawl(c);
//        }
//
//        if(drawBody) drawOutline(unit);
//        drawWeaponOutlines(unit);
//        if(engineLayer > 0) Draw.z(engineLayer);
//        if(trailLength > 0 && !naval && (unit.isFlying() || !useEngineElevation)){
//            drawTrail(unit);
//        }
//        if(engines.size > 0) drawEngines(unit);
//        Draw.z(z);
//        if(drawBody) drawBody(unit);
//        if(drawCell) drawCell(unit);
//        drawWeapons(unit);
//        if(drawItems) drawItems(unit);
//        drawLight(unit);
//
//        if(unit.shieldAlpha > 0 && drawShields){
//            drawShield(unit);
//        }
//
//        //TODO how/where do I draw under?
//        if(parts.size > 0){
//            for(int i = 0; i < parts.size; i++){
//                var part = parts.get(i);
//
//                WeaponMount first = unit.mounts.length > part.weaponIndex ? unit.mounts[part.weaponIndex] : null;
//                if(first != null){
//                    DrawPart.params.set(first.warmup, first.reload / weapons.first().reload, first.smoothReload, first.heat, first.recoil, first.charge, unit.x, unit.y, unit.rotation);
//                }else{
//                    DrawPart.params.set(0f, 0f, 0f, 0f, 0f, 0f, unit.x, unit.y, unit.rotation);
//                }
//
//                if(unit instanceof Scaled s){
//                    DrawPart.params.life = s.fin();
//                }
//
//                part.draw(DrawPart.params);
//            }
//        }
//
//        if(!isPayload){
//            for(Ability a : unit.abilities){
//                Draw.reset();
//                a.draw(unit);
//            }
//        }
//
//        if(floatMech != null){
//            unit.trns(-legOffset.x, -legOffset.y);
//        }
//
//        if(mech != null){
//            unit.trns(-legOffset.x, -legOffset.y);
//        }
//
//        Draw.reset();
//    }

//    public void drawMech(FloatMechc floatMech){
//        Unit unit = (Unit)floatMech;
//
//        Draw.reset();
//
//        float e = unit.elevation;
//
//        float sin = Mathf.lerp(Mathf.sin(floatMech.walkExtend(true), 2f / Mathf.PI, 1f), 0f, e);
//        float extension = Mathf.lerp(floatMech.walkExtend(false), 0, e);
//        float boostTrns = e * 2f;
//
//        Floor floor = unit.isFlying() ? Blocks.air.asFloor() : unit.floorOn();
//
//        if(floor.isLiquid){
//            Draw.color(Color.white, floor.mapColor, 0.5f);
//        }
//
//        for(int i : Mathf.signs){
//            Draw.mixcol(Tmp.c1.set(mechLegColor).lerp(Color.white, Mathf.clamp(unit.hitTime)), Math.max(Math.max(0, i * extension / mechStride), unit.hitTime));
//
//            Draw.rect(legRegion,
//                unit.x + Angles.trnsx(floatMech.baseRotation(), extension * i - boostTrns, -boostTrns * i),
//                unit.y + Angles.trnsy(floatMech.baseRotation(), extension * i - boostTrns, -boostTrns * i),
//                legRegion.width * legRegion.scl() * i,
//                legRegion.height * legRegion.scl() * (1 - Math.max(-sin * i, 0) * 0.5f),
//                floatMech.baseRotation() - 90 + 35f * i * e);
//        }
//
//        Draw.mixcol(Color.white, unit.hitTime);
//
//        if(unit.lastDrownFloor != null){
//            Draw.color(Color.white, Tmp.c1.set(unit.lastDrownFloor.mapColor).mul(0.83f), unit.drownTime * 0.9f);
//        }else{
//            Draw.color(Color.white);
//        }
//
//        Draw.rect(baseRegion, unit, floatMech.baseRotation() - 90);
//
//        Draw.mixcol();
//    }
}
