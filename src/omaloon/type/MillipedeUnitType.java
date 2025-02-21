package omaloon.type;

import arc.audio.*;
import arc.func.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.entities.abilities.*;
import mindustry.entities.units.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import omaloon.gen.*;

import static arc.Core.atlas;

public class MillipedeUnitType extends GlassmoreUnitType{
    public TextureRegion segmentRegion, tailRegion, segmentCellRegion, tailCellRegion,
        segmentOutline, tailOutline;
    public Seq<Weapon> bottomWeapons = new Seq<>();
    //Millipedes
    /**
     * Decal used on unit death
     */
    public MillipedeDecal millipedeDecal;

    /**
     * Min amount of segments required for this chain, any less and everything dies.
     */
    public int minSegments = 3;
    // TODO rename
    /**
     * Max amount of segments that this chain can grow to.
     */
    public int segmentLength = 9;
    /**
     * Max amount of segments that this chain can be. Will not chain if total amount of the resulting chain is bigger.
     */
    public int maxSegments = 18;

    /**
     * Offset between each segment of the chain.
     */
    public float segmentOffset = -1f;
    /**
     * Max difference of angle that one segment can be from the parent.
     */
    public float angleLimit = 30f;

    /**
     * Time taken for a new segment to grow. If -1 it will not grow.
     */
    public float regenTime = -1f;
    /**
     * Time taken for 2 chains to connect to each-other. If -1 will now connect.
     */
    public float chainTime = -1f;

    /**
     * Sound played when growing/chaining.
     */
    public Sound chainSound = Sounds.door;

    /**
     * Sound played when splitting. Splittable must be true.
     */
    public Sound splitSound = Sounds.door;

    /**
     * If true, this unit can split when one of it's segments die. If applicable.
     */
    public boolean splittable = false;

    //Should reduce the "Whip" effect.
    public int segmentCast = 4;
    public float segmentDamageScl = 6f;
    public float segmentLayerOffset = 0f;

    //Legs extra
    protected static Vec2 legOffsetB = new Vec2();

    public final Seq<Seq<Weapon>> chainWeapons = new Seq<>();
    public Intf<Unit> weaponsIndex = unit -> {
        if(unit instanceof Chainedc chain) return chain.countForward();
        else return 0;
    };

    public MillipedeUnitType(String name){
        super(name);
    }

    @Override
    public Unit create(Team team){
        return super.create(team);
    }

    @Override
    public void load(){
        super.load();
        //worm
        if(millipedeDecal != null) millipedeDecal.load();
        segmentRegion = atlas.find(name + "-segment");
        tailRegion = atlas.find(name + "-tail");
        segmentCellRegion = atlas.find(name + "-segment-cell", cellRegion);
        tailCellRegion = atlas.find(name + "-tail-cell", cellRegion);
        segmentOutline = atlas.find(name + "-segment-outline");
        tailOutline = atlas.find(name + "-tail-outline");

        chainWeapons.each(w -> w.each(Weapon::load));
    }

    @Override
    public void init(){
        super.init();

        if(segmentOffset < 0) segmentOffset = hitSize * 2f;

        chainWeapons.each(w -> {
            sortSegWeapons(w);
            if(weapons.isEmpty() && !w.isEmpty()) weapons.add(w.first());
            w.each(Weapon::init);
        });
    }

    public void sortSegWeapons(Seq<Weapon> weaponSeq){
        Seq<Weapon> mapped = new Seq<>();
        for(int i = 0, len = weaponSeq.size; i < len; i++){
            Weapon w = weaponSeq.get(i);
            if(w.recoilTime < 0f){
                w.recoilTime = w.reload;
            }
            mapped.add(w);

            if(w.mirror){
                Weapon copy = w.copy();
                copy.x *= -1;
                copy.shootX *= -1;
                copy.flipSprite = !copy.flipSprite;
                mapped.add(copy);

                w.reload *= 2;
                copy.reload *= 2;
                w.recoilTime *= 2;
                copy.recoilTime *= 2;
                w.otherSide = mapped.size - 1;
                copy.otherSide = mapped.size - 2;
            }
        }

        weaponSeq.set(mapped);
    }

