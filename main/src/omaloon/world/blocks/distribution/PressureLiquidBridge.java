package omaloon.world.blocks.distribution;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.*;
import mindustry.core.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.meta.*;
import omaloon.annotations.Annotations.*;
import omaloon.world.*;
import omaloon.world.interfaces.*;
import omaloon.world.meta.PressureTank.*;

import static mindustry.Vars.tilesize;

public class PressureLiquidBridge extends GenericPressureBlock{
    public int maxConnections = 4;

    public float range = 80;

    public @Load("@-end") TextureRegion endRegion;
    public @Load("@-end-bottom") TextureRegion endBottomRegion;
    public @Load("@-end-liquid") TextureRegion endLiquidRegion;
    public @Load("@-bridge") TextureRegion bridgeRegion;
    public @Load("@-bridge-bottom") TextureRegion bridgeBottomRegion;
    public @Load("@-bridge-liquid") TextureRegion bridgeLiquidRegion;

    public @Load(value = "@-bottom", fallBack = "@modname-liquid-bottom") TextureRegion bottomRegion;

    public PressureLiquidBridge(String name){
        super(name);
        configurable = true;
        destructible = true;
        update = true;
        canOverdrive = false;
        group = BlockGroup.liquids;

        config(Integer.class, (PressureLiquidBridgeBuild build, Integer link) -> build.link = link);
        config(Point2.class, (PressureLiquidBridgeBuild build, Point2 link) -> {
            build.link = Point2.unpack(build.pos()).add(link).pack();
        });
        configClear((PressureLiquidBridgeBuild build) -> {
            build.link = -1;
        });
    }

    public void drawBridge(TextureRegion bridge, TextureRegion end, float x1, float y1, float x2, float y2) {
        float angle = Angles.angle(x1, y1, x2, y2);
        float dst = Mathf.dst(x1, y1, x2, y2);

        Draw.rect(end, x1, y1, angle);
        Draw.xscl = -1f;
        Draw.rect(end, x2, y2, angle);
        Draw.xscl = 1f;

        Tmp.v1.trns(angle, end.width / 16f).add(x1, y1);
        Tmp.v2.trns(angle, dst - end.width / 16f).add(x1, y1);

        Lines.stroke(end.height/4f);
        Lines.line(bridge, Tmp.v1.x, Tmp.v1.y, Tmp.v2.x, Tmp.v2.y, false);
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid){
        Drawf.dashCircle(x * tilesize + offset, y * tilesize + offset, range, Pal.accent);
    }

//    @Override
//    public void drawBridge(BuildPlan req, float ox, float oy, float flip){
//        drawBridge(bridgeBottomRegion, endBottomRegion, new Vec2(req.drawx(), req.drawy()), new Vec2(ox, oy));
//        drawBridge(new Vec2(req.drawx(), req.drawy()), new Vec2(ox, oy));
//    }
//
//    public void drawBridge(Vec2 pos1, Vec2 pos2){
//        boolean line = pos1.x == pos2.x || pos1.y == pos2.y;
//
//        int segments = length(pos1.x, pos1.y, pos2.x, pos2.y) + 1;
//        float sl = 0;
//        if(!line){
//            sl = Mathf.dst(pos1.x, pos1.y, pos2.x, pos2.y) / segments;
//        }
//        float sa = pos1.angleTo(pos2);
//        float oa = pos2.angleTo(pos1);
//
//        if(line){
//            if(pos1.y == pos2.y){
//                Position a = pos1.x < pos2.x ? pos2 : pos1;
//                Position b = pos1.x < pos2.x ? pos1 : pos2;
//
//                segments = (int)(a.getX() / 8 - b.getX() / 8);
//            }
//
//            if(pos1.x == pos2.x){
//                Position a = pos1.y < pos2.y ? pos2 : pos1;
//                Position b = pos1.y < pos2.y ? pos1 : pos2;
//
//                segments = (int)(a.getY() / 8 - b.getY() / 8);
//            }
//        }
//
//        boolean reverse = pos1.x > pos2.x;
//
//        if(line){
//            reverse |= pos1.y < pos2.y;
//        }
//
//        float r = sa + (reverse ? 180 : 0);
//
//        TextureRegion end = reverse ? endRegion1 : endRegion;
//        TextureRegion str = reverse ? endRegion : endRegion1;
//
//        Draw.rect(end, pos1.x, pos1.y, sa);
//        Draw.rect(str, pos2.x, pos2.y, oa);
//
//        for(int i = 1; i < segments; i++){
//            float s_x = Mathf.lerp(pos1.x, pos2.x, (float)i / segments);
//            float s_y = Mathf.lerp(pos1.y, pos2.y, (float)i / segments);
//
//            if(line){
//                Draw.rect(bridgeRegion, s_x, s_y, r);
//            }else{
//                Draw.rect(bridgeRegion, s_x, s_y, sl, bridgeRegion.height * scl * xscl, r);
//            }
//        }
//    }
//
//    public int length(float x1, float y1, float x2, float y2){
//        return (int)(Mathf.dst(x1, y1, x2, y2) / tilesize);
//    }
//
//    @Override
//    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list){
//        Draw.rect(bottomRegion, plan.drawx(), plan.drawy());
//        super.drawPlanRegion(plan, list);
//    }

