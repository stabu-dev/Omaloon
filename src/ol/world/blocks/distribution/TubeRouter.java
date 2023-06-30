package ol.world.blocks.distribution;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import me13.core.flywheel.FlyDraw;
import mindustry.Vars;
import mindustry.graphics.Drawf;
import mindustry.world.blocks.distribution.Router;

public class TubeRouter extends Router {
    public TextureRegion rotorRegion, bottomRegion;

    public TubeRouter(String name) {
        super(name);
    }

    public TextureRegion loadRegion(String prefix) {
        return Core.atlas.find(name + prefix);
    }

    @Override
    public void load() {
        super.load();
        rotorRegion = loadRegion("-rotor");
        bottomRegion = loadRegion("-bottom");
    }

    @Override
    protected TextureRegion[] icons() {
        return new TextureRegion[] {bottomRegion, rotorRegion, region};
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
            Drawf.spinSprite(rotorRegion, x, y, rotation());
            Draw.rect(region, x, y);
        }
    }
}