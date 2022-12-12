package ol.world.blocks.pressure;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.struct.FloatSeq;
import arc.struct.ObjectMap;
import arc.util.Nullable;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.content.Fx;
import mindustry.core.GameState;
import mindustry.entities.Effect;
import mindustry.entities.TargetPriority;
import mindustry.gen.Building;
import mindustry.graphics.Layer;
import mindustry.ui.Bar;
import mindustry.world.Block;
import mindustry.world.meta.BlockGroup;
import ol.content.OlFx;
import ol.content.blocks.OlDistribution;
import ol.world.meta.OlStat;
import ol.world.meta.OlStatUnit;

import static mindustry.Vars.state;
import static mindustry.Vars.world;
import static ol.graphics.OlPal.*;

public class PressurePipe extends Block implements PressureReplaceable, RegionAble {
    public final ObjectMap<String, TextureRegion> cache = new ObjectMap<>();
    public @Nullable Block junctionReplacement = OlDistribution.pressureJunction;

    public int tier = -1;
    /**draw connections?*/
    public boolean mapDraw = true;
    /**max pressure that can store block. if pressure is bigger when boom*/
    public float maxPressure;
    /**when pressure need in the block to alert*/
    public float dangerPressure = -1;
    /**if false when conduit sandbox block*/
    public boolean canExplode = true;
    public Effect explodeEffect = Fx.none;
    int timer = timers++;

    public void drawT(int x, int y, int rotation) {
        TextureRegion pressureIcon = Core.atlas.find("ol-pressure-icon");

        float dx = x * 8;
        float dy = y * 8;
        float ds = size * 8;

        if(rotation == 1 || rotation == 3) {
            Draw.rect(pressureIcon, dx, dy + ds, -90);
            Draw.rect(pressureIcon, dx, dy - ds, 90);
        }

        if(rotation == 0 || rotation == 2) {
            Draw.rect(pressureIcon, dx + ds, dy, 180);
            Draw.rect(pressureIcon, dx - ds, dy, 0);
        }
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        super.drawPlace(x, y, rotation, valid);
        drawT(x, y, rotation);
    }

    public PressurePipe(String name) {
        super(name);

        conveyorPlacement = underBullets = rotate = update = solid = true;
        drawArrow = false;
        group = BlockGroup.power;
        priority = TargetPriority.transport;
    }

    @Override
    public void setStats() {
        super.setStats();

        if(canExplode) {
            stats.add(OlStat.maxPressure, maxPressure, OlStatUnit.pressure);
        }
    }
    @Override
    public void setBars() {
        super.setBars();

        addBar("pressure", (PressurePipeBuild b) -> {
            float pressure = b.pressure / maxPressure;

            return new Bar(
                    () -> Core.bundle.get("bar.pressure") + " " + (int) (b.pressure),
                    () -> mixcol(oLPressureMin, oLPressure, pressure),
                    () -> pressure
            );
        });
    }

    @Override
    public boolean canReplace(Block other) {
        boolean valid = true;
        if(other instanceof PressurePipe pipe) {
            valid = pipe.tier == tier || pipe.tier == -1 || tier == -1;
        }

        return canBeReplaced(other) && valid;
    }

    @Override
    public String name() {
        return name;
    }

    public class PressurePipeBuild extends Building implements PressureAble {
        public float pressure;
        public static float dt = 0;

        @Override
        public int tier() {
            return tier;
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.f(pressure);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            pressure = read.f();
        }

        public float sumx(FloatSeq arr) {
            return Math.max(arr.sum(), 0);
        }

        @Override
        public void updateTile() {
            dt++;
            if(dt >= 60) {
                dt = 0;
            }

            onUpdate(canExplode, maxPressure, explodeEffect);
        }
        public boolean avalible(Building b) {
            return b instanceof PressureAble && inNet(b, false);
        }

        @Override
        @SuppressWarnings("unchecked")
        public PressurePipeBuild self() {
            return this;
        }

        @Override
        public float pressure() {
            return pressure;
        }

        @Override
        public void pressure(float pressure) {
            this.pressure = pressure;
        }

        /**pipes name based on connections <name>-[L][R][T][B]

        example: if connected to all sides when loaded 1111
                if connected only right when loaded 0100
                ...
                if connected only right and left when loaded 1100...*/

        @Override
        public void draw() {
            if(mapDraw) {
                int tx = tileX();
                int ty = tileY();

                Building left   = world.build(tx - 1, ty);
                Building right  = world.build(tx + 1, ty);
                Building bottom = world.build(tx, ty - 1);
                Building top    = world.build(tx, ty + 1);

                boolean bLeft   = avalible(left)   || left    instanceof PressureJunction.PressureJunctionBuild;
                boolean bRight  = avalible(right)  || right   instanceof PressureJunction.PressureJunctionBuild;
                boolean bTop    = avalible(top)    || top     instanceof PressureJunction.PressureJunctionBuild;
                boolean bBottom = avalible(bottom) || bottom  instanceof PressureJunction.PressureJunctionBuild;

                int l = bLeft   ? 1 : 0;
                int r = bRight  ? 1 : 0;
                int t = bTop    ? 1 : 0;
                int b = bBottom ? 1 : 0;

                String sprite = "-" + l + "" + r + "" + t + "" + b;

                if(cache.get(sprite) == null) {
                    cache.put(sprite, loadRegion(sprite));
                }
                float random = Mathf.random(-3, 3);

                if(pressure > maxPressure && canExplode) {

                    if(state.is(GameState.State.paused)) {
                        Draw.rect(cache.get(sprite), this.x, this.y);
                    } else {
                        Draw.rect(cache.get(sprite), this.x, this.y, random);
                    }
                    if(timer(PressurePipe.this.timer, Mathf.random(35, 65)))OlFx.pressureDamage.at(x + random/2, y + random/2, this.totalProgress() * random, Layer.blockUnder);
                } else {
                    Draw.rect(cache.get(sprite), this.x, this.y);
                }
            } else {
                super.draw();
            }

            this.drawTeamTop();
        }

        @Override
        public void onDestroyed() {
            super.onDestroyed();
            onUpdate(false, maxPressure, explodeEffect);
        }
    }
}