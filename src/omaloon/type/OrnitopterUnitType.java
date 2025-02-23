package omaloon.type;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.struct.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.meta.*;
import omaloon.gen.*;
import omaloon.world.draw.*;

public class OrnitopterUnitType extends GlassmoreUnitType{
    public final Seq<Blade> blades = new Seq<>();

    public float bladeDeathMoveSlowdown = 0.01f, fallDriftScl = 60f;
    public float fallSmokeX = 0f, fallSmokeY = 0f, fallSmokeChance = 0.1f;

    public OrnitopterUnitType(String name){
        super(name);
        engineSize = 0f;
        outlineColor = Color.valueOf("454552");
        envDisabled = Env.space;
    }

    public void drawBlade(Unit unit){
        float z = unit.elevation > 0.5f ? (lowAltitude ? Layer.flyingUnitLow : Layer.flyingUnit) : groundLayer + Mathf.clamp(hitSize / 4000f, 0, 0.01f);

        applyColor(unit);
        if(unit instanceof Ornitopterc copter){
            for(int sign : Mathf.signs){
                long seedOffset = 0;
                for(Blade.BladeMount mount : copter.blades()){
                    Blade blade = mount.blade;
                    float rx = unit.x + Angles.trnsx(unit.rotation - 90, blade.x * sign, blade.y);
                    float ry = unit.y + Angles.trnsy(unit.rotation - 90, blade.x * sign, blade.y);
                    float bladeScl = Draw.scl * blade.bladeSizeScl;
                    float shadeScl = Draw.scl * blade.shadeSizeScl;


                    if(blade.bladeRegion.found()){
                        Draw.z(z + blade.layerOffset);
                        Draw.alpha(blade.blurRegion.found() ? 1 - (copter.bladeMoveSpeedScl() / 0.8f) : 1);
                        Draw.rect(
                            blade.bladeOutlineRegion, rx, ry,
                            blade.bladeOutlineRegion.width * bladeScl * sign,
                            blade.bladeOutlineRegion.height * bladeScl,
                            unit.rotation - 90 + sign * Mathf.randomSeed(copter.drawSeed() + (seedOffset++), blade.bladeMaxMoveAngle, -blade.bladeMinMoveAngle)
                        );
                        Draw.mixcol(Color.white, unit.hitTime);
                        Draw.rect(blade.bladeRegion, rx, ry,
                            blade.bladeRegion.width * bladeScl * sign,
                            blade.bladeRegion.height * bladeScl,
                            unit.rotation - 90 + sign * Mathf.randomSeed(copter.drawSeed() + (seedOffset++), blade.bladeMaxMoveAngle, -blade.bladeMinMoveAngle)
                        );
                        Draw.reset();
                    }

                    if(blade.blurRegion.found()){
                        Draw.z(z + blade.layerOffset);
                        Draw.alpha(copter.bladeMoveSpeedScl() * blade.blurAlpha * (copter.dead() ? copter.bladeMoveSpeedScl() * 0.5f : 1));
                        Draw.rect(
                            blade.blurRegion, rx, ry,
                            blade.blurRegion.width * bladeScl * sign,
                            blade.blurRegion.height * bladeScl,
                            unit.rotation - 90 + sign * Mathf.randomSeed(copter.drawSeed() + (seedOffset++), blade.bladeMaxMoveAngle, -blade.bladeMinMoveAngle)
                        );
                        Draw.reset();
                    }

                    if(blade.shadeRegion.found()){
                        //Draw.z(z + blade.layerOffset + 0.001f);
                        Draw.alpha(copter.bladeMoveSpeedScl() * blade.blurAlpha * (copter.dead() ? copter.bladeMoveSpeedScl() * 0.5f : 1));
                        Draw.rect(
                            blade.shadeRegion, rx, ry,
                            blade.shadeRegion.width * shadeScl * sign,
                            blade.shadeRegion.height * shadeScl,
                            unit.rotation - 90 + sign * Mathf.randomSeed(copter.drawSeed() + (seedOffset++), blade.bladeMaxMoveAngle, -blade.bladeMinMoveAngle)
                        );
                        Draw.mixcol(Color.white, unit.hitTime);
                        Draw.reset();
                    }
                }
            }
        }
    }

    @Override
    public void createIcons(MultiPacker packer){
        super.createIcons(packer);
        for(Blade blade : blades){
            Outliner.outlineRegion(packer, blade.bladeRegion, outlineColor, blade.spriteName + "-outline", outlineRadius);
            Outliner.outlineRegion(packer, blade.shadeRegion, outlineColor, blade.spriteName + "-top-outline", outlineRadius);
        }
    }

    @Override
    public void draw(Unit unit){
        drawBlade(unit);
        super.draw(unit);
    }

    @Override
    public void load(){
        super.load();
        blades.each(Blade::load);
    }
}
