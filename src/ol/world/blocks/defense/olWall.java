package ol.world.blocks.defense;

import arc.*;
import arc.math.Mathf;
import arc.util.*;
import mindustry.content.Fx;
import mindustry.content.StatusEffects;
import mindustry.entities.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.blocks.defense.*;
import mindustry.world.meta.*;

import static mindustry.Vars.tilesize;

public class olWall extends Wall {

    public StatusEffect status = StatusEffects.none;
    public float statusDuration = 0f;
    public Effect shotEffect = Fx.none;
    public Effect dynamicEffect = Fx.none;
    public float dynamicEffectChance = 0f;

    public olWall(String name) {
        super(name);
        flashHit = true;
        flashColor = null;
        update = true;
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(Stat.abilities, table -> {
            table.image(status.uiIcon).size(18f);
            table.add(" [accent]" + status.localizedName + "[] " + (int) (statusDuration / 60) + " " + Core.bundle.get("unit.seconds"));
        });
    }

    public class olWallBuild extends WallBuild {

        @Override
        public void update() {
            super.update();
            if (Mathf.chanceDelta(dynamicEffectChance)) {
                dynamicEffect.at(x + Mathf.range(size * 3f), y + Mathf.range(size * 3));
            }
        }

        @Override
        public boolean collide(Bullet other) {
            if (other.type.speed > 0.01f && other.team != team) {
                other.hit = false;
                other.type.despawnEffect.at(other.x, other.y, other.rotation(), other.type.hitColor);

                damage(other.damage);
                hit = 1f;

                other.remove();
                return false;
            }
            return super.collide(other);
        }

        public void reactTo(Unit unit) {
            unit.apply(status, statusDuration);
            float angle = angleTo(unit);
            Tmp.v1.trns(angle, size * tilesize / 2f).add(this);
            shotEffect.at(Tmp.v1.x, Tmp.v1.y, angle, status.color);
        }

        @Override
        public boolean collision(Bullet bullet) {
            if (bullet.team != team && (bullet.owner instanceof Unit)) reactTo((Unit) bullet.owner);
            return super.collision(bullet);
        }

        @Override
        public void drawLight() {
            super.drawLight();
            Drawf.light(x, y, 12f * size, status.color, 0.2f);
        }
    }
}