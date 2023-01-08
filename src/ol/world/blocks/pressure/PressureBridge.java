package ol.world.blocks.pressure;

import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.*;
import mindustry.annotations.Annotations.*;
import mindustry.core.*;
import mindustry.entities.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.input.*;
import mindustry.world.*;
import mindustry.world.meta.*;
import ol.gen.*;
import ol.graphics.*;
import ol.utils.*;
import ol.utils.pressure.*;
import ol.world.blocks.*;

import static arc.graphics.g2d.Draw.*;
import static mindustry.Vars.*;

public class PressureBridge extends PressureBlock implements PressureReplaceable{
    private static BuildPlan otherReq;

    @Load("@-bridge")
    public TextureRegion bridge;
    @Load("@-end")
    public TextureRegion bridgeEnd;
    @Load("@-end2")
    public TextureRegion bridgeEnd2;
    public float range = 2.5f * tilesize;

    public PressureBridge(String name){
        super(name);

        solid = true;
        configurable = true;
        update = true;
        underBullets = true;
        noUpdateDisabled = true;
        copyConfig = false;
        allowConfigInventory = false;
        priority = TargetPriority.transport;
        swapDiagonalPlacement = true;
        group = BlockGroup.transportation;

        config(Integer.class, (PressureBridgeBuild c, Integer link) -> {
            if(c.link == link){
                c.unlink();
                c.pressureNet.set(c);
                if(world.build(link) instanceof PressureAblec linkBuild){
                    PressureNet newNet = new PressureNet();
                    newNet.set(linkBuild);
                    PressureUpdater.nets.add(newNet);
                }
            }else{
                if(world.build(link) instanceof PressureAblec linkBuild){
                    PressureNet oldNet = linkBuild.pressureNet();
                    c.pressureNet.merge(oldNet);
                    PressureUpdater.nets.remove(oldNet);
                }
                c.link = link;
            }
//            Core.app.post(PressureRenderer::reload);
        });

        config(Point2.class, (PressureBridgeBuild tile, Point2 i) -> {
            configurations.get(Integer.class).get(tile, Point2.pack(i.x + tile.tileX(), i.y + tile.tileY()));
        });
    }

    public static boolean collision(float x1, float y1, float x2, float y2, float radius){
        return Mathf.within(x1, y1, x2, y2, radius + 1f);
    }

    public void drawBridge(Position self, Position other){
        float sx = self.getX(), sy = self.getY();
        float ox = other.getX(), oy = other.getY();

        float sa = self.angleTo(other);
        float oa = other.angleTo(self);

        boolean line = sx == ox || sy == oy;
        int segments = length(sx, sy, ox, oy) + 1;

        if(line){
            if(sy == oy){
                Position a = sx < ox ? other : self;
                Position b = sx < ox ? self : other;

                segments = (int)(a.getX() / 8 - b.getX() / 8);
            }

            if(sx == ox){
                Position a = sy < oy ? other : self;
                Position b = sy < oy ? self : other;

                segments = (int)(a.getY() / 8 - b.getY() / 8);
            }
        }

        float sl = 0;
        if(!line){
            sl = Mathf.dst(sx, sy, ox, oy) / segments;
        }

        Draw.alpha(Renderer.bridgeOpacity);

        OlGraphics.l(Layer.power - 5);
        Lines.stroke(4);
        boolean reverse = sx > ox;

        if(line){
            reverse |= sy < oy;
        }

        float r = sa + (reverse ? 180 : 0);

        TextureRegion end = reverse ? bridgeEnd2 : bridgeEnd;
        TextureRegion str = reverse ? bridgeEnd : bridgeEnd2;

        Draw.rect(end, sx, sy, sa);
        Draw.rect(str, ox, oy, oa);

        for(int i = 1; i < segments; i++){
            float s_x = Mathf.lerp(sx, ox, (float)i / segments);
            float s_y = Mathf.lerp(sy, oy, (float)i / segments);

            if(line){
                Draw.rect(bridge, s_x, s_y, r);
            }else{
                Draw.rect(bridge, s_x, s_y, sl, bridge.height * scl * xscl, r);
            }
        }

        Draw.reset();
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.add(Stat.linkRange, range / tilesize, StatUnit.blocks);
    }