    @Override
    protected TextureRegion[] icons(){
        return new TextureRegion[]{
        Core.atlas.find(name + "-bottom", "omaloon-liquid-bottom"),
        Core.atlas.find(name)
        };
    }

    @Override
    public void init(){
        pressureConfig.hasPressure = pressureConfig.acceptsPressure = pressureConfig.outputsPressure = true;

        super.init();

        if(hasLiquids) hasLiquids = false;
        if(pressureConfig.group == null) pressureConfig.group = TankGroup.transportation;
    }

    public boolean linkValid(Tile from, Tile to) {
        return from.dst(to) <= range;
    }

    @Override
    public void setBars(){
        super.setBars();

        addBar("omaloon-bridge-connections", (PressureLiquidBridgeBuild entity) -> new Bar(
        () -> Core.bundle.format("bar.powerlines", entity.linked.size + (entity.getLink() == null ? 0 : 1), maxConnections),
        () -> Pal.items,
        () -> (entity.linked.size + (entity.getLink() == null ? 0f : 1f)) / maxConnections
        ));
    }

    public class PressureLiquidBridgeBuild extends GenericPressureBlockBuild{
        public int link = -1;
        public IntSeq linked = new IntSeq();

        @Override
        public boolean acceptsFluid(HasPressure from, @Nullable Liquid liquid, float amount){
            return
            super.acceptsFluid(from, liquid, amount) &&
            (liquid == pressure.getMain() || liquid == null || pressure.getMain() == null || from.pressure().getMain() == null);
        }

        public boolean acceptsLinks() {
            return (linked.size + (getLink() == null ? 0 : 1)) <= maxConnections;
        }

        @Override
        public Seq<HasPressure> connections(){
            Seq<HasPressure> o = super.connections();
            if(Vars.world.build(link) instanceof PressureLiquidBridgeBuild b) o.add(b);
            for(int pos : linked.items) if(Vars.world.build(pos) instanceof PressureLiquidBridgeBuild b) o.add(b);
            return o;
        }

        @Override public Point2 config(){
            return Point2.unpack(link).sub(tileX(), tileY());
        }

