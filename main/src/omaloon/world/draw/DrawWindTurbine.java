package omaloon.world.draw;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.gl.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import mindustry.entities.units.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.*;
import mindustry.world.draw.*;

public class DrawWindTurbine extends DrawBlock{
    public String suffix = "-rotator";
    public float rotateSpeed = 1f, beamStroke = 1.6f, armLength = 11f;
    public float minShadowOffset = -3.5f, shadowOffset = -16f, topOffset = -12f;
    public int shadowPrecision = 20;

    public TextureRegion rotatorRegion, topRegion, capRegion;

    public static final FrameBuffer shadowBuffer = Core.graphics == null ? null : new FrameBuffer(Pixmap.Format.rgba8888, Core.graphics.getWidth(), Core.graphics.getHeight(), false, true);
    public static final Seq<TurbineDrawCall> shadowBufferDrawCalls = new Seq<>();

    static{
        if(Core.graphics != null){
            Events.on(ResizeEvent.class, e -> shadowBuffer.resize(Core.graphics.getWidth(), Core.graphics.getHeight()));
            Events.run(Trigger.draw, DrawWindTurbine::renderShadows);
            Events.on(DisposeEvent.class, e -> shadowBuffer.dispose());
        }
    }

    public DrawWindTurbine(String suffix){
        this.suffix = suffix;
    }

    public DrawWindTurbine(){
    }

    private static void renderShadows(){
        if(shadowBufferDrawCalls.isEmpty()) return;
        var calls = shadowBufferDrawCalls.copy();
        shadowBufferDrawCalls.clear();

        Draw.draw(Layer.power + 0.2f, () -> {
            shadowBuffer.begin(Color.clear);

            for(var call : calls){
                DrawWindTurbine d = call.drawer;
                Building b = call.build;
                Draw.rect(d.capRegion, b.x + d.topOffset, b.y + d.topOffset);
                Lines.stroke(d.beamStroke);
                Lines.line(b.x, b.y, b.x + d.topOffset, b.y + d.topOffset, false);
            }
            Draw.flush();

            Gl.colorMask(false, false, false, true);
            Draw.blend(new Blending(Gl.zero, Gl.oneMinusSrcAlpha));
            Draw.color(Color.white);
            for(var call : calls) Draw.rect(call.drawer.capRegion, call.build.x, call.build.y);
            Draw.flush();
            Draw.blend();
            Gl.colorMask(true, true, true, true);

            for(var call : calls){
                DrawWindTurbine d = call.drawer;
                Building b = call.build;
                float totalProgress = b.totalProgress() * d.rotateSpeed, ang = totalProgress + 90f;
                float top = d.shadowOffset + 1.5f, bot = d.minShadowOffset - 1.5f;
                for(int s : Mathf.signs){
                    float a = ang + (s == 1 ? 0 : 180f), bx = b.x + d.topOffset, by = b.y + d.topOffset;
                    Lines.line(bx, by, b.x + top + Angles.trnsx(a, d.armLength), b.y + top + Angles.trnsy(a, d.armLength));
                    Lines.line(bx, by, b.x + bot + Angles.trnsx(a, d.armLength), b.y + bot + Angles.trnsy(a, d.armLength));
                }
            }
            Draw.flush();

            Gl.clear(Gl.stencilBufferBit);
            Gl.stencilMask(0xFF);
            Gl.colorMask(false, false, false, false);
            Gl.enable(Gl.stencilTest);
            Gl.stencilFunc(Gl.always, 1, 0xFF);
            Gl.stencilOp(Gl.replace, Gl.replace, Gl.replace);

            for(var call : calls) Draw.rect(call.drawer.topRegion, call.build.x, call.build.y, Mathf.mod(call.build.totalProgress() * call.drawer.rotateSpeed, 180f));
            Draw.flush();

            Gl.colorMask(true, true, true, true);
            Gl.stencilFunc(Gl.equal, 1, 0xFF);
            Gl.stencilOp(Gl.keep, Gl.keep, Gl.keep);

            for(var call : calls){
                DrawWindTurbine d = call.drawer;
                float totalProgress = call.build.totalProgress() * d.rotateSpeed;
                for(int i = 0; i <= 6; i++) Draw.rect(d.rotatorRegion, call.build.x + d.minShadowOffset * (i / 6f), call.build.y + d.minShadowOffset * (i / 6f), totalProgress);
            }
            Draw.flush();
            Gl.disable(Gl.stencilTest);

            for(var call : calls){
                DrawWindTurbine d = call.drawer;
                float totalProgress = call.build.totalProgress() * d.rotateSpeed;
                for(int i = 0; i <= d.shadowPrecision; i++){
                    float s = (float)i / d.shadowPrecision, off = d.minShadowOffset + (d.shadowOffset - d.minShadowOffset) * s;
                    Draw.rect(d.rotatorRegion, call.build.x + off, call.build.y + off, totalProgress);
                }
            }

            shadowBuffer.end();
            Draw.color(Pal.shadow, Pal.shadow.a);
            Draw.rect(Draw.wrap(shadowBuffer.getTexture()), Core.camera.position.x, Core.camera.position.y, Core.camera.width, -Core.camera.height);
            Draw.reset();
        });
    }

    @Override
    public void draw(Building build){
        float r = Mathf.mod(build.totalProgress() * rotateSpeed, 180f);
        Draw.z(Layer.power + 0.3f);
        Draw.rect(rotatorRegion, build.x, build.y, r);
        Draw.z(Layer.power + 0.1f);
        Draw.rect(topRegion, build.x, build.y, r);
        Draw.rect(capRegion, build.x, build.y);
        Draw.alpha(Mathf.clamp(r / 90f));
        Draw.z(Layer.power + 0.3f);
        Draw.rect(rotatorRegion, build.x, build.y, r - 180f);
        Draw.reset();
        drawShadow(build);
    }

    @Override
    public void drawPlan(Block block, BuildPlan plan, Eachable<BuildPlan> list){
        Draw.rect(rotatorRegion, plan.drawx(), plan.drawy());
        Draw.rect(topRegion, plan.drawx(), plan.drawy());
        Draw.rect(capRegion, plan.drawx(), plan.drawy());
    }

    public void drawShadow(Building b){
        shadowBufferDrawCalls.add(new TurbineDrawCall(this, b));
    }

    @Override
    public void load(Block block){
        rotatorRegion = Core.atlas.find(block.name + suffix);
        topRegion = Core.atlas.find(block.name + suffix + "-top");
        capRegion = Core.atlas.find(block.name + suffix + "-cap");
    }

    public static class TurbineDrawCall{
        final DrawWindTurbine drawer;
        final Building build;

        TurbineDrawCall(DrawWindTurbine drawer, Building build){
            this.drawer = drawer;
            this.build = build;
        }
    }
}
