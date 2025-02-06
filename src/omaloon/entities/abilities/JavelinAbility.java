package omaloon.entities.abilities;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.abilities.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.meta.*;
import omaloon.world.meta.*;

import static mindustry.Vars.state;

public class JavelinAbility extends Ability{
    public String suffix = "";
    public String name;

    /**
     * Base damaged applied when this ability is active.
     */
    public float damage = 1;
    /**
     * Min damage that this ability will apply
     */
    public float minDamage = 0f;
    /**
     * Time taken for the ability to apply the damage. In ticks.
     */
    public float damageInterval = 5f;
    /**
     * Radius of ability. Set to unit's hitSize by default.
     */
    public float radius = -1;

    public boolean targetAir = true, targetGround = true;

    /**
     * Min speed that the abiility functions.
     */
    public float minSpeed = 0.8f;
    /**
     * Max speed where it stops getting better.
     */
    public float maxSpeed = 1.2f;

    /**
     * Position offset relative to the unit.
     */
    public float x, y;

    /**
     * Layer offset relative to unit;.
     */
    public float layerOffset = 0f;

    /**
     * Position offset based on sine wave. Purely visual.
     */
    public float sclX = 1, magX = 0;
    public float sclY = 1, magY = 0;
    public float sinOffset = Mathf.PI;

    /**
     * Overlay region and effect tint.
     */
    public Color color = Color.white;
    /**
     * Overaly blending mode;
     */
    public Blending blending = Blending.additive;
    /**
     * When true, draws an overlay sprite on top of the unit.
     */
    public boolean drawOverlay = true;

    /**
     * Effect applied on every target that has been damaged by this ability. uses the unit's rotation.
     */
    public Effect hitEffect = Fx.none;

    public TextureRegion overlayRegion;

    protected float timer;
    protected final Seq<Healthc> targets = new Seq<>();

    public JavelinAbility(float damage, float damageInterval, float radius){
        this.damage = damage;
        this.damageInterval = damageInterval;
        this.radius = radius;

        suffix = "-overlay";
    }

    public JavelinAbility(){
    }

    @Override
    public void addStats(Table t){
        t.add(
        "[lightgray]" + Stat.damage.localized() + ": [white]" +
        Strings.autoFixed(60f * minDamage / damageInterval, 2) + " - " +
        Strings.autoFixed(60f * damage / damageInterval, 2) + " " + StatUnit.perSecond.localized()
        ).row();
        t.add("[lightgray]" + Stat.range.localized() + ": [white]" + Strings.autoFixed(radius / 8f, 2) + " " + StatUnit.blocks.localized()).row();
        t.add("[lightgray]" + OlStats.minSpeed.localized() + ": [white]" + Strings.autoFixed(minSpeed / 8f, 2) + " " + StatUnit.tilesSecond.localized()).row();
        t.add("[lightgray]" + OlStats.maxSpeed.localized() + ": [white]" + Strings.autoFixed(maxSpeed / 8f, 2) + " " + StatUnit.tilesSecond.localized()).row();
        t.add("[lightgray]" + Stat.targetsAir.localized() + ": [white]" + Core.bundle.get(targetAir ? "yes" : "no")).row();
        t.add("[lightgray]" + Stat.targetsGround.localized() + ": [white]" + Core.bundle.get(targetGround ? "yes" : "no")).row();
    }

    @Override
    public void draw(Unit unit){
        if(drawOverlay){
            float scl = Mathf.clamp(Mathf.map(unit.vel().len(), minSpeed, maxSpeed, 0f, 1f));

            if(overlayRegion == null) overlayRegion = Core.atlas.find(name);
            float
            drawx = unit.x + x + Mathf.sin(Time.time + unit.id, sclX, magX),
            drawy = unit.y + y + Mathf.sin(Time.time + sinOffset + unit.id, sclY, magY);
            float z = Draw.z();
            Draw.z(z - layerOffset);
            Draw.color(color);
            Draw.alpha(scl);
            Draw.blend(blending);
            Draw.rect(overlayRegion, drawx, drawy, unit.rotation - 90f);
            Draw.blend();
            Draw.z(z);
        }
    }

    @Override
    public void init(UnitType type){
        if(name == null) name = type.name + suffix;
        if(radius == -1) radius = type.hitSize;
    }

    @Override
    public void update(Unit unit){
        float scl = Mathf.clamp(Mathf.map(unit.vel().len(), minSpeed, maxSpeed, 0f, 1f));
        if(scl <= 0) return;
        if(timer >= damageInterval){
            float ax = unit.x + x, ay = unit.y + y;
            targets.clear();
            Units.nearby(null, ax, ay, radius, other -> {
                if(other != unit && other.checkTarget(targetAir, targetGround) && other.targetable(unit.team) && (other.team != unit.team)){
                    targets.add(other);
                }
            });
            if(targetGround){
                Units.nearbyBuildings(ax, ay, radius, b -> {
                    if((b.team != Team.derelict || state.rules.coreCapture) && (b.team != unit.team)){
                        targets.add(b);
                    }
                });
            }
            float dmg = Math.max(minDamage, damage * scl * state.rules.unitDamage(unit.team));
            targets.each(other -> {
                if(other instanceof Building b){
                    b.damage(unit.team, dmg);
                }else other.damage(dmg);
                hitEffect.at(other.x(), other.y(), unit.rotation - 90f, color);
            });
            timer %= 1f;
        }
        timer += Time.delta;
    }
}
