package ol.world.blocks.distribution;

import arc.func.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;

import me13.core.block.BlockAngles;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.graphics.Layer;
import mindustry.type.Item;
import mindustry.world.*;
import mindustry.world.blocks.distribution.*;

import ol.content.blocks.*;
import ol.utils.OlUtils;

import static arc.Core.atlas;
import static mindustry.Vars.*;
import static mindustry.Vars.itemSize;

public class TubeConveyor extends Conveyor {
    public static final int[][] tiles = new int[][] { new int[] {},
            new int[] {0, 2}, new int[] {1, 3}, new int[] {0, 1},
            new int[] {0, 2}, new int[] {0, 2}, new int[] {1, 2},
            new int[] {0, 1, 2}, new int[] {1, 3}, new int[] {0, 3},
            new int[] {1, 3}, new int[] {0, 1, 3}, new int[] {2, 3},
            new int[] {0, 2, 3}, new int[] {1, 2, 3}, new int[] {0, 1, 2, 3}
    };

    public TextureRegion[][] topRegion;
    public TextureRegion[] _arr_125832;
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
        _arr_125832 = new TextureRegion[] {topRegion[1][0], topRegion[1][1]};
        uiIcon = atlas.find(name + "-icon");
    }

    public class TubeConveyorBuild extends ConveyorBuild {
        public int tiling = 0;

        public Building buildAt(int i) {
            return nearby(Geometry.d4(i).x, Geometry.d4(i).y); //why not nearby(i)?
        }

        public boolean valid(int i) {
            Building b = buildAt(i);
            return b != null && (b instanceof TubeConveyorBuild ? (b.front() != null && b.front() == this) :
                    ((b.block.outputsItems() || b.block.acceptsItems) && !(b instanceof StackConveyor.StackConveyorBuild stack
                            && stack.state != 2)));
        }

        public boolean isEnd(int i) {
            var b = buildAt(i);
            return !valid(i) && (b == null ? null : b.block) != this.block;
        }

        @Override
        public void draw() {
            Draw.rect(topRegion[0][tiling], x, y, 0);
            int[] placementID = tiles[tiling];
            for(int i : placementID) {
                if(isEnd(i)) {
                    int id = i == 0 || i == 3 ? 1 : 0;
                    Draw.rect(_arr_125832[id], x, y, i == 0 || i == 2 ? 0 : -90);
                }
            }

            Draw.z(Layer.block + 0.1f);
            //-------------------------------
            int frame = enabled && clogHeat <= 0.5f ? (int)(((Time.time * speed * 8f * timeScale * efficiency)) % 4) : 0;

            //draw extra conveyors facing this one for non-square tiling purposes
            Draw.z(Layer.blockUnder);
            for(int i = 0; i < 4; i++){
                if((blending & (1 << i)) != 0){
                    int dir = rotation - i;
                    float rot = i == 0 ? rotation * 90 : (dir)*90;

                    Draw.rect(sliced(regions[0][frame], i != 0 ? SliceMode.bottom : SliceMode.top), x + Geometry.d4x(dir) * tilesize*0.75f, y + Geometry.d4y(dir) * tilesize*0.75f, rot);
                }
            }

            Draw.z(Layer.block - 0.2f);

            Draw.rect(regions[blendbits][frame], x, y, tilesize * blendsclx, tilesize * blendscly, rotation * 90);

            Draw.z(Layer.block - 0.1f);
            float layer = Layer.block - 0.1f, wwidth = world.unitWidth(), wheight = world.unitHeight(), scaling = 0.01f;
            float s = size * 4;
            for(int i = 0; i < len; i++){
                Item item = ids[i];
                Tmp.v1.trns(rotation * 90, tilesize, 0);
                Tmp.v2.trns(rotation * 90, -tilesize / 2f, xs[i] * tilesize / 2f);

                float
                        ix = (x + Tmp.v1.x * ys[i] + Tmp.v2.x),
                        iy = (y + Tmp.v1.y * ys[i] + Tmp.v2.y);

                float tmp1;
                if((isEnd(rotation) || isEnd(BlockAngles.reverse(rotation))) && (i == len - 1 || i == 0)) {
                    tmp1 = x + s - itemSize/2f;
                    if(ix > tmp1) {
                        ix = tmp1;
                    }
                    tmp1 = x - s + itemSize/2f;
                    if(ix < tmp1) {
                        ix = tmp1;
                    }

                    tmp1 = y + s - itemSize/2f;
                    if(iy > tmp1) {
                        iy = tmp1;
                    }
                    tmp1 = y - s + itemSize/2f;
                    if(iy < tmp1) {
                        iy = tmp1;
                    }
                }

                //keep draw position deterministic.
                Draw.z(layer + (ix / wwidth + iy / wheight) * scaling);
                Draw.rect(item.fullIcon, ix, iy, itemSize, itemSize);
            }
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
                if(i == rotation || valid(i)) {
                    tiling |= (1 << i);
                }
            }
        }
    }
}