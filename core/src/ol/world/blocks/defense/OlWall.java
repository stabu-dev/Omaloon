package ol.world.blocks.defense;

import arc.*;
import arc.graphics.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;

import mindustry.content.*;
import mindustry.entities.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.blocks.defense.*;
import mindustry.world.meta.*;

import static mindustry.Vars.*;

public class OlWall extends Wall {
    public boolean canApplyStatus, canBurn, drawDynamicLight;
    public StatusEffect status = StatusEffects.none;
    public float statusDuration, dynamicEffectChance, dynamicLightRadius, dynamicLightOpacity;
    public Effect dynamicEffect = Fx.none;
    public Color dynamicLightColor;

    public OlWall(String name) {
        super(name);
        flashHit = true;
        update = true;
    }

    @Override
    public void setStats() {
        super.setStats();
        if(canApplyStatus) {
            stats.add(Stat.abilities, table -> {
                table.image(status.uiIcon).size(18f);
                table.add(" [accent]" + status.localizedName + "[] " + (int) (statusDuration / 60) + " " + Core.bundle.get("unit.seconds"));
            });
        }
    }

    public class OlWallBuild extends WallBuild {
        @Override
        public void updateTile() {
            if(!canBurn) {
                float intensity = 9000f;
                Fires.extinguish(world.tileWorld(x + 2, y + 2), intensity);

                for(Point2 p : Geometry.d8) {
                    Fires.extinguish(world.tileWorld(x + p.x * tilesize, y + p.y * tilesize), intensity);
                }
            }
        }

        @Override
        public void update() {
            super.update();
            if (Mathf.chanceDelta(dynamicEffectChance)) dynamicEffect.at(x + Mathf.range(size * 3f), y + Mathf.range(size * 3));
        }

        public void reactTo(Unit unit) {
            if (canApplyStatus) {
                unit.apply(status, statusDuration);
                Tmp.v1.trns(angleTo(unit), size * tilesize / 2f).add(this);
            }
        }

        @Override
        public boolean collision(Bullet bullet) {
            if (bullet.type.speed < 0.01f && bullet.team != team && (bullet.owner instanceof Unit)) reactTo((Unit) bullet.owner);
            return super.collision(bullet);
        }

        @Override
        public void drawLight() {
            super.drawLight();
            if(drawDynamicLight) Drawf.light(x, y, dynamicLightRadius * size, dynamicLightColor, dynamicLightOpacity);
        }
    }
}
