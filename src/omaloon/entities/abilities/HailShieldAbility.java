package omaloon.entities.abilities;

import arc.audio.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.abilities.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.meta.*;

import java.util.concurrent.atomic.*;

import static mindustry.Vars.tilesize;
import static omaloon.OmaloonMod.*;

/**
 * An ability for a shield covering the unit that protects from hail, but not enemy bullets.
 */
public class HailShieldAbility extends Ability{
    /**
     * Position relative to unit.
     */
    public float x, y;

    /**
     * Shield radius, defaults to twice the unit's hitsize.
     */
    public float radius = -1f;

    /**
     * Shield regen amount per tick while active.
     */
    public float regen = 0.1f;
    /**
     * Shield regen amount per tick while broken.
     */
    public float regenBroken = 1f;
    /**
     * Maximum shield health.
     */
    public float maxHealth = 100f;

    /**
     * When true, effects and sounds will be played at the position of the shield instead of the bullet's position.
     */
    public boolean parentizeEffect = true;

    /**
     * Layer offset for ability.
     */
    public float layerOffset = 0f;

    /**
     * Effect displayed when something hits the shield.
     */
    public Effect hitEffect = Fx.none;
    /**
     * Effect displayed when the shield is broken.
     */
    public Effect breakEffect = Fx.none;
    /**
     * Effect displayed when the shield regenerates.
     */
    public Effect regenEffect = Fx.none;

    /**
     * Sound played when something hits the shield.
     */
    public Sound hitSound = Sounds.none;
    public float hitSoundVolume = 1;

    /**
     * Color displayed in shield health bar.
     */
    public Color barColor = Pal.heal;

    /**
     * Color used in HitEffect;
     */
    public Color hitColor = Color.white;

    /** Shield visibility timer. */
    protected float shieldVisibleTime = 0f;

    /** Duration the shield remains visible after being hit. */
    public float shieldVisibleDuration = 10f;


    protected float damage;
    protected boolean broken;

    @Override
    public void addStats(Table t){
        t.add("[lightgray]" + Stat.health.localized() + ": [white]" + Math.round(maxHealth)).row();
        t.add("[lightgray]" + Stat.range.localized() + ": [white]" + Strings.autoFixed(radius / tilesize, 2) + " " + StatUnit.blocks.localized()).row();
        t.add("[lightgray]" + Stat.repairSpeed.localized() + ": [white]" + Strings.autoFixed(regen * 60f, 2) + StatUnit.perSecond.localized()).row();
        t.add("[lightgray]" + Stat.cooldownTime.localized() + ": [white]" + Strings.autoFixed(maxHealth / regenBroken / 60f, 2) + " " + StatUnit.seconds.localized()).row();
    }

    @Override
    public void displayBars(Unit unit, Table bars){
        bars.add(new Bar("bar.hail-shield-health", barColor, () -> 1f - (damage / maxHealth)).blink(Color.white));
    }

    @Override
    public void init(UnitType type){
        if(radius == -1) radius = type.hitSize * 1.3f;
    }

    @Override
    public void draw(Unit unit){
        if(broken) return;

        float alpha = Mathf.clamp(shieldVisibleTime / shieldVisibleDuration);

        if(alpha > 0.001f){
            Fill.light(unit.x + x, unit.y + y, Lines.circleVertices(radius), radius,
                Color.clear,
                Tmp.c2.set(Pal.heal).lerp(Color.white, Mathf.clamp(unit.hitTime() / 2f)).a(0.7f * alpha)
            );
        }
    }

    @Override
    public void update(Unit unit){
        float dx = unit.x + x,
            dy = unit.y + y;

        if(broken){
            if(damage > 0){
                damage -= Time.delta * regenBroken;
            }else{
                broken = false;
                regenEffect.at(dx, dy);
            }
        }else{
            if(damage > 0) damage -= Time.delta * regen;

            AtomicBoolean wasHit = new AtomicBoolean(false);
            Groups.bullet.intersect(
                unit.x + x - radius - shieldBuffer,
                unit.y + y - radius - shieldBuffer,
                (radius + shieldBuffer) * 2f,
                (radius + shieldBuffer) * 2f,
                b -> {
                    if(b.team == Team.derelict){
                        if(Mathf.dst(unit.x + x, unit.y + y, b.x, b.y) <= radius + b.type.splashDamageRadius){
                            b.absorb();
                            if(parentizeEffect){
                                hitEffect.at(dx, dy, b.hitSize, hitColor);
                                hitSound.at(dx, dy, Mathf.random(0.9f, 1.1f), hitSoundVolume);
                            }else{
                                hitEffect.at(b.x, b.y, b.hitSize, hitColor);
                                hitSound.at(b.x, b.y, Mathf.random(0.9f, 1.1f), hitSoundVolume);
                            }
                            damage += b.damage;
                            wasHit.set(true);
                            if(damage > maxHealth){
                                broken = true;
                                breakEffect.at(dx, dy, b.hitSize, unit);
                            }
                        }
                    }
                }
            );

            if(wasHit.get()){
                shieldVisibleTime = shieldVisibleDuration;
            }else{
                shieldVisibleTime = Math.max(0f, shieldVisibleTime - Time.delta);
            }
        }
    }
}
