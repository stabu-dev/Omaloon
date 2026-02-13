package omaloon.type;

import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
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

        if (unit instanceof Ornithopterc) drawBlades((Unit & Ornithopterc) unit);
        super.draw(unit);

        groundLayer = ground;
        flyingLayer = air;
    }

    public <T extends Unit & Ornithopterc> void drawBlades(T unit) {
        applyColor(unit);
        float z = unit.elevation > 0.5f ? (lowAltitude ? Layer.flyingUnitLow : Layer.flyingUnit) : groundLayer + Mathf.clamp(hitSize / 4000f, 0, 0.01f);
        
        int i = 0;
        for(Blade.BladeMount mount : unit.blades()){
            Blade blade = mount.blade;
            float rx = unit.x + Angles.trnsx(unit.rotation - 90, blade.x, blade.y);
            float ry = unit.y + Angles.trnsy(unit.rotation - 90, blade.x, blade.y);
            float bladeScl = Draw.scl * blade.bladeSizeScl;
            float shadeScl = Draw.scl * blade.shadeSizeScl;

            int seedIndex = blade.mirror ? i / 2 : i;
            float moveAngle = Mathf.randomSeed(unit.drawSeed() + seedIndex, blade.bladeMaxMoveAngle, -blade.bladeMinMoveAngle);
            float rot = unit.rotation - 90 + blade.side * moveAngle;

            if(blade.bladeRegion.found()){
                Draw.z(z + blade.layerOffset);
                Draw.alpha(blade.blurRegion.found() ? 1 - (unit.bladeMoveSpeedScl() / 0.8f) : 1);
                Draw.rect(
                blade.bladeOutlineRegion, rx, ry,
                blade.bladeOutlineRegion.width * bladeScl * blade.side,
                blade.bladeOutlineRegion.height * bladeScl,
                rot
                );
                Draw.mixcol(Color.white, unit.hitTime);
                Draw.rect(blade.bladeRegion, rx, ry,
                blade.bladeRegion.width * bladeScl * blade.side,
                blade.bladeRegion.height * bladeScl,
                rot
                );
                Draw.reset();
            }

            if(blade.blurRegion.found()){
                Draw.z(z + blade.layerOffset);
                Draw.alpha(unit.bladeMoveSpeedScl() * blade.blurAlpha * (unit.dead() ? unit.bladeMoveSpeedScl() * 0.5f : 1));
                Draw.rect(
                blade.blurRegion, rx, ry,
                blade.blurRegion.width * bladeScl * blade.side,
                blade.blurRegion.height * bladeScl,
                rot
                );
                Draw.reset();
            }

            if(blade.shadeRegion.found()){
                //Draw.z(z + blade.layerOffset + 0.001f);
                Draw.alpha(unit.bladeMoveSpeedScl() * blade.blurAlpha * (unit.dead() ? unit.bladeMoveSpeedScl() * 0.5f : 1));
                Draw.rect(
                blade.shadeRegion, rx, ry,
                blade.shadeRegion.width * shadeScl * blade.side,
                blade.shadeRegion.height * shadeScl,
                rot
                );
                Draw.mixcol(Color.white, unit.hitTime);
                Draw.reset();
            }
            i++;
        }
    }

    @Override
    public void init() {
        super.init();

        Seq<Blade> temp = new Seq<>(blades);

        blades.clear();
        for (Blade blade : temp) {
            blades.add(blade);
            if (blade.mirror) {
                Blade clone = blade.copy();
                clone.x *= -1f;
                clone.flipSprite = !clone.flipSprite;
                clone.side = -1f;

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
    public void setStats() {
        super.setStats();

        if (sample instanceof Chainedc) {
            if (segmentUnit != null) stats.add(Stat.weapons, StatValues.weapons(this, segmentUnit.weapons));
            if (segmentEndUnit != null) stats.add(Stat.weapons, StatValues.weapons(this, segmentEndUnit.weapons));
        }
    }

    @Override
    public Unit spawn(Team team, float x, float y, float rotation, Cons<Unit> cons){
        Unit unit = super.spawn(team, x, y, rotation, cons);

        if(unit instanceof Chainedc chain && segmentUnit != null){
            UnitType segType = segmentUnit;
            for(int i = 0; i < segmentUnits - 1; i++){
                UnitType type = (i == segmentUnits - 2 && segmentEndUnit != null) ? segmentEndUnit : segType;
                Unit segment = type.create(team);
                Tmp.v1.trns(unit.rotation + 180f, segmentSpacing * (i + 1f)).add(unit);
                segment.set(Tmp.v1);
                segment.add();
                chain.connect(segment);
            }
            chain.head().updateChain();
        }

        return unit;
    }
}