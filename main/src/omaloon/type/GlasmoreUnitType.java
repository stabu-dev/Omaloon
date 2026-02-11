package omaloon.type;

import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.graphics.MultiPacker.*;
import mindustry.type.*;
import mindustry.type.ammo.*;
import mindustry.world.meta.*;
import omaloon.content.*;
import omaloon.entities.*;
import omaloon.gen.*;

public class GlasmoreUnitType extends UnitType{
    private static final Vec2 legOffset = new Vec2();

    public boolean killSmallChains = false;
    public boolean splittable = false;

    public float segmentLayerOffset = 0.001f;

    // Ornithopter
    public Seq<Blade> blades = new Seq<>();
    public float bladeDeathMoveSlowdown = 0.01f, fallDriftScl = 60f;
    public float fallSmokeX = 0f, fallSmokeY = 0f, fallSmokeChance = 0.1f;

    public GlasmoreUnitType(String name){
        super(name);
        outlineColor = Color.valueOf("2f2f36");
        envDisabled = Env.space;
        ammoType = new ItemAmmoType(OlItems.cobalt);
        researchCostMultiplier = 8f;
    }
    @Override
    public void createIcons(MultiPacker packer){
        super.createIcons(packer);
        for(Blade blade : blades){
            if (!blade.bladeRegion.found() || blade.bladeOutlineRegion.found()) continue;
//            Outliner.outlineRegion(packer, blade.bladeRegion, outlineColor, blade.spriteName + "-outline", outlineRadius);
//            Outliner.outlineRegion(packer, blade.shadeRegion, outlineColor, blade.spriteName + "-top-outline", outlineRadius);

            makeOutline(PageType.main, packer, blade.bladeRegion, true, outlineColor, outlineRadius);
        }
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
        if (unit instanceof Ornithopterc) drawBlades((Unit & Ornithopterc) unit);

        groundLayer = ground;
        flyingLayer = air;
    }

    public <T extends Unit & Ornithopterc> void drawBlades(T unit) {
        applyColor(unit);
        long seedOffset = 0;
        float z = Draw.z();
        for(Blade.BladeMount mount : unit.blades()){
            Blade blade = mount.blade;

            float rx = unit.x + Angles.trnsx(unit.rotation - 90, blade.x, blade.y);
            float ry = unit.y + Angles.trnsy(unit.rotation - 90, blade.x, blade.y);
            float bladeScl = Draw.scl * blade.bladeSizeScl;
            float shadeScl = Draw.scl * blade.shadeSizeScl;


            if(blade.bladeRegion.found()){
                if (blade.flipSprite) Draw.xscl *= -1f;
                Draw.z(z + blade.layerOffset);
                Draw.alpha(blade.blurRegion.found() ? 1 - (unit.bladeMoveSpeedScl() / 0.8f) : 1);
                Draw.rect(
                        blade.bladeOutlineRegion, rx, ry,
                        blade.bladeOutlineRegion.width * bladeScl * Draw.xscl,
                        blade.bladeOutlineRegion.height * bladeScl,
//                        unit.rotation - 90 + sign * Mathf.randomSeed(unit.drawSeed() + (seedOffset++), blade.bladeMaxMoveAngle, -blade.bladeMinMoveAngle)
                        unit.rotation - 90 + Draw.xscl * Mathf.randomSeed(unit.drawSeed() + (seedOffset++), blade.bladeMaxMoveAngle, -blade.bladeMinMoveAngle)
                );
                Draw.mixcol(Color.white, unit.hitTime);
                Draw.rect(blade.bladeRegion, rx, ry,
                        blade.bladeRegion.width * bladeScl * Draw.xscl,
                        blade.bladeRegion.height * bladeScl,
//                        unit.rotation - 90 + sign * Mathf.randomSeed(unit.drawSeed() + (seedOffset++), blade.bladeMaxMoveAngle, -blade.bladeMinMoveAngle)
                        unit.rotation - 90 + Draw.xscl * Mathf.randomSeed(unit.drawSeed() + (seedOffset++), blade.bladeMaxMoveAngle, -blade.bladeMinMoveAngle)
                );
                Draw.reset();
            }

            if(blade.blurRegion.found()){
                if (blade.flipSprite) Draw.xscl *= -1f;
                Draw.z(z + blade.layerOffset);
                Draw.alpha(unit.bladeMoveSpeedScl() * blade.blurAlpha * (unit.dead() ? unit.bladeMoveSpeedScl() * 0.5f : 1));
                Draw.rect(
                        blade.blurRegion, rx, ry,
                        blade.blurRegion.width * bladeScl * Draw.xscl,
                        blade.blurRegion.height * bladeScl,
//                        unit.rotation - 90 + sign * Mathf.randomSeed(unit.drawSeed() + (seedOffset++), blade.bladeMaxMoveAngle, -blade.bladeMinMoveAngle)
                        unit.rotation - 90 + Draw.xscl * Mathf.randomSeed(unit.drawSeed() + (seedOffset++), blade.bladeMaxMoveAngle, -blade.bladeMinMoveAngle)
                );
                Draw.reset();
            }

            if(blade.shadeRegion.found()){
                if (blade.flipSprite) Draw.xscl *= -1f;
                //Draw.z(z + blade.layerOffset + 0.001f);
                Draw.alpha(unit.bladeMoveSpeedScl() * blade.blurAlpha * (unit.dead() ? unit.bladeMoveSpeedScl() * 0.5f : 1));
                Draw.rect(
                        blade.shadeRegion, rx, ry,
                        blade.shadeRegion.width * shadeScl * Draw.xscl,
                        blade.shadeRegion.height * shadeScl,
//                        unit.rotation - 90 + sign * Mathf.randomSeed(unit.drawSeed() + (seedOffset++), blade.bladeMaxMoveAngle, -blade.bladeMinMoveAngle)
                        unit.rotation - 90 + Draw.xscl * Mathf.randomSeed(unit.drawSeed() + (seedOffset++), blade.bladeMaxMoveAngle, -blade.bladeMinMoveAngle)
                );
                Draw.mixcol(Color.white, unit.hitTime);
                Draw.reset();
            }
        }
    }

    @Override
    public void init() {
        super.init();

        Seq<Blade> temp = blades.copy();

        blades.clear();
        for (Blade blade : temp) {
            if (blade.mirror) {
                Blade clone = blade.copy();
                clone.x *= -1f;
                clone.flipSprite = !clone.flipSprite;

                blades.add(blade);
                blades.add(clone);
            }
        }
    }

    @Override
    public void load() {
        super.load();

        blades.each(Blade::load);
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