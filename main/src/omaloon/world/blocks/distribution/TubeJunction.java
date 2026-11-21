package omaloon.world.blocks.distribution;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.distribution.*;
import omaloon.annotations.Annotations.*;
import omaloon.content.blocks.*;
import omaloon.graphics.*;

public class TubeJunction extends Junction{
    private static final float itemSpace = 0.4f;
    private static final int inputPort = 1, outputPort = 2;

    public @Load(value = "@-side#0$", lengths = {2}) TextureRegion[] sideRegion;
    public @Load("@-bottom") TextureRegion bottomRegion;

    public TubeJunction(String name){
        super(name);
        capacity = 3;
        squareSprite = false;
        drawCached = false;
        drawDynamic = true;
    }

    @Override
    public void loadIcon(){
        super.loadIcon();
        uiIcon = Core.atlas.find(name + "-ui", uiIcon);
    }

    private int portFlags(Block block, int rotation, int side){
        if(block == null) return 0;
        int facing = (side + 2) % 4;
        return (block.outputsItems() && (!(block instanceof Conveyor) || rotation == facing) ? inputPort : 0) |
        (block.acceptsItems && (!block.rotate || rotation != facing) ? outputPort : 0);
    }

    private boolean connected(int first, int second){
        return ((first & inputPort) != 0 && (second & outputPort) != 0) ||
        ((second & inputPort) != 0 && (first & outputPort) != 0);
    }

    private int openSides(int east, int north, int west, int south){
        return (connected(east, west) ? 5 : 0) | (connected(north, south) ? 10 : 0);
    }

    private int planPortFlags(BuildPlan plan, Eachable<BuildPlan> plans, int side){
        int dx = Geometry.d4x(side), dy = Geometry.d4y(side);
        for(int x = plan.x + dx, y = plan.y + dy; ; x += dx, y += dy){
            int px = x, py = y;
            BuildPlan other = findPlan(plans, x, y, p -> {
                int size = p.block == null ? 1 : p.block.size;
                return p != plan && px >= p.x - (size - 1) / 2 && px <= p.x + size / 2 &&
                py >= p.y - (size - 1) / 2 && py <= p.y + size / 2;
            });
            if(other != null && !other.breaking && other.block instanceof Junction) continue;
            if(other != null) return other.breaking ? 0 : portFlags(other.block, other.rotation, side);

            Building build = plan.worldContext ? Vars.world.build(x, y) : null;
            if(build instanceof JunctionBuild) continue;
            return build == null ? 0 : portFlags(build.block, build.rotation, side);
        }
    }

    private void drawFrame(float x, float y, int openSides){
        Draw.z(Layer.block);
        Draw.rect(region, x, y);
        for(int side = 0; side < 4; side++)
            if((openSides & (1 << side)) == 0) Draw.rect(sideRegion[side / 2], x, y, side * 90f);
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> plans){
        Draw.z(Layer.blockUnder);
        Draw.rect(bottomRegion, plan.drawx(), plan.drawy());
        drawFrame(plan.drawx(), plan.drawy(), openSides(
        planPortFlags(plan, plans, 0), planPortFlags(plan, plans, 1),
        planPortFlags(plan, plans, 2), planPortFlags(plan, plans, 3)
        ));
    }

    public class TubeJunctionBuild extends JunctionBuild{
        private final int[] directions = {-1, -1};
        private final int[] previousCounts = new int[4];
        private final float[] clogHeat = new float[2];
        private final float[] minItemPosition = {1f, 1f, 1f, 1f};

        private int endpointFlags(int side){
            Building next = nearby(side);
            while(next instanceof JunctionBuild && next != this && next.team == team){
                next = next.nearby(side);
            }
            if(next == null || next == this || next.team != team) return 0;
            return TubeJunction.this.portFlags(next.block, next.rotation, side);
        }

        private int laneCapacity(int side){
            boolean through = (endpointFlags((side + 2) % 4) & inputPort) != 0 &&
            (endpointFlags(side) & outputPort) != 0;
            return Math.min(through ? 3 : 2, capacity);
        }

        private float progress(long entry){
            float elapsed = Time.time - BufferItem.time(entry);
            return elapsed < 0f ? 1f : Mathf.clamp(elapsed * timeScale / speed);
        }

        private float endPosition(int side){
            Building next = nearby(side);
            if(next == null || next.team != team) return 1f;

            float space = next instanceof TubeJunctionBuild junction ? junction.minItemPosition[side] : 1f;
            if(next instanceof Conveyor.ConveyorBuild conveyor){
                if(next.rotation == side){
                    space = conveyor.minitem;
                }else if((next.rotation + 2) % 4 != side){
                    int sign = (side + 4 - next.rotation) % 4 == 1 ? -1 : 1;
                    for(int i = 0; i < conveyor.len; i++){
                        space = Math.min(space, (1f - sign * conveyor.xs[i]) / 2f);
                    }
                }
            }

            return 1f - Math.max(itemSpace - space, 0f);
        }

