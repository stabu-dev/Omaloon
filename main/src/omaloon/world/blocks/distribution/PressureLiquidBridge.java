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
import omaloon.world.graph.*;
import omaloon.world.interfaces.*;
import omaloon.world.meta.PressureTank.*;

import static mindustry.Vars.*;

public class PressureLiquidBridge extends GenericPressureBlock{
    public int maxConnections = 4;

    public float range = 80;

    public @Load("@-end") TextureRegion endRegion;
    public @Load("@-end-bottom") TextureRegion endBottomRegion;
    public @Load("@-end-liquid") TextureRegion endLiquidRegion;
    public @Load("@-bridge") TextureRegion bridgeRegion;
    public @Load("@-bridge-bottom") TextureRegion bridgeBottomRegion;
    public @Load("@-bridge-liquid") TextureRegion bridgeLiquidRegion;

    public @Load(value = "@-arrow", fallBack = "item-bridge-arrow") TextureRegion arrowRegion;

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

        @Override
        public void drawConfigure(){
            Drawf.select(x, y, size * tilesize / 2f + 2f, Pal.accent);

            if (acceptsLinks()) {
                indexer.eachBlock(this, range, other -> other != this && other.dst(this) <= range && other instanceof PressureLiquidBridgeBuild bridge && HasPressure.connects(this, bridge) && bridge.acceptsLinks(), other -> {
                    if (!linked.contains(other.pos())) {
                        Drawf.select(
                        other.x, other.y,
                        other.block.size * tilesize / 2f + 2f + (other != getLink() ? Mathf.absin(4f, 1) : 0),
                        other != getLink() ? Pal.breakInvalid : Pal.place
                        );
                    }
                });
            } else {
                if (getLink() != null) {
                    Drawf.select(getLink().x, getLink().y, getLink().block.size * tilesize / 2f + 2f, Pal.place);
                }
            }
        }

        @Override
        public void drawSelect(){
            for(int i : linked.items) {
                Building other = Vars.world.build(i);

                if (other == null) continue;

                Tmp.v1.trns(angleTo(other), 2);

                Lines.stroke(3f, Pal.gray);

                Lines.poly(other.x, other.y, 12, 2f, 0f);
                Lines.line(
                other.x - Tmp.v1.x,
                other.y - Tmp.v1.y,
                x + Tmp.v1.x,
                y + Tmp.v1.y
                );

                Lines.stroke(1f, Pal.accent);

                Lines.poly(other.x, other.y, 12, 2f, 0f);
                Lines.line(
                other.x - Tmp.v1.x,
                other.y - Tmp.v1.y,
                x + Tmp.v1.x,
                y + Tmp.v1.y
                );
                Tmp.v1.set(other).lerp(this, ((Time.time * 2f) % 100f) / 100);
                Draw.mixcol(Pal.accent, 1f);
                Draw.color();
                Draw.rect(arrowRegion, Tmp.v1.x, Tmp.v1.y, other.angleTo(this));
                Draw.mixcol();
            }

            if (getLink() != null) {
                Building other = getLink();

                Tmp.v1.trns(angleTo(other), 2);

                Lines.stroke(3f, Pal.gray);

                Lines.poly(other.x, other.y, 12, 2f, 0f);
                Lines.line(
                other.x - Tmp.v1.x,
                other.y - Tmp.v1.y,
                x + Tmp.v1.x,
                y + Tmp.v1.y
                );

                Lines.stroke(1f, Pal.place);

                Lines.poly(other.x, other.y, 12, 2f, 0f);
                Lines.line(
                other.x - Tmp.v1.x,
                other.y - Tmp.v1.y,
                x + Tmp.v1.x,
                y + Tmp.v1.y
                );
                Tmp.v1.set(this).lerp(other, ((Time.time * 2f) % 100f) / 100);
                Draw.mixcol(Pal.place, 1f);
                Draw.color();
                Draw.rect(arrowRegion, Tmp.v1.x, Tmp.v1.y, other.angleTo(this));
                Draw.mixcol();
            }
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
        public void updateTile(){
            if (getLink() != null && getLink().linked.contains(pos())){
                getLink().linked.add(pos());
                new PressureGraph().floodMergeGraph(this);
            }
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.i(link);

            write.b(linked.size);

            linked.each(write::i);
        }
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
