package omaloon.entities.abilities;

import arc.*;
import arc.graphics.g2d.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import mindustry.*;
import mindustry.entities.abilities.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.meta.*;

/**
 * An ability that creates a bullet if the unit has a specific status effect.
 * @author Liz
 */
public class TankAbility extends Ability {
    public StatusEffect effect;
    public BulletType bullet;

    public String liquidRegionName;
    public TextureRegion liquidRegion;

    public float layerOffset = -0.001f;

    public TankAbility(StatusEffect effect, BulletType bullet) {
        this.effect = effect;
        this.bullet = bullet;
    }

    @Override
    public void addStats(Table t) {
        StatValues.ammo(ObjectMap.of(effect, bullet)).display(t.table().get());
    }

    @Override
    public void death(Unit unit) {
        if (data != 0 && !Vars.net.client()) {
            Call.createBullet(bullet, unit.team, unit.x, unit.y, unit.rotation, bullet.damage, 1f, 1f);
        }
    }

    @Override
    public void draw(Unit unit) {
        if (liquidRegion == null) loadSprites(unit.type);
        if (data == 0) return;
        float z = Draw.z();
        Draw.z(z + layerOffset);
        Draw.color(effect.color);
        Draw.rect(liquidRegion, unit.x, unit.y, unit.rotation - 90f);
        Draw.z(z);
    }

    public void loadSprites(UnitType type) {
        liquidRegion = Core.atlas.find(liquidRegionName == null ? type.name + "-liquid" : liquidRegionName);
    }

    @Override
    public void update(Unit unit) {
        if (!unit.dead) data = unit.hasEffect(effect) ? 1 : 0;
    }
}