    @Override
    public void drawPlanConfigTop(BuildPlan plan, Eachable<BuildPlan> list){
        otherReq = null;

        list.each(other -> {
            if(other.block == this && plan != other && plan.config instanceof Point2 p && p.equals(other.x - plan.x, other.y - plan.y)){
                otherReq = other;
            }
        });

        if(otherReq != null){
            drawBridge(plan, otherReq);
        }
    }

    @Override
    public void handlePlacementLine(Seq<BuildPlan> plans){
        for(int i = 0; i < plans.size - 1; i++){
            var cur = plans.get(i);
            var next = plans.get(i + 1);

            if(validLink(cur, next.x, next.y)){
                Point2 config = new Point2(next.x - cur.x, next.y - cur.y);
                cur.config = config;

                if(world.build(cur.x, cur.y) instanceof PressureBridgeBuild bridgec){
                    if(validLink(next, cur.x, cur.y)){
                        bridgec.configure(config);
                    }
                }
            }
        }
    }

    @Override
    public boolean canReplace(Block other){
        boolean valid = true;
        if(other instanceof PressureBridge cond){
            valid = PressureAPI.tierAble(cond, tier);
        }

        return canBeReplaced(other) && valid;
    }

    public boolean validLink(BuildPlan other, float x, float y){
        if(other == null){
            return false;
        }

        return collision(x, y, other.x, other.y, range / tilesize + 1);
    }

    public boolean validLink(Building other, int x, int y){
        if(other == null){
            return false;
        }

        PressureBridgeBuild b2 = (PressureBridgeBuild)world.build(x, y);

        //null check
        if(b2 == null){
            return false;
        }

        return collision(b2.x, b2.y, other.x, other.y, range) && other instanceof PressureBridgeBuild b &&
                   (PressureAPI.tierAble(b2, b));
    }

    public boolean positionsValid(int x1, int y1, int x2, int y2){
        if(x1 == x2){
            return Math.abs(y1 - y2) <= range / tilesize;
        }else if(y1 == y2){
            return Math.abs(x1 - x2) <= range / tilesize;
        }else{
            return false;
        }
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid){
        super.drawPlace(x, y, rotation, valid);

        Drawf.dashCircle(x * 8, y * 8, range, Pal.accent);
    }

    public int length(float x1, float y1, float x2, float y2){
        return (int)(Mathf.dst(x1, y1, x2, y2) / tilesize);
    }

    @Override
    public void changePlacementPath(Seq<Point2> points, int rotation){
        Placement.calculateNodes(points, this, rotation, (point, other) -> overlaps(world.tile(point.x, point.y), world.tile(other.x, other.y)));
    }

    public boolean overlaps(@Nullable Tile src, @Nullable Tile other){
        if(src == null || other == null) return true;

        return Intersector.overlaps(Tmp.cr1.set(src.worldx() + offset, src.worldy() + offset, range - 1),
            Tmp.r1.setSize(size).setCenter(other.worldx() + offset, other.worldy() + offset));
    }

    public class PressureBridgeBuild extends PressureBlockBuild implements Ranged{
        public int link = -1;

        @Override
        @SuppressWarnings("unchecked")
        public PressureBridgeBuild self(){
            return this;
        }

        @Override
        public void updateTile(){
            super.updateTile();

            if(linked() && link() instanceof PressureBridgeBuild link32){
                if(link32.linked(this) && linked(link32)){
                    unlink();
                    link32.unlink();
                }
            }
        }

        @Override
        public void drawSelect(){
            super.drawSelect();

//            IntSeq tmp = Pools.get(IntSeq.class, IntSeq::new).obtain();
            if(Vars.control.input.config.getSelected() != this){
                drawWires();
            }
        }

