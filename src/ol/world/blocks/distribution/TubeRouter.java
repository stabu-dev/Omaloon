package ol.world.blocks.distribution;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.geom.Geometry;
import arc.util.Log;
import me13.core.flywheel.FlyDraw;
import mindustry.Vars;
import mindustry.world.blocks.distribution.Router;

public class TubeRouter extends Router {
    public TextureRegion gearRegion, bottomRegion, arrowRegion;

    public TubeRouter(String name) {
        super(name);
    }

    public TextureRegion loadRegion(String prefix) {
        return Core.atlas.find(name + prefix);
    }

    @Override
    public void load() {
        super.load();
        gearRegion = loadRegion("-gear");
        bottomRegion = loadRegion("-bottom");
        arrowRegion = loadRegion("-arrow");
    }

    @Override
    protected TextureRegion[] icons() {
        return new TextureRegion[] {bottomRegion, loadRegion("-gears"), region};
    }

    public class TubeRouterBuild extends RouterBuild {
        public float rot = 0;

        @Override
        public void updateTile() {
            super.updateTile();
            if(!Vars.state.isPaused()) {
                rot += 3;
            }
        }

        @Override
        public void draw() {
            Draw.rect(bottomRegion, x, y);
            final float mn = 1.25f; //MN = Magic Number
            FlyDraw.drawSpin(gearRegion, this, mn, -mn, rot, 0);
            FlyDraw.drawSpin(gearRegion, this, -mn, mn, -rot + 45, 0);
            Draw.rect(region, x, y);
            Draw.rect(arrowRegion, x, y, rotation * 90);
        }
    }
}