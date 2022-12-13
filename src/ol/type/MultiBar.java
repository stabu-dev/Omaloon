package ol.type;

import arc.Core;
import arc.func.Floatp;
import arc.func.Prov;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Font;
import arc.graphics.g2d.GlyphLayout;
import arc.graphics.g2d.ScissorStack;
import arc.math.Mathf;
import arc.math.geom.Rect;
import arc.scene.style.Drawable;
import arc.struct.Seq;
import arc.util.pooling.Pools;
import mindustry.gen.Tex;
import mindustry.ui.Bar;
import mindustry.ui.Fonts;

import java.util.concurrent.atomic.AtomicReference;

public class MultiBar extends Bar {
    private static final Rect scissor = new Rect();
    private String name = "";


    public MultiBar(String name, Seq<BarPart> barParts) {
        this.barParts=barParts;
        this.name = Core.bundle.get(name, name);
        this.update(this::updateParts);
    }

    public MultiBar(Prov<String> name, Seq<BarPart> barParts) {
        this.barParts=barParts;
        this.update(() -> {
            updateParts();
            try {
                this.name = (String) name.get();
            } catch (Exception var4) {
                this.name = "";
            }

        });
    }

    public void reset(float value) {
        float v=value/barParts.size;
        barParts.each((part)->{
            part.value = part.lastValue = part.blink = v;
        });
    }
    public void updateParts(){
        this.barParts.each(BarPart::update);
    }
    public void drawParts(){
        float[] offset=new float[]{x};

        this.barParts.each((part)->{
            part.draw(offset[0],this.y,this.width,this.height,this.barParts);
            offset[0]=part.offset;
        });
    }
    public void set(Prov<String> name, Seq<BarPart> barParts) {
        this.barParts=barParts;
        this.update(() -> {
            this.name = (String) name.get();
            updateParts();
        });
    }

    public Bar blink(Color color) {
        return this;
    }

    public static float normalize(float f) {
        if (Float.isNaN(f)) {
            return 0.0F;
        }

        if (Float.isInfinite(f)) {
            return 1.0F;
        }
        return f;
    }
    Seq<BarPart> barParts=new Seq<>();
    @Override
    public void draw() {
        if (this.barParts != null && this.barParts.size>0) {
            Drawable bar = Tex.bar;
            Draw.colorl(0.1F);
            bar.draw(this.x, this.y, this.width, this.height);

            drawParts();

            Draw.color();
            Font font = Fonts.outline;
            GlyphLayout lay = Pools.obtain(GlyphLayout.class, GlyphLayout::new);
            lay.setText(font, this.name);
            font.setColor(Color.white);
            font.draw(this.name, this.x + this.width / 2.0F - lay.width / 2.0F, this.y + this.height / 2.0F + lay.height / 2.0F + 1.0F);
            Pools.free(lay);
        }
    }

    public static class BarPart {
        public float lastValue = 0;
        public float blink = 0;
        public float value = 0;
        public Color color;
        public Color blinkColor = new Color();
        public Floatp fraction;
        Runnable runnable = () -> {
        };

        public BarPart(Color color, Floatp fraction) {
            this.fraction = fraction;
            this.blinkColor.set(color);
            this.lastValue = this.value = fraction.get();
            this.color = color;
        }

        public BarPart(Prov<Color> color, Floatp fraction) {
            this.fraction = fraction;

            try {
                this.lastValue = this.value = Mathf.clamp(fraction.get());
            } catch (Exception var5) {
                this.lastValue = this.value = 0.0F;
            }

            this.update(() -> {
                try {
                    this.blinkColor.set((Color) color.get());
                    this.color = color.get();
                } catch (Exception ignored) {
                }

            });
        }

        public void update(Runnable runnable) {
            this.runnable = runnable;
        }

        public void update() {
            runnable.run();
        }
        float x,y,width,height,offset;
        public void draw(float x,float y,float width,float height,Seq<BarPart> barParts) {
            if (this.fraction != null) {
                this.x=x;
                this.y=y;
                this.width=width;
                this.height=height;
                float computed;
                try {
                    computed = Mathf.clamp(this.fraction.get());
                } catch (Exception var7) {
                    computed = 0.0F;
                }

                if (this.lastValue > computed) {
                    this.blink = 1.0F;
                    this.lastValue = computed;
                }
                this.lastValue = normalize(this.lastValue);
                this.value = normalize(this.value);
                computed = normalize(computed);

                this.blink = Mathf.lerpDelta(this.blink, 0.0F, 0.2F);
                this.value = Mathf.lerpDelta(this.value, computed, 0.15F);
                Draw.color(this.color, this.blinkColor, this.blink);
                Drawable top = Tex.barTop;
                float topWidth = this.width * value;
                topWidth/=barParts.size;
                if (topWidth > (float) Core.atlas.find("bar-top").width) {
                    top.draw(this.x, this.y, topWidth, this.height);
                    this.offset=this.x+topWidth;
                } else if (ScissorStack.push(scissor.set(this.x, this.y, topWidth, this.height))) {
                    top.draw(this.x, this.y, (float) Core.atlas.find("bar-top").width, this.height);
                    this.offset=this.x+Core.atlas.find("bar-top").width;
                    this.offset=this.x+topWidth;
                    ScissorStack.pop();
                } else {
                    this.offset=this.x;
                }


            }
        }
    }
}