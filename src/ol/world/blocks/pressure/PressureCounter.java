package ol.world.blocks.pressure;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import mindustry.core.GameState;
import mindustry.gen.Building;
import mindustry.graphics.Layer;
import mindustry.world.Tile;
import ol.world.blocks.RegionAble;

import static mindustry.Vars.state;
import static mindustry.Vars.world;

public class PressureCounter extends PressurePipe implements RegionAble {
    public TextureRegion arrowRegion;

    public PressureCounter(String name) {
        super(name);
        mapDraw = true;
    }

    @Override
    public void load() {
        super.load();
        arrowRegion = Core.atlas.find(name + "-arrow");
        uiIcon = Core.atlas.find(name + "-icon");
    }

    @Override
    public String name() {
        return name;
    }

    public class PressureCounterBuild extends PressurePipeBuild {

        public boolean isDanger() {
            if(dangerPressure == -1) {
                return false;
            }

            return pressure > dangerPressure && canExplode;
        }

        public float angle() {
            if(!isDanger()){
                return ((pressure / dangerPressure) * 360);
            }
            return this.totalProgress() * pressure;
        }

        public boolean visibleArrow() {
            return true;
        }

        @Override
        public void draw() {
            super.draw();
            drawArrow();
        }

        public void drawArrow() {
            if(visibleArrow()) {
                Draw.draw(Layer.blockBuilding + 5, () -> {
                    float angle = angle();
                    if(!state.is(GameState.State.paused)) {
                        Draw.rect(arrowRegion, x, y, ((angle / 360) * -180) + Mathf.random(pressure, angle) / 10);
                    } else {
                        Draw.rect(arrowRegion, x, y, ((angle / 360) * -180));
                    }
                    Draw.reset();
                });
            }
        }

        @Override
        public float drawrot() {
            if(!quickRotate || !rotate || alignX(rotation)) {
                return 0;
            }

            if(alignY(rotation)) {
                return -90;
            }

            return 0;
        }

        @Override
        public boolean inNet(Building b, PressureAble<?> p, boolean junction) {
            Building self = self();
            int delta = 1;
            if(junction) {
                delta++;
            }

            int tx = self.tileX();
            int ty = self.tileY();

            Tile left = world.tile(tx - delta, ty);
            Tile right = world.tile(tx + delta, ty);

            if((left.build == b || right.build == b) && !alignX(rotation)) {
                return false;
            }

            Tile top = world.tile(tx, ty + delta);
            Tile bottom = world.tile(tx, ty - delta);

            if((top.build == b || bottom.build == b) && !alignY(rotation)) {
                return false;
            }

            return p.online() && (p.tier() == -1 || p.tier() == tier());
        }
    }
}