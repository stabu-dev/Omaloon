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
    beamStroke = 1.6f, armLength = 11f,
    minShadowOffset = -3.5f, shadowOffset = -16f, topOffset = -12f;
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
            for(var c : calls){
                Draw.rect(c.d.capRegion, c.b.x + c.d.topOffset, c.b.y + c.d.topOffset);
                Lines.stroke(c.d.beamStroke);
                Lines.line(c.b.x, c.b.y, c.b.x + c.d.topOffset, c.b.y + c.d.topOffset, false);
            }
            Draw.flush();

            Gl.colorMask(false, false, false, true);
            Draw.blend(new Blending(Gl.zero, Gl.oneMinusSrcAlpha));
            Draw.color(Color.white);
            for(var c : calls) Draw.rect(c.d.capRegion, c.b.x, c.b.y);
            Draw.flush();
            Draw.blend();
            Gl.colorMask(true, true, true, true);
            for(var c : calls){
                float tp = c.b.totalProgress() * c.d.rotateSpeed, ang = tp + 90f, t = c.d.shadowOffset + 1.5f, bt = c.d.minShadowOffset - 1.5f;
                for(int s : Mathf.signs){
                    float a = ang + (s == 1 ? 0 : 180f), bx = c.b.x + c.d.topOffset, by = c.b.y + c.d.topOffset;
                    Lines.line(bx, by, c.b.x + t + Angles.trnsx(a, c.d.armLength), c.b.y + t + Angles.trnsy(a, c.d.armLength));
                    Lines.line(bx, by, c.b.x + bt + Angles.trnsx(a, c.d.armLength), c.b.y + bt + Angles.trnsy(a, c.d.armLength));
                }
            }
            Draw.flush();

            Gl.clear(Gl.stencilBufferBit);
            Gl.stencilMask(0xFF);
            Gl.colorMask(false, false, false, false);
            Gl.enable(Gl.stencilTest);
            Gl.stencilFunc(Gl.always, 1, 0xFF);
            Gl.stencilOp(Gl.replace, Gl.replace, Gl.replace);
            for(var c : calls) Draw.rect(c.d.topRegion, c.b.x, c.b.y, Mathf.mod(c.b.totalProgress() * c.d.rotateSpeed, 180f));
            Draw.flush();

            Gl.colorMask(true, true, true, true);
            Gl.stencilFunc(Gl.equal, 1, 0xFF);
            Gl.stencilOp(Gl.keep, Gl.keep, Gl.keep);
            for(var c : calls){
                float tp = c.b.totalProgress() * c.d.rotateSpeed;
                for(int i = 0; i <= 6; i++) Draw.rect(c.d.rotatorRegion, c.b.x + c.d.minShadowOffset * (i / 6f), c.b.y + c.d.minShadowOffset * (i / 6f), tp);
            }
            Draw.flush();
            Gl.disable(Gl.stencilTest);
            for(var c : calls){
                float tp = c.b.totalProgress() * c.d.rotateSpeed;
                for(int i = 0; i <= c.d.shadowPrecision; i++){
                    float s = (float)i / c.d.shadowPrecision, off = c.d.minShadowOffset + (c.d.shadowOffset - c.d.minShadowOffset) * s;
                    Draw.rect(c.d.rotatorRegion, c.b.x + off, c.b.y + off, tp);
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

        shadowBufferDrawCalls.add(new TurbineDrawCall(this, build));
    }

    @Override
    public void drawPlan(Block block, BuildPlan plan, Eachable<BuildPlan> list){
        Draw.rect(rotatorRegion, plan.drawx(), plan.drawy());
        Draw.rect(topRegion, plan.drawx(), plan.drawy());
        Draw.rect(capRegion, plan.drawx(), plan.drawy());
    }

    @Override
    public void load(Block block){
        rotatorRegion = Core.atlas.find(block.name + suffix);
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
