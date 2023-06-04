package ol.world.blocks.distribution;

import arc.*;
import arc.func.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;

import me13.core.layers.*;
import me13.core.layers.layout.*;

import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.world.*;
import mindustry.world.blocks.distribution.*;

import net.tmmc.util.*;

import ol.content.blocks.*;

import org.jetbrains.annotations.*;

import java.util.*;

public class TubeConveyor extends Conveyor implements ILayerBlock {
    public Block junctionReplacement;

    public List<ILayer> layerList = List.of(new DrawAtlas() {{
        boolfHeme = (tile, self, other) -> {
            if (other != null && other.block instanceof Conveyor) {
                int r = self.x == other.x ? (self.y > other.y ? 3 : 1) : (self.x > other.x ? 2 : 0);
                int r2 = self.x == other.x ? (self.y > other.y ? 1 : 3) : (self.x > other.x ? 0 : 2);
                return self.rotation == r || other.rotation == r2;
            }
            return false;
        };
        boolf = (tile, self) -> {
            Building building = XBlocks.of(tile);
            if (building instanceof TubeConveyorBuild) {
                boolean bool = building.nearby(building.rotation) == self;
                int oppositeRotation = switch (building.rotation) {
                    case 0 -> 2;
                    case 1 -> 3;
                    case 2 -> 0;
                    case 3 -> 1;
                    default -> throw new IllegalStateException();
                };
                return (building.nearby(oppositeRotation) == self && bool) || self.nearby(self.rotation) == building || (self.nearby(oppositeRotation) == building && bool) || bool;
            } else {
                return building != null && building.block.acceptsItems;
            }
        };
        prefix = "-top";
    }});

    public TubeConveyor(String name) {
        super(name);
    }

    @Override
    public void init() {
        super.init();

        if (junctionReplacement == null) junctionReplacement = OlDistributionBlocks.tubeJunction;
    }

    @Override
    public Block getReplacement(BuildPlan req, Seq<BuildPlan> plans) {
        if (junctionReplacement == null) return this;

        Boolf<Point2> cont = p -> plans.contains(o -> o.x == req.x + p.x && o.y == req.y + p.y && (req.block instanceof TubeConveyor || req.block instanceof Junction));
        return cont.get(Geometry.d4(req.rotation)) &&
                cont.get(Geometry.d4(req.rotation - 2)) &&
                req.tile() != null &&
                req.tile().block() instanceof TubeConveyor &&
                Mathf.mod(req.tile().build.rotation - req.rotation, 2) == 1 ? junctionReplacement : this;
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list) {
        super.drawPlanRegion(plan, list);
        drawPlanLayers(this, plan, list);
    }

    @Override
    public @NotNull List<ILayer> getLayers() {
        return layerList == null ? List.of() : layerList;
    }

    @Override
    public void load() {
        super.load();
        loadLayers(this);
        uiIcon = Core.atlas.find(name + "-icon");
    }

    public class TubeConveyorBuild extends ConveyorBuild implements ILayerBuilding {
        @Override
        public void draw() {
            super.draw();
            draw(getLayers(), TubeConveyor.this, this);
        }

        @Override
        public void unitOn(Unit unit) {
        }
    }
}