        @Override
        public void draw(){
            Draw.rect(bottomRegion, x, y);

//            Liquid main = pressure.getMain();
//
//            smoothAlpha = Mathf.approachDelta(smoothAlpha, main == null ? 0f : pressure.liquids[main.id] / (pressure.liquids[main.id] + pressure.air), PressureModule.smoothingSpeed);
//
//            if(smoothAlpha > 0.001f){
//                LiquidBlock.drawTiledFrames(size, x, y, liquidPadding, pressure.current, Mathf.clamp(smoothAlpha));
//            }

            Draw.rect(region, x, y);

            Draw.z(Layer.power);

            if(getLink() != null){
                Draw.alpha(Renderer.bridgeOpacity);
                drawBridge(bridgeBottomRegion, endBottomRegion, x, y, getLink().x, getLink().y);

//            if(smoothAlpha > 0.001f){
//                Draw.color(pressure.current.color, Mathf.clamp(smoothAlpha) * Renderer.bridgeOpacity);
//                drawBridge(bridgeLiquidRegion, endLiquidRegion, pos1, pos2);
//                Draw.color();
//            }

                Draw.alpha(Renderer.bridgeOpacity);
                drawBridge(bridgeRegion, endRegion, x, y, getLink().x, getLink().y);
            }

            Draw.reset();
        }

        public @Nullable PressureLiquidBridgeBuild getLink() {
            return Vars.world.build(link) instanceof PressureLiquidBridgeBuild bridge ? bridge : null;
        }

        @Override
        public boolean onConfigureBuildTapped(Building other){
            if(other instanceof PressureLiquidBridgeBuild bridge && HasPressure.connects(this, bridge)){
                if(bridge.link == pos()){
                    linked.removeValue(other.pos());
                    bridge.linked.add(pos());
                    configure(other.pos());
                    other.configure(-1);
                }else if(linkValid(this.tile, other.tile)){
                    if (other == this) {
                        if (getLink() != null) {
                            getLink().linked.removeValue(pos());
                            configure(-1);
                        }
                        return false;
                    }
                    if(link == other.pos()){
                        bridge.linked.removeValue(pos());
                        configure(-1);
                    }else if(acceptsLinks() && bridge.acceptsLinks()){
                        bridge.linked.add(pos());
                        configure(other.pos());
                    }
                    return false;
                }
            }
            return true;
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            link = read.i();

            byte size = read.b();

            for(int i = 0; i < size; i++){
                int otherLink = read.i();
                linked.add(otherLink);
            }
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.i(link);

            write.b(linked.size);

            linked.each(write::i);
        }

//        @Override
//        @AutoImplement.NoInject(HasPressureImpl.class)
//        public void updateTile(){
//            incoming.size = Math.min(incoming.size, maxConnections - (link == -1 ? 0 : 1));
//            incoming.shrink();
//
//            checkIncoming();
//
//            updatePressure();
//
//            Tile other = world.tile(link);
//            if(linkValid(tile, other)){
//                if(other.build instanceof TubeItemBridgeBuild && cast(other.build).acceptIncoming(this.tile.pos())){
//                    configureAny(-1);
//                    return;
//                }
//
//                IntSeq inc = ((ItemBridgeBuild)other.build).incoming;
//                int pos = tile.pos();
//                if(!inc.contains(pos)){
//                    inc.add(pos);
//                }
//
//                warmup = Mathf.approachDelta(warmup, efficiency(), 1f / 30f);
//            }
//        }
//
//        @Override
//        protected void drawInput(Tile other){
//            if(linkValid(this.tile, other, false)){
//                final float angle = tile.angleTo(other);
//                v2.trns(angle, 2.0F);
//                float tx = tile.drawx();
//                float ty = tile.drawy();
//                float ox = other.drawx();
//                float oy = other.drawy();
//                Draw.color(Pal.gray);
//                Lines.stroke(2.5F);
//                Lines.square(ox, oy, 2.0F, 45.0F);
//                Lines.square(tx, ty, 2.0F, 45.0F);
//                Lines.stroke(2.5F);
//                Lines.line(tx + v2.x, ty + v2.y, ox - v2.x, oy - v2.y);
//                Draw.color(Pal.place);
//                Lines.stroke(1.0F);
//                Lines.line(tx + v2.x, ty + v2.y, ox - v2.x, oy - v2.y);
//                Lines.square(ox, oy, 2.0F, 45.0F);
//                Lines.square(tx, ty, 2.0F, 45.0F);
//                Draw.mixcol(Draw.getColor(), 1.0F);
//                Draw.color();
//                Draw.mixcol();
//            }
//        }
    }
}
