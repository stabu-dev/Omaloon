package omaloon.type;

import arc.audio.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.ctype.*;
import mindustry.entities.abilities.*;
import mindustry.entities.units.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import omaloon.entities.units.*;
import omaloon.gen.*;

import static arc.Core.*;
import static mindustry.Vars.*;

public class MillipedeUnitType extends GlassmoreUnitType{
    public final Seq<Weapon> segWeapSeq = new Seq<>();

    public TextureRegion segmentRegion, tailRegion, segmentCellRegion, tailCellRegion,
            segmentOutline, tailOutline, payloadCellRegion;
    public Seq<Weapon> bottomWeapons = new Seq<>();
    //Millipedes
    /**
     * Decal used on unit death
     */
    public MillipedeDecal millipedeDecal;

    public int segmentLength = 9;
    public int maxSegments = -1;
    //Should reduce the "Whip" effect.
    public int segmentCast = 4;
    public float segmentOffset = 23f, headOffset = 0f;
    public float angleLimit = 30f;
    public float regenTime = -1f, healthDistribution = 0.1f;
    public float segmentDamageScl = 6f;
    public float anglePhysicsSmooth = 0f;
    public float jointStrength = 1f;
    // Hopefully make segment movement more consistent
    public boolean counterDrag = false;
    // Attempt to prevent angle drifting due to the inaccurate Atan2
    public boolean preventDrifting = false;
    public boolean splittable = false, chainable = false;
    public Sound splitSound = Sounds.door, chainSound = Sounds.door;
    //Millipede rendering
    private final static Rect viewport = new Rect(), viewport2 = new Rect();
    private final static int chunks = 4;

    protected boolean immuneAll = false;

    //Legs extra
    protected static Vec2 legOffsetB = new Vec2();
    /**
     * Weapons for each segment.
     * Last item of the array is the tail's weapon.
     */
    public Seq<Weapon>[] segmentWeapons;

    public MillipedeUnitType(String name) {
        super(name);
    }

    @Override
    public Unit create(Team team){
        return super.create(team);
    }

    @Override
    public void load() {
        super.load();
        //worm
        if(millipedeDecal != null) millipedeDecal.load();
        segmentRegion = atlas.find(name + "-segment");
        tailRegion = atlas.find(name + "-tail");
        segmentCellRegion = atlas.find(name + "-segment-cell", cellRegion);
        tailCellRegion = atlas.find(name + "-tail-cell", cellRegion);
        segmentOutline = atlas.find(name + "-segment-outline");
        tailOutline = atlas.find(name + "-tail-outline");
    }

