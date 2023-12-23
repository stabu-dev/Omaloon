package omaloon.world.blocks.distribution;

import arc.func.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.distribution.*;
import omaloon.content.blocks.*;
import omaloon.utils.*;

import static arc.Core.*;
import static mindustry.Vars.*;
import static omaloon.utils.OlUtils.*;

//TODO topRegion for planning
public class TubeConveyor extends Conveyor{
    private static final float itemSpace = 0.4f;
    public static final int[][] tiles = new int[][]{
            {},
            {0, 2}, {1, 3}, {0, 1},
            {0, 2}, {0, 2}, {1, 2},
            {0, 1, 2}, {1, 3}, {0, 3},
            {1, 3}, {0, 1, 3}, {2, 3},
            {0, 2, 3}, {1, 2, 3}, {0, 1, 2, 3}
    };

    public TextureRegion[][] topRegion;
    public TextureRegion[] capRegion;
    public Block junctionReplacement;

    public TubeConveyor(String name){
        super(name);
    }

    @Override
    public void init(){
        super.init();

        if(junctionReplacement == null) junctionReplacement = OlDistributionBlocks.tubeJunction;
    }

    @Override
    public Block getReplacement(BuildPlan req, Seq<BuildPlan> plans){
        if (junctionReplacement == null) return this;

        Boolf<Point2> cont = p -> plans.contains(o -> o.x == req.x + p.x && o.y == req.y + p.y
                && (req.block instanceof TubeConveyor || req.block instanceof Junction));
        return cont.get(Geometry.d4(req.rotation))
                && cont.get(Geometry.d4(req.rotation - 2))
                && req.tile() != null
                && req.tile().block() instanceof TubeConveyor
                && Mathf.mod(req.tile().build.rotation - req.rotation, 2) == 1
                ? junctionReplacement
                : this;
    }

    @Override
    public void drawPlanRegion(BuildPlan req, Eachable<BuildPlan> list){
        super.drawPlanRegion(req, list);
        BuildPlan[] directionals = new BuildPlan[4];
        list.each(other -> {
            if(other.breaking || other == req) return;

            int i = 0;
            for(Point2 point : Geometry.d4){
                int x = req.x + point.x, y = req.y + point.y;
                if(x >= other.x -(other.block.size - 1) / 2 && x <= other.x + (other.block.size / 2) && y >= other.y -(other.block.size - 1) / 2 && y <= other.y + (other.block.size / 2)){
                    if (other.block instanceof Conveyor ?
                      (req.rotation == i || (other.rotation + 2) % 4 == i) :
                      (
                        (req.rotation == i && other.block.acceptsItems) ||
                        (req.rotation != i && other.block.outputsItems())
                      )
                    ) {
                        directionals[i] = other;
                    }
                }
                i++;
            }
        });

        int mask = 0;
        for(int i = 0; i < directionals.length; i++) {
            if (directionals[i] != null) {
                mask += (1 << i);
            }
        }
        mask |= (1 << req.rotation);
        Draw.rect(topRegion[0][mask], req.drawx(), req.drawy(), 0);
        for(int i : tiles[mask]){
            if(
              directionals[i] == null ||
                (directionals[i].block instanceof Conveyor ?
                  (directionals[i].rotation + 2) % 4 == req.rotation :
                  (
                    (req.rotation == i && !directionals[i].block.acceptsItems) ||
                    (req.rotation != i && !directionals[i].block.outputsItems())
                  )
                )
            ){
                int id = i == 0 || i == 3 ? 1 : 0;
                Draw.rect(capRegion[id], req.drawx(), req.drawy(), i == 0 || i == 2 ? 0 : -90);
            }
        }
    }

    @Override
    public void load(){
        super.load();
        topRegion = OlUtils.splitLayers(name + "-sheet", 32, 2);
        capRegion = new TextureRegion[] { topRegion[1][0], topRegion[1][1] };
        uiIcon = atlas.find(name + "-icon");
    }

    @Override
    public TextureRegion[] icons(){
        return new TextureRegion[]{atlas.find(name + "-icon-editor")};
    }

    public class TubeConveyorBuild extends ConveyorBuild{
        public int tiling = 0;
        public int calls = 0;

