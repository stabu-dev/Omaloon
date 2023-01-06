package ol.world.blocks.pressure;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;

import mindustry.annotations.Annotations.*;
import mindustry.core.*;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.*;

import mma.type.*;
import mma.type.pixmap.*;
import ol.world.blocks.*;

import static mindustry.Vars.*;

public class PressureCounter extends PressurePipe implements ImageGenerator{
    @Load("@-arrow")
    public TextureRegion arrowRegion;

    public PressureCounter(String name) {
        super(name);
        mapDraw = true;
    }

    @Override
    public boolean inBuildPlanNet(BuildPlan s, int x, int y, int ignored) {
        int ox = s.x - x;
        int oy = s.y - y;

        if(ox == 0 && oy == 0) {
            return true;
        }

        ox = ox < 0 ? -ox : ox;
        return (ox == 1) ? alignX(s.rotation) : alignY(s.rotation);
    }

    @Override
    public Pixmap generate(Pixmap icon, PixmapProcessor processor){
        PixmapProcessor.drawCenter(icon,processor.get(arrowRegion));
        return ImageGenerator.super.generate(icon, processor);
    }


    public class PressureCounterBuild extends PressurePipeBuild {

        public boolean isDanger() {
            if(dangerPressure == -1) {
                return false;
            }

            return pressure > dangerPressure && canExplode;
        }

        public float angle() {
            if(!isDanger()) {
                return ((pressure / dangerPressure) * 360);
            }

            return this.totalProgress() * pressure;
        }

        @Override
        public boolean avalibleX() {
            return this.alignX(this.rotation);
        }

        @Override
        public boolean avalibleY() {
            return this.alignY(this.rotation);
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