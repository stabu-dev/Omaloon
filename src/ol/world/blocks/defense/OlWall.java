package ol.world.blocks.defense;

import arc.*;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.math.geom.Point2;
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
import static mindustry.Vars.world;

public class OlWall extends Wall {
    public boolean canApplyStatus = false;
    /** status effect that is imposed on units when is hit a block.*/
    public StatusEffect status = StatusEffects.none;
    /** status effect duration.*/
    public float statusDuration = 0f;
    /** effect when block hitting.*/
    public Effect shotEffect = Fx.none;
    /** effect that appears on block.*/
    public Effect dynamicEffect = Fx.none;
    /** effect chance.*/
    public float dynamicEffectChance = 0f;
    /** if true draw light on block, light can not be null, set light color.*/
    public boolean drawDynamicLight = false;
    /** light color.*/
    public Color dynamicLightColor = null;
    /** dynamic light radius*/
    public float dynamicLightRadius = 0f;
    /** dynamic light opacity*/
    public float dynamicLightOpacity = 0f;
    /**if false block cannot burn*/
    public boolean canBurn = true;

    public OlWall(String name) {
        super(name);
        flashHit = true;
        flashColor = null;
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
                Fires.extinguish(world.tileWorld(x+2, y+2), intensity);

                for(Point2 p : Geometry.d8) {
                    Fires.extinguish(world.tileWorld(x + p.x * tilesize, y + p.y * tilesize), intensity);
                }
            }
        }

        @Override
        public void update() {
            super.update();

            if (Mathf.chanceDelta(dynamicEffectChance)) {
                dynamicEffect.at(x + Mathf.range(size * 3f), y + Mathf.range(size * 3));
            }
        }

        public void reactTo(Unit unit) {
            if (canApplyStatus) {
                unit.apply(status, statusDuration);
                float angle = angleTo(unit);
                Tmp.v1.trns(angle, size * tilesize / 2f).add(this);
            }
        }

        @Override
        public boolean collision(Bullet bullet) {
            if (bullet.type.speed < 0.01f && bullet.team != team && (bullet.owner instanceof Unit)) {
                reactTo((Unit) bullet.owner);
            }

            return super.collision(bullet);
        }

        @Override
        public void drawLight() {
            super.drawLight();

            if(drawDynamicLight) {
                Drawf.light(x, y, dynamicLightRadius * size, dynamicLightColor, dynamicLightOpacity);
            }
        }
    }
}