        @Override
        public void updateTile(){
            minitem = 1f;
            mid = 0;

            //skip updates if possible
            if(len == 0){
                clogHeat = 0f;
                sleep();
                return;
            }

            float nextMax = aligned ? 1f - Math.max(itemSpace - nextc.minitem, 0) : 1f;

            if(isEnd(rotation)){
                nextMax = Math.min(nextMax, 1f - itemSpace);
            }

            if(isEnd(reverse(rotation)) && blendbits == 0){
                float nextMaxReverse = aligned ? (items.total() > 2 ? (0.5f - Math.max(itemSpace - nextc.minitem, 0))
                        : Math.max(itemSpace - nextc.minitem, 0)) : 0f;

                float movedReverse = speed * edelta();

                for(int i = 0; i < len; i++){
                    float nextposReverse = (i == 0 ? 0f : ys[i - 1]) + itemSpace;
                    float maxmoveReverse = Mathf.clamp(ys[i] - nextposReverse, 0, movedReverse);

                    ys[i] += maxmoveReverse;

                    if(ys[i] < nextMaxReverse) ys[i] = nextMaxReverse;
                    if(ys[i] < minitem) minitem = ys[i];
                }
            }

            float moved = speed * edelta();

            for(int i = len - 1; i >= 0; i--){
                float nextpos = (i == len - 1 ? 100f : ys[i + 1]) - itemSpace;
                float maxmove = Mathf.clamp(nextpos - ys[i], 0, moved);

                ys[i] += maxmove;

                if(ys[i] > nextMax) ys[i] = nextMax;
                if(ys[i] > 0.5 && i > 0) mid = i - 1;
                xs[i] = Mathf.approach(xs[i], 0, moved * 2);

                if(isEnd(rotation) && isEnd(reverse(rotation)) && items.total() > 1 && calls > 0){
                    //remove last item
                    items.remove(ids[i], len - i);
                    len = Math.min(i, len);
                }

                if(ys[i] >= 1f && pass(ids[i])){
                    // align X position if passing forwards
                    if(aligned){
                        nextc.xs[nextc.lastInserted] = xs[i];
                    }
                    //remove last item
                    items.remove(ids[i], len - i);
                    len = Math.min(i, len);
                }else if(ys[i] < minitem){
                    minitem = ys[i];
                }
            }

            if(minitem < itemSpace + (blendbits == 1 ? 0.3f : 0f)
                    || isEnd(reverse(rotation)) && items.total() >= 2
                    || isEnd(reverse(rotation)) && isEnd(rotation) && items.total() >= 1){
                clogHeat = Mathf.approachDelta(clogHeat, 1f, 1f / 60f);
            }else{
                clogHeat = 0f;
            }

            noSleep();
        }

        public void updateProximity() {
            super.updateProximity();
            calls++;
        }

        public Building buildAt(int i){
            return nearby(i);
        }

        public boolean valid(int i){
            Building b = buildAt(i);
            return b != null && (b instanceof TubeConveyorBuild ? (b.front() != null && b.front() == this)
                    : b.block.acceptsItems);
        }

        public boolean isEnd(int i){
            var b = buildAt(i);
            return !valid(i) && (b == null ? null : b.block) != this.block;
        }

        @Override
        public void draw(){
            int frame = enabled && clogHeat <= 0.5f ? (int) (((Time.time * speed * 8f * timeScale * efficiency)) % 4) : 0;

            //draw extra conveyors facing this one for non-square tiling purposes
            Draw.z(Layer.blockUnder);
            for(int i = 0; i < 4; i++){
                if((blending & (1 << i)) != 0){
                    int dir = rotation - i;
                    float rot = i == 0 ? rotation * 90 : (dir) * 90;

                    Draw.rect(sliced(regions[0][frame], i != 0 ? SliceMode.bottom : SliceMode.top),
                            x + Geometry.d4x(dir) * tilesize * 0.75f, y + Geometry.d4y(dir) * tilesize * 0.75f,
                            rot);
                }
            }

            Draw.z(Layer.block - 0.25f);

            Draw.rect(regions[blendbits][frame], x, y, tilesize * blendsclx, tilesize * blendscly, rotation * 90);

            Draw.z(Layer.block - 0.2f);
            float layer = Layer.block - 0.2f, wwidth = world.unitWidth(), wheight = world.unitHeight(),
                    scaling = 0.01f;

            for(int i = 0; i < len; i++){
                Item item = ids[i];
                Tmp.v1.trns(rotation * 90, tilesize, 0);
                Tmp.v2.trns(rotation * 90, -tilesize / 2f, xs[i] * tilesize / 2f);

                float ix = (x + Tmp.v1.x * ys[i] + Tmp.v2.x), iy = (y + Tmp.v1.y * ys[i] + Tmp.v2.y);

                //keep draw position deterministic.
                Draw.z(layer + (ix / wwidth + iy / wheight) * scaling);
                Draw.rect(item.fullIcon, ix, iy, itemSize, itemSize);
            }

            Draw.z(Layer.block - 0.15f);
            Draw.rect(topRegion[0][tiling], x, y, 0);
            int[] placementID = tiles[tiling];
            for(int i : placementID){
                if(isEnd(i)){
                    int id = i == 0 || i == 3 ? 1 : 0;
                    Draw.rect(capRegion[id], x, y, i == 0 || i == 2 ? 0 : -90);
                }
            }
        }

        @Override
        public void drawCracks(){
            Draw.z(Layer.block);
            super.drawCracks();
        }

        @Override
        public int acceptStack(Item item, int amount, Teamc source){
            if(isEnd(reverse(rotation)) && items.total() >= 2) return 0;
            if(isEnd(reverse(rotation)) && isEnd(rotation) && items.total() >= 1) return 0;
            return Math.min((int)(minitem / itemSpace), amount);
        }

        @Override
        public void unitOn(Unit unit){
        }

        @Override
        public void onProximityUpdate(){
            super.onProximityUpdate();
            noSleep();
            next = front();
            nextc = next instanceof TubeConveyorBuild d ? d : null;

            tiling = 0;
            for(int i = 0; i < 4; i++){
                if(i == rotation || valid(i)){
                    tiling |= (1 << i);
                }
            }
        }
    }
}