        private float freeSpace(int side){
            if(buffer.indexes[side] == 0) return 1f;

            float position = endPosition(side);
            for(int i = 0; i < buffer.indexes[side]; i++)
                position = Math.min(position, progress(buffer.buffers[side][i])) - itemSpace;
            return position;
        }

        @Override
        public boolean acceptItem(Building source, Item item){
            int side = source.relativeTo(tile);
            return side >= 0 && source.team == team && buffer.accepts(side) && (endpointFlags(side) & outputPort) != 0 &&
            buffer.indexes[side] < laneCapacity(side) && freeSpace(side) >= 0f;
        }

        @Override
        public void handleItem(Building source, Item item){
            int side = source.relativeTo(tile);
            super.handleItem(source, item);
            directions[side % 2] = side;
            minItemPosition[side] = 0f;
        }

        private void settleQueue(int side, float duration){
            float limit = Math.min(1f - itemSpace, endPosition(side));
            for(int i = 0; i < buffer.indexes[side]; i++){
                long entry = buffer.buffers[side][i];
                float position = Math.min(progress(entry), limit);
                buffer.buffers[side][i] = BufferItem.get(BufferItem.item(entry), Time.time - position * duration);
                limit = position - itemSpace;
            }
        }

        @Override
        public void updateTile(){
            System.arraycopy(buffer.indexes, 0, previousCounts, 0, previousCounts.length);
            super.updateTile();

            float duration = speed / Math.max(timeScale, 0.0001f);
            for(int axis = 0; axis < 2; axis++){
                boolean blocked = false;
                for(int side = axis; side < 4; side += 2){
                    int count = buffer.indexes[side];
                    if(count < previousCounts[side]) settleQueue(side, duration);

                    float space = freeSpace(side);
                    minItemPosition[side] = Mathf.clamp(space + itemSpace);
                    if(count > 0){
                        int max = laneCapacity(side);
                        blocked |= (count >= max && max < capacity) || space < 0f;
                    }
                }

                clogHeat[axis] = blocked ? Mathf.approachDelta(clogHeat[axis], 1f, 1f / 60f) : 0f;
            }
        }

        private TextureRegion beltRegion(int side, TubeConveyor tube){
            Building reference = nearby(side) instanceof TubeConveyor.TubeConveyorBuild conveyor ? conveyor : this;
            boolean stopped = !reference.enabled || Vars.state.rules.editor || clogHeat[side % 2] > 0.5f ||
            (reference instanceof TubeConveyor.TubeConveyorBuild conveyor && conveyor.clogHeat > 0.5f);
            int frame = stopped ? 0 : (int)((Time.time * tube.speed * 8f * reference.timeScale() * reference.efficiency) % 4);
            return tube.regions[0][frame];
        }

        private void drawBelts(int openSides){
            TubeConveyor tube = (TubeConveyor)OlDistributionBlocks.tubeConveyor;
            float offset = Vars.tilesize * 7f / 16f;
            Draw.z(Layer.blockUnder + 0.001f);

            for(int side = 0; side < 4; side++){
                int direction = directions[side % 2];
                if((openSides & (1 << side)) == 0 || direction < 0) continue;

                TextureRegion belt = beltRegion(side, tube);
                TextureRegion edge = OlDraw.slice(belt, side == direction ? belt.width - 4 : 0, 0, 4, belt.height);
                Draw.rect(edge, x + Geometry.d4x(side) * offset, y + Geometry.d4y(side) * offset,
                Vars.tilesize / 8f, Vars.tilesize, direction * 90f);
            }
        }

        private void drawItems(){
            for(int side = 0; side < 4; side++){
                float limit = endPosition(side);
                for(int i = 0; i < buffer.indexes[side] && limit >= 0f; i++){
                    long entry = buffer.buffers[side][i];
                    Item item = Vars.content.item(BufferItem.item(entry));
                    if(item == null) continue;

                    float position = Math.min(progress(entry), limit);
                    limit = position - itemSpace;
                    float offset = (position - 0.5f) * Vars.tilesize;
                    float ix = x + Geometry.d4x(side) * offset, iy = y + Geometry.d4y(side) * offset;
                    Draw.z(Layer.block - 0.1f + (ix / Vars.world.unitWidth() + iy / Vars.world.unitHeight()) * 0.01f);
                    Draw.rect(item.fullIcon, ix, iy, Vars.itemSize, Vars.itemSize);
                }
            }
        }

        @Override
        public void draw(){
            int sides = openSides(endpointFlags(0), endpointFlags(1), endpointFlags(2), endpointFlags(3));

            Draw.z(Layer.blockUnder);
            Draw.rect(bottomRegion, x, y);
            drawBelts(sides);
            drawFrame(x, y, sides);
            drawItems();
        }

        @Override
        public byte version(){ return 3; }

        @Override
        public void write(Writes write){
            super.write(write);
            for(int direction : directions) write.b(direction);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            for(int i = 0; i < directions.length; i++) directions[i] = read.b();
        }
    }
}