package omaloon.world.draw;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.gl.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import mindustry.entities.units.*;
import mindustry.game.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.*;
import mindustry.world.draw.*;

public class DrawWindTurbine extends DrawBlock{
    public String suffix;

    public float rotateSpeed = 1;

    public float beamStroke = 2f;

    public float minShadowOffset = -3f, shadowOffset = -10f;
    public int shadowPrecision = 20;

    public TextureRegion rotatorRegion, topRegion, capRegion;
    
    // TODO bring all buffers to their own class
    public static final FrameBuffer shadowBuffer = Core.graphics == null ? null : new FrameBuffer(Core.graphics.getWidth(), Core.graphics.getHeight());
    public static final Seq<Runnable> shadowBufferDrawCalls = new Seq<>();

    static {
        if(Core.graphics != null){
            Events.on(EventType.ResizeEvent.class, e -> {
                shadowBuffer.resize(Core.graphics.getWidth(), Core.graphics.getHeight());
            });
            Events.run(Trigger.draw, () -> {
                if(!shadowBufferDrawCalls.isEmpty()){
                    var copy = shadowBufferDrawCalls.copy();
                    shadowBufferDrawCalls.clear();

                    Draw.draw(Layer.blockProp + 1, () -> {
                        Draw.flush();
                        shadowBuffer.begin(Color.clear);
                        copy.each(Runnable::run);
                        shadowBuffer.end();

                        Draw.color(Pal.shadow, Pal.shadow.a);
                        Draw.rect(
                        Draw.wrap(shadowBuffer.getTexture()),
                        Core.camera.position.x,
                        Core.camera.position.y,
                        Core.camera.width,
                        -Core.camera.height
                        );
                        Draw.reset();
                        Draw.flush();
                    });
                }
            });
            Events.on(EventType.DisposeEvent.class, e -> shadowBuffer.dispose());
        }
    }
    
    public DrawWindTurbine(String suffix) {
        this.suffix = suffix;
    }
    public DrawWindTurbine() {
        this("-rotator");
    }

    @Override
    public void draw(Building build){
        float r = Mathf.mod(build.totalProgress() * rotateSpeed, 180f);
        Draw.z(Layer.blockOver);
        Draw.rect(rotatorRegion, build.x, build.y, r);
        Draw.rect(capRegion, build.x, build.y, r);
        Draw.z(Layer.block);
        Draw.rect(topRegion, build.x, build.y, r);
        Draw.alpha(Mathf.clamp(r / 90f));
        Draw.z(Layer.blockOver);
        Draw.rect(rotatorRegion, build.x, build.y, r - 180f);
        Draw.rect(capRegion, build.x, build.y, r - 180f);
        Draw.z(Layer.block);
        Draw.rect(topRegion, build.x, build.y, r - 180f);
        Draw.alpha(1);

        drawShadow(build);
    }

    @Override
    public void drawPlan(Block block, BuildPlan plan, Eachable<BuildPlan> list){
        Draw.rect(rotatorRegion, plan.drawx(), plan.drawy());
        Draw.rect(topRegion, plan.drawx(), plan.drawy());
    }

    public void drawShadow(Building b){
        float totalProgress = b.totalProgress() * rotateSpeed;
        shadowBufferDrawCalls.add(() -> {
            Draw.rect(topRegion, b.x + shadowOffset, b.y + shadowOffset, totalProgress);
            Draw.rect(capRegion, b.x + shadowOffset, b.y + shadowOffset, totalProgress);

            Lines.stroke(beamStroke);
            Lines.line(b.x, b.y, b.x + shadowOffset, b.y + shadowOffset, false);

            for(int i = 0; i <= shadowPrecision; i++) {
                float dx = b.x + minShadowOffset + (shadowOffset - minShadowOffset) / shadowPrecision * i;
                float dy = b.y + minShadowOffset + (shadowOffset - minShadowOffset) / shadowPrecision * i;

                Draw.rect(rotatorRegion, dx, dy, totalProgress);
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