        private void drawWires(){

            float s = size * 8 / 2f + 2f;
            int range = (int)(range() / tilesize * 2);

            for(int x = -range; x < range; x++){
                for(int y = -range; y < range; y++){
                    Building build = nearby(x, y);
                    if(validLink(build, tileX(), tileY()) && build instanceof PressureBridgeBuild b){
                        if(!b.linked(this) && !linked(b) && b!=this){

                            continue;
                        }
                        if (b!=this){
                            float angleTo = b.angleTo(this);
                            float blockSize = size * tilesize + 1f;

                            Tmp.v1.trns(angleTo, OlGeomerty.squareEdgeDistance(blockSize, angleTo)).add(b);
                            Tmp.v2.trns(angleTo + 180, OlGeomerty.squareEdgeDistance(blockSize, angleTo + 180)).add(this);
//                        Drawf.dashLine(Pal.place, b.x, b.y, this.x, this.y);
//                        Drawf.dashLine(Pal.place, b.x, b.y, this.x, this.y);
                        dashLine(Pal.place, Tmp.v1.x,Tmp.v1.y,Tmp.v2.x,Tmp.v2.y                            );
//                            Lines.line(Tmp.v1.x, Tmp.v1.y, Tmp.v2.x, Tmp.v2.y);
//                        Drawf.dashLine(Pal.place, Tmp.v1.x,Tmp.v1.y,Tmp.v2.x,Tmp.v2.y);
                        }

                        Drawf.select(b.x, b.y, s, Pal.place);

//                        Drawf.square(b.x, b.y, 2f, 45f, Pal.place);

//                        Lines.stroke(3f, Pal.gray);
//                        Lines.square(x, y, 2f + 1f, rotation);
                        /*if(build != this){
                            tmp.add(build.pos());
                        }*/
                    }
                }
            }
        }

        private void dashLine(Color color, float x, float y, float x2, float y2){
            Drawf.dashLine(color,x,y,x2,y2,(int)(Math.max(Math.abs(x - x2), Math.abs(y - y2)) / tilesize * 2+1));
        }


        @Override
        public void draw(){
            super.draw();

            if(linked()){
                drawBridge(this, link());
            }

        }

        @Override
        public void drawConfigure(){
            float s = size * 8 / 2f + 2f;

            int range = (int)(range() / tilesize * 2);
            for(int x = -range; x < range; x++){
                for(int y = -range; y < range; y++){
                    Building b = nearby(x, y);
                    if(validLink(b, tileX(), tileY()) && b instanceof PressureBridgeBuild build && b != this){
                        if(!build.linked(this) && !linked(b)){
                            Drawf.select(b.x, b.y, s, build.linked(this) || linked(b) ? Pal.place : Pal.accent);
                        }
                    }
                }
            }
            drawWires();

//            Drawf.select(x, y, s, Pal.accent);
            drawRange();
        }

        @Override
        public void nextBuildings(Building income, Cons<Building> consumer){
            int range = (int)(range() / tilesize * 2);
            for(int x = -range; x < range; x++){
                for(int y = -range; y < range; y++){
                    if(nearby(x, y) instanceof PressureBridgeBuild b && validLink(nearby(x, y), tileX(), tileY())){
                        if(b != this && b.linked(this) || linked(b)) consumer.get(b);
                    }
                }
            }
            super.nextBuildings(income, consumer);

        }

        public Building link(){
            if(link == -1){
                return null;
            }

            Tile tile = world.tile(link);
            return tile == null ? null : tile.build;
        }

        public boolean linked(Building b){
            return link() == b;
        }


        public boolean linked(){
            return link != -1 && link() instanceof PressureBridgeBuild;
        }

        public void unlink(){
            link = -1;
        }

        @Override
        public boolean onConfigureBuildTapped(Building other){
            if(link == other.pos()){
                configure(other.pos());
                deselect();
                return false;
            }
            if(other instanceof PressureBridgeBuild b && validLink(other, tileX(), tileY()) && other != this){
                if(b.link == pos()){
                    b.configure(pos());
                }else{
                    configure(other.pos());
                }

                deselect();
                return false;
            }

            return super.onConfigureBuildTapped(other);
        }

        public Seq<PressureBridgeBuild> validLinks(Boolf<PressureBridgeBuild> validator){
            Seq<PressureBridgeBuild> builds = new Seq<>();
            int range = (int)(range() / tilesize * 2);
            for(int x = -range; x < range; x++){
                for(int y = -range; y < range; y++){
                    if(nearby(x, y) instanceof PressureBridgeBuild b && validLink(nearby(x, y), tileX(), tileY()) && validator.get(b)){
                        builds.add(b);
                    }
                }
            }

            return builds;
        }

        @Override
        public Seq<Building> children(){
            Seq<Building> childs = super.children();
            for(PressureBridgeBuild b : validLinks(b -> b.linked(this) || linked(b))){
                if(b == this){
                    continue;
                }

                if(PressureAPI.tierAble(b, this)){
                    childs.add(b);
                }
            }

            return childs;
        }

        @Override
        public float range(){
            return range;
        }

        @Override
        public Point2 config(){
            return Point2.unpack(link).sub(tile.x, tile.y);
        }

        @Override
        public byte version(){
            return 1;
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.i(link);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            link = read.i();
        }
    }
}