    @Override
    public void init() {
        super.init();
        if(segmentWeapons == null){
            sortSegWeapons(segWeapSeq);
            segmentWeapons = new Seq[]{segWeapSeq};
        }else{
            for(Seq<Weapon> seq : segmentWeapons){
                sortSegWeapons(seq);
            }
        }

        Seq<Weapon> addBottoms = new Seq<>();
        for(Weapon w : weapons){
            if(bottomWeapons.contains(w) && w.otherSide != -1){
                addBottoms.add(weapons.get(w.otherSide));
            }
        }

        bottomWeapons.addAll(addBottoms.distinct());

        if(immuneAll){
            immunities.addAll(content.getBy(ContentType.status));
        }
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

    public <T extends Unit & Millipedec> void drawWorm(T unit){
        Mechc mech = unit instanceof Mechc ? (Mechc)unit : null;
        float z = (unit.elevation > 0.5f ? (lowAltitude ? Layer.flyingUnitLow : Layer.flyingUnit) : groundLayer + Mathf.clamp(hitSize / 4000f, 0, 0.01f)) - (unit.layer() * 0.00001f);

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
            legOffsetB.trns(mech.baseRotation(), 0f, Mathf.lerp(Mathf.sin(mech.walkExtend(true), 2f/Mathf.PI, 1) * mechSideSway, 0f, unit.elevation));

            //front
            legOffsetB.add(Tmp.v1.trns(mech.baseRotation() + 90, 0f, Mathf.lerp(Mathf.sin(mech.walkExtend(true), 1f/Mathf.PI, 1) * mechFrontSway, 0f, unit.elevation)));

            unit.trns(legOffsetB.x, legOffsetB.y);
        }
        if(unit instanceof Legsc){
            drawLegs((Unit & Legsc)unit);
        }

        Draw.z(Math.min(z - 0.01f, Layer.bullet - 1f));

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
                Drawf.construct(Tmp.v1.x, Tmp.v1.y, tailRegion, unit.rotation - 90f, unit.regenTime() / regenTime, unit.regenTime() / regenTime, Time.time);
                Drawf.construct(unit.x, unit.y, segmentRegion, unit.rotation - 90f, unit.regenTime() / regenTime, unit.regenTime() / regenTime, Time.time);
            });
        }

        Draw.z(z - 0.02f);

        drawBody(unit);
        if(drawCell) drawCell(unit);
        if(millipedeDecal != null) millipedeDecal.draw(unit, unit.parent());

        cellRegion = tmpCell;
        region = tmp;
        outlineRegion = tmpOutline;

        //drawWeapons(unit);

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
        if (unit instanceof Millipedec m && !m.isHead()) {
            drawWorm((Unit & Millipedec) m);
        } else {
            super.draw(unit);
        }
    }

    /*@Override
    public void drawCell(Unit unit) {
        if(unit.isAdded()){
            super.drawCell(unit);
        }else{
            applyColor(unit);

            Draw.color(cellColor(unit));
            Draw.rect(payloadCellRegion, unit.x, unit.y, unit.rotation - 90);
            Draw.reset();
        }
    }*/

    @Override
    public void drawShadow(Unit unit){
        super.drawShadow(unit);
        if(unit instanceof MillipedeDefaultUnit millipedeUnit) millipedeUnit.drawShadow();
    }

    @Override
    public void drawSoftShadow(Unit unit){
        //worm
        if(!(unit instanceof MillipedeDefaultUnit millipedeUnit)) return;
        for(MillipedeSegmentUnit s : millipedeUnit.segmentUnits){
            millipedeUnit.type.drawSoftShadow(s);
        }
        float z = Draw.z();
        for(int i = 0; i < millipedeUnit.segmentUnits.length; i++){
            Draw.z(z - (i + 1.1f) / 10000f);
            millipedeUnit.type.drawSoftShadow(millipedeUnit.segmentUnits[i]);
        }
        Draw.z(z);
    }

    @Override
    public void drawOutline(Unit unit) {
        super.drawOutline(unit);
    }

    @Override
    public void drawBody(Unit unit) {
        float z = Draw.z();
        if(unit instanceof MillipedeDefaultUnit millipedeUnit){
            camera.bounds(viewport);
            int index = -chunks;
            for(int i = 0; i < millipedeUnit.segmentUnits.length; i++){
                if(i >= index + chunks){
                    index = i;
                    Unit seg = millipedeUnit.segmentUnits[index];
                    Unit segN = millipedeUnit.segmentUnits[Math.min(index + chunks, millipedeUnit.segmentUnits.length - 1)];
                    float grow = millipedeUnit.regenAvailable() && (index + chunks) >= millipedeUnit.segmentUnits.length - 1 ? seg.clipSize() : 0f;
                    Tmp.r3.setCentered(segN.x, segN.y, segN.clipSize());
                    viewport2.setCentered(seg.x, seg.y, seg.clipSize()).merge(Tmp.r3).grow(grow + (seg.clipSize() / 2f));
                }
                if(viewport.overlaps(viewport2)){
                    Draw.z(z - (i + 1f) / 10000f);
                    if(millipedeUnit.regenAvailable() && i == millipedeUnit.segmentUnits.length - 1){
                        int finalI = i;
                        Draw.draw(z - (i + 2f) / 10000f, () -> {
                            Tmp.v1.trns(millipedeUnit.segmentUnits[finalI].rotation + 180f, segmentOffset).add(millipedeUnit.segmentUnits[finalI]);
                            Drawf.construct(Tmp.v1.x, Tmp.v1.y, tailRegion, millipedeUnit.segmentUnits[finalI].rotation - 90f, millipedeUnit.repairTime / regenTime, 1f, millipedeUnit.repairTime);
                        });
                    }
                    millipedeUnit.segmentUnits[i].drawBody();
                    drawWeapons(millipedeUnit.segmentUnits[i]);
                }
            }
        }else{
            applyColor(unit);

            Draw.rect(region, unit.x, unit.y, unit.rotation - 90);

            Draw.reset();
        }

        Draw.z(z);
    }

    @Override
    public void drawWeapons(Unit unit){
        float z = Draw.z();

        applyColor(unit);
        for(WeaponMount mount : unit.mounts){
            Weapon weapon = mount.weapon;
            if(bottomWeapons.contains(weapon)) Draw.z(z - 0.0001f);

            //weapon.draw(unit, mount);
            Draw.z(z);
        }

        Draw.reset();
    }
}
