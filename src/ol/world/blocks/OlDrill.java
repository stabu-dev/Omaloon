package ol.world.blocks;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import mindustry.world.blocks.production.Drill;

public class OlDrill extends Drill {
    public TextureRegion teamRegion;
    public boolean isUnitDrill;

    public OlDrill(String name) {
        super(name);
        hasItems = true;
        acceptsItems = false;
        hasLiquids = false;
        liquidCapacity = 0f;
        outputsLiquid = false;
    }

    @Override
    public boolean isHidden() {
        return isUnitDrill;
    }

    @Override
    public boolean outputsItems() {
        return !isUnitDrill;
    }

    @Override
    public void load() {
        super.load();
        teamRegion = Core.atlas.find(name + "-team");
        uiIcon = Core.atlas.find(name + "-icon");
    }

    public class OlDrillBuild extends DrillBuild {
        @Override
        public void draw() {
            super.draw();
            if(teamRegion.found()) {
                Draw.color(team.color);
                Draw.rect(teamRegion, x, y, drawrot());
                Draw.reset();
            }
        }
    }
}