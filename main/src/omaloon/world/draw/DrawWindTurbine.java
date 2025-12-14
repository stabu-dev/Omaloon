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
    public float rotateSpeed = 1f,
    beamStroke = 1.6f, armLength = 10f,
    minShadowOffset = -3.5f, shadowOffset = -16f, topOffset = -12f;
    public int blades = 3, shadowPrecision = 20;

    public TextureRegion rotatorRegion, rotatorRegionRev, topRegion, capRegion;

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

            Gl.clear(Gl.stencilBufferBit);

            Gl.enable(Gl.stencilTest);
            Gl.stencilMask(0xFF);
            Gl.colorMask(false, false, false, false);
            Gl.stencilFunc(Gl.always, 1, 0xFF);
            Gl.stencilOp(Gl.replace, Gl.replace, Gl.replace);

            for(var c : calls){
                float tp = c.b.totalProgress() * c.d.rotateSpeed;
                for(int k = 0; k < c.d.blades; k++){
                    float a = tp + k * (360f / c.d.blades);
                    Draw.rect(c.d.topRegion, c.b.x + Angles.trnsx(a, c.d.armLength / 2f), c.b.y + Angles.trnsy(a, c.d.armLength / 2f), a);
                }
            }
            Draw.flush();

            Gl.colorMask(true, true, true, true);
            Gl.stencilFunc(Gl.notequal, 1, 0xFF);
            Gl.stencilOp(Gl.keep, Gl.keep, Gl.keep);

            for(var c : calls){
                Lines.stroke(c.d.beamStroke);
                Lines.line(c.b.x, c.b.y, c.b.x + c.d.topOffset, c.b.y + c.d.topOffset, false);
            }
            Draw.flush();

            Gl.disable(Gl.stencilTest);

            for(var c : calls){
                Draw.rect(c.d.capRegion, c.b.x + c.d.topOffset, c.b.y + c.d.topOffset);

                float tp = c.b.totalProgress() * c.d.rotateSpeed, t = c.d.shadowOffset + 1.5f, bt = c.d.minShadowOffset - 1.5f;
                for(int k = 0; k < c.d.blades; k++){
                    float a = tp + k * (360f / c.d.blades);
                    float bx = c.b.x + c.d.topOffset + 1f, by = c.b.y + c.d.topOffset + 0.5f;
                    Lines.line(bx, by, c.b.x + t + Angles.trnsx(a, c.d.armLength + 0.7f), c.b.y + t + Angles.trnsy(a, c.d.armLength + 0.7f));
                    Lines.line(bx, by, c.b.x + bt + Angles.trnsx(a, c.d.armLength + 0.7f), c.b.y + bt + Angles.trnsy(a, c.d.armLength + 0.7f));
                }
            }
            Draw.flush();

            Gl.colorMask(false, false, false, true);
            Draw.blend(new Blending(Gl.zero, Gl.oneMinusSrcAlpha));
            Draw.color(Color.white);
            for(var c : calls) Draw.rect(c.d.capRegion, c.b.x, c.b.y);
            Draw.flush();
            Draw.blend();
            Gl.colorMask(true, true, true, true);

            Gl.clear(Gl.stencilBufferBit);
            Gl.stencilMask(0xFF);
            Gl.colorMask(false, false, false, false);
            Gl.enable(Gl.stencilTest);
            Gl.stencilFunc(Gl.always, 1, 0xFF);
            Gl.stencilOp(Gl.replace, Gl.replace, Gl.replace);

            for(var c : calls){
                float tp = c.b.totalProgress() * c.d.rotateSpeed;
                for(int k = 0; k < c.d.blades; k++){
                    float a = tp + k * (360f / c.d.blades);
                    Draw.rect(c.d.topRegion, c.b.x + Angles.trnsx(a, c.d.armLength / 2f), c.b.y + Angles.trnsy(a, c.d.armLength / 2f), a);
                }
            }
            Draw.flush();

            Gl.colorMask(true, true, true, true);
            Gl.stencilFunc(Gl.equal, 1, 0xFF);
            Gl.stencilOp(Gl.keep, Gl.keep, Gl.keep);

            for(var c : calls){
                float tp = c.b.totalProgress() * c.d.rotateSpeed;
                for(int i = 0; i <= 6; i++){
                    float off = c.d.minShadowOffset * (i / 6f);
                    drawBlades(c.d, c.b.x + off, c.b.y + off, tp);
                }
            }
            Draw.flush();
            Gl.disable(Gl.stencilTest);

            for(var c : calls){
                float tp = c.b.totalProgress() * c.d.rotateSpeed;
                for(int i = 0; i <= c.d.shadowPrecision; i++){
                    float s = (float)i / c.d.shadowPrecision, off = c.d.minShadowOffset + (c.d.shadowOffset - c.d.minShadowOffset) * s;
                    drawBlades(c.d, c.b.x + off, c.b.y + off, tp);
                }
            }

            shadowBuffer.end();
            Draw.color(Pal.shadow, Pal.shadow.a);
            Draw.rect(Draw.wrap(shadowBuffer.getTexture()), Core.camera.position.x, Core.camera.position.y, Core.camera.width, -Core.camera.height);
            Draw.reset();
        });
    }

    private static void drawBlades(DrawWindTurbine d, float x, float y, float rotation){
        for(int k = 0; k < d.blades; k++){
            float a = rotation + k * (360f / d.blades);
            float bladeX = x + Angles.trnsx(a, d.armLength);
            float bladeY = y + Angles.trnsy(a, d.armLength);

            float s = Mathf.sinDeg(a);
            float t = 0.5f - 0.5f * s;

            if(t < 1f){
                Draw.color(Color.white, 1f - t);
                Draw.rect(d.rotatorRegion, bladeX, bladeY, a);
            }
            if(t > 0f){
                Draw.color(Color.white, t);
                Draw.rect(d.rotatorRegionRev, bladeX, bladeY, a);
            }
            Draw.color(Color.white, 1f);
        }
    }

    @Override
    public void draw(Building build){
        float r = build.totalProgress() * rotateSpeed;

        Draw.z(Layer.power + 0.1f);
        for(int k = 0; k < blades; k++){
            float a = r + k * (360f / blades);
            float armX = build.x + Angles.trnsx(a, armLength / 2f);
            float armY = build.y + Angles.trnsy(a, armLength / 2f);
            Draw.rect(topRegion, armX, armY, a);
        }
        Draw.rect(capRegion, build.x, build.y);

        Draw.z(Layer.power + 0.3f);
        drawBlades(this, build.x, build.y, r);
        Draw.reset();

        shadowBufferDrawCalls.add(new TurbineDrawCall(this, build));
    }

    @Override
    public void drawPlan(Block block, BuildPlan plan, Eachable<BuildPlan> list){
        for(int k = 0; k < blades; k++){
            float a = k * (360f / blades);
            float armX = plan.drawx() + Angles.trnsx(a, armLength / 2f);
            float armY = plan.drawy() + Angles.trnsy(a, armLength / 2f);
            Draw.rect(topRegion, armX, armY, a);
        }

        drawBlades(this, plan.drawx(), plan.drawy(), 0);

        Draw.rect(capRegion, plan.drawx(), plan.drawy());
    }

    @Override
    public void load(Block block){
        rotatorRegion = Core.atlas.find(block.name + suffix);
        rotatorRegionRev = Core.atlas.find(block.name + suffix + "-rev");
        topRegion = Core.atlas.find(block.name + suffix + "-top");
        capRegion = Core.atlas.find(block.name + suffix + "-cap");
    }

    public static class TurbineDrawCall{
        final DrawWindTurbine d;
        final Building b;

        TurbineDrawCall(DrawWindTurbine d, Building b){
            this.d = d;
            this.b = b;
        }
    }
}