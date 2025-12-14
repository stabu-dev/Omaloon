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
    public static final Seq<Runnable> shadowBufferDrawCalls = new Seq<>();

    static{
        if(Core.graphics != null){
            Events.on(ResizeEvent.class, e -> shadowBuffer.resize(Core.graphics.getWidth(), Core.graphics.getHeight()));
            Events.run(Trigger.draw, () -> {
                if(shadowBufferDrawCalls.isEmpty()) return;

                var copy = shadowBufferDrawCalls.copy();
                shadowBufferDrawCalls.clear();

                Draw.draw(Layer.power + 0.2f, () -> {
                    shadowBuffer.begin(Color.clear);
                    copy.each(Runnable::run);
                    shadowBuffer.end();

                    Draw.color(Pal.shadow, Pal.shadow.a);
                    Draw.rect(Draw.wrap(shadowBuffer.getTexture()), Core.camera.position.x, Core.camera.position.y, Core.camera.width, -Core.camera.height);
                    Draw.reset();
                });
            });
            Events.on(DisposeEvent.class, e -> shadowBuffer.dispose());
        }
    }

    public DrawWindTurbine(String suffix){
        this.suffix = suffix;
    }

    public DrawWindTurbine(){
    }

    @Override
    public void draw(Building build){
        float r = Mathf.mod(build.totalProgress() * rotateSpeed, 180f);

        Draw.z(Layer.power + 0.3f);
        Draw.rect(rotatorRegion, build.x, build.y, r);
        Draw.rect(capRegion, build.x, build.y);

        Draw.z(Layer.power + 0.1f);
        Draw.rect(topRegion, build.x, build.y, r);
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
        float totalProgress = b.totalProgress() * rotateSpeed;

        shadowBufferDrawCalls.add(() -> {
            Gl.clear(Gl.stencilBufferBit);
            Draw.rect(capRegion, b.x + topOffset, b.y + topOffset);

            Lines.stroke(beamStroke);
            Lines.line(b.x, b.y, b.x + topOffset, b.y + topOffset, false);

            float bladeTopOffset = shadowOffset + 1.5f;
            float bladeBotOffset = minShadowOffset - 1.5f;
            float ang = totalProgress + 90f;

            for(int s : Mathf.signs){
                float a = ang + (s == 1 ? 0 : 180f);
                float bx = b.x + topOffset, by = b.y + topOffset;

                Lines.line(bx, by, b.x + bladeTopOffset + Angles.trnsx(a, armLength), b.y + bladeTopOffset + Angles.trnsy(a, armLength));
                Lines.line(bx, by, b.x + bladeBotOffset + Angles.trnsx(a, armLength), b.y + bladeBotOffset + Angles.trnsy(a, armLength));
            }

            Draw.stencil(
            () -> Draw.rect(topRegion, b.x, b.y, Mathf.mod(totalProgress, 180f)),
            () -> {
                for(int i = 0; i <= 6; i++){
                    float offset = minShadowOffset * (i / 6f);
                    Draw.rect(rotatorRegion, b.x + offset, b.y + offset, totalProgress);
                }
            }
            );

            for(int i = 0; i <= shadowPrecision; i++){
                float s = (float)i / shadowPrecision;
                float off = minShadowOffset + (shadowOffset - minShadowOffset) * s;
                Draw.rect(rotatorRegion, b.x + off, b.y + off, totalProgress);
            }
        });
    }

    @Override
    public void load(Block block){
        rotatorRegion = Core.atlas.find(block.name + suffix);
        topRegion = Core.atlas.find(block.name + suffix + "-top");
        capRegion = Core.atlas.find(block.name + suffix + "-cap");
    }
}