    public <T extends Unit&Chainedc> void drawWorm(T unit){
        Mechc mech = unit instanceof Mechc ? (Mechc)unit : null;
        float z = (unit.elevation > 0.5f ? (lowAltitude ? Layer.flyingUnitLow : Layer.flyingUnit) : groundLayer + Mathf.clamp(hitSize / 4000f, 0, 0.01f)) + (unit.countForward() * segmentLayerOffset);

        if(unit.isFlying() || shadowElevation > 0){
            TextureRegion tmpShadow = shadowRegion;
            if(!unit.isHead() || unit.isTail()){
                shadowRegion = unit.isTail() ? tailRegion : segmentRegion;
            }

            Draw.z(Math.min(Layer.darkness, z - 1f));
            drawShadow(unit);
            shadowRegion = tmpShadow;
        }

        Draw.z(z - 0.02f);
        if(mech != null){
            drawMech(mech);

            //side
            legOffsetB.trns(mech.baseRotation(), 0f, Mathf.lerp(Mathf.sin(mech.walkExtend(true), 2f / Mathf.PI, 1) * mechSideSway, 0f, unit.elevation));

            //front
            legOffsetB.add(Tmp.v1.trns(mech.baseRotation() + 90, 0f, Mathf.lerp(Mathf.sin(mech.walkExtend(true), 1f / Mathf.PI, 1) * mechFrontSway, 0f, unit.elevation)));

            unit.trns(legOffsetB.x, legOffsetB.y);
        }
        if(unit instanceof Legsc) drawLegs((Unit & Legsc)unit);

        Draw.z(Math.min(z - 0.01f, Layer.groundUnit - 1f));

        if(unit instanceof Payloadc){
            drawPayload((Unit & Payloadc)unit);
        }

        drawSoftShadow(unit);

        Draw.z(z - 0.02f);

        TextureRegion tmp = region, tmpCell = cellRegion, tmpOutline = outlineRegion;
        if(!unit.isHead()){
            region = unit.isTail() ? tailRegion : segmentRegion;
            cellRegion = unit.isTail() ? tailCellRegion : segmentCellRegion;
            outlineRegion = unit.isTail() ? tailOutline : segmentOutline;
        }

        drawOutline(unit);
        drawWeaponOutlines(unit);

        if(unit.isTail()){
            Draw.draw(z + 0.01f, () -> {
                Tmp.v1.trns(unit.rotation + 180f, segmentOffset).add(unit);
                Drawf.construct(Tmp.v1.x, Tmp.v1.y, tailRegion, unit.rotation - 90f, unit.growTime() / regenTime, unit.growTime() / regenTime, Time.time);
                Drawf.construct(unit.x, unit.y, segmentRegion, unit.rotation - 90f, unit.growTime() / regenTime, unit.growTime() / regenTime, Time.time);
            });
        }

        Draw.z(z - 0.02f);

        drawBody(unit);
        if(drawCell) drawCell(unit);
        if(millipedeDecal != null) millipedeDecal.draw(unit, unit.parent());

        cellRegion = tmpCell;
        region = tmp;
        outlineRegion = tmpOutline;

        drawWeapons(unit);

        if(unit.shieldAlpha > 0 && drawShields){
            drawShield(unit);
        }

        if(mech != null){
            unit.trns(-legOffsetB.x, -legOffsetB.y);
        }

        if(unit.abilities.length > 0){
            for(Ability a : unit.abilities){
                Draw.reset();
                a.draw(unit);
            }

            Draw.reset();
        }
    }

    @Override
    public void draw(Unit unit){
        if(unit instanceof Chainedc m && !m.isHead()){
            drawWorm((Unit & Chainedc)m);
        }else{
            super.draw(unit);
        }
    }

    @Override
    public void drawWeapons(Unit unit){
        float z = Draw.z();

        applyColor(unit);
        for(WeaponMount mount : unit.mounts){
            Weapon weapon = mount.weapon;
            if(bottomWeapons.contains(weapon)) Draw.z(z - 0.0001f);

            weapon.draw(unit, mount);
            Draw.z(z);
        }

        Draw.reset();
    }

    @Override
    public boolean hasWeapons(){
        return chainWeapons.contains(w -> !w.isEmpty());
    }
}
