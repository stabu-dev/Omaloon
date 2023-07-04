package ol.world.blocks.distribution;

import arc.func.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;

import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.graphics.Layer;
import mindustry.world.*;
import mindustry.world.blocks.distribution.*;
import ol.content.blocks.*;
import ol.utils.OlUtils;

import static arc.Core.atlas;

public class TubeConveyor extends Conveyor {
    public TextureRegion[][] topRegion;
    public Block junctionReplacement;

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
    }

    @Override
    public void load() {
        super.load();
        topRegion = OlUtils.splitLayers(name + "-sheet", 32, 2);
        uiIcon = atlas.find(name + "-icon");
    }

    public class TubeConveyorBuild extends ConveyorBuild {
        public boolean capped, backCapped = false;
        public int tiling = 0;

        @Override
        public void draw() {
            Draw.rect(topRegion[0][tiling], x, y, 0);
            Draw.z(Layer.block + 0.1f);
            super.draw();
        }

        @Override
        public void unitOn(Unit unit) {
        }

        @Override
        public void onProximityUpdate(){
            super.onProximityUpdate();
            noSleep();
            next = front();
            nextc = next instanceof TubeConveyorBuild d ? d : null;

            tiling = 0;

            for(int i = 0; i < 4; i++){
                Building b = nearby(Geometry.d4(i).x, Geometry.d4(i).y);
                if(i == rotation || b != null && (b instanceof TubeConveyorBuild ? (b.front() != null && b.front() == this) : (b.block.outputsItems() && !(b instanceof StackConveyor.StackConveyorBuild stack && stack.state != 2)))){
                    tiling |= (1 << i);
                }
            }
        }
    }
}