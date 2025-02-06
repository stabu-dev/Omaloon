package omaloon.ui.elements;

import arc.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.scene.style.*;
import arc.scene.ui.layout.*;
import arc.util.pooling.*;
import mindustry.gen.*;
import mindustry.ui.*;

public class CenterBar extends Bar{
    private Floatp fraction;
    private CharSequence name = "";
    private float value, lastValue, blink, outlineRadius;
    private Color blinkColor = new Color(), outlineColor = new Color();

    public CenterBar(String name, Color color, Floatp fraction){
        this.fraction = fraction;
        this.name = Core.bundle.get(name, name);
        this.blinkColor.set(color);
        lastValue = value = fraction.get();
        setColor(color);
    }

    public CenterBar(Prov<CharSequence> name, Prov<Color> color, Floatp fraction){
        this.fraction = fraction;
        lastValue = value = Mathf.clamp(fraction.get());
        update(() -> {
            this.name = name.get();
            this.blinkColor.set(color.get());
            setColor(color.get());
        });
    }

    public CenterBar(){

    }

    public void reset(float value){
        this.value = lastValue = blink = value;
    }

    public void set(Prov<String> name, Floatp fraction, Color color){
        this.fraction = fraction;
        this.lastValue = fraction.get();
        this.blinkColor.set(color);
        setColor(color);
        update(() -> this.name = name.get());
    }

    public void snap(){
        lastValue = value = fraction.get();
    }

    public Bar outline(Color color, float stroke){
        outlineColor.set(color);
        outlineRadius = Scl.scl(stroke);
        return this;
    }

    public void flash(){
        blink = 1f;
    }

    public Bar blink(Color color){
        blinkColor.set(color);
        return this;
    }

    @Override
    public void draw(){
        if(fraction == null) return;

        float computed = Mathf.clamp(fraction.get(), -1f, 1f);


        if(lastValue > computed){
            blink = 1f;
            lastValue = computed;
        }

        if(Float.isNaN(lastValue)) lastValue = 0;
        if(Float.isInfinite(lastValue)) lastValue = 1f;
        if(Float.isNaN(value)) value = 0;
        if(Float.isInfinite(value)) value = 1f;
        if(Float.isNaN(computed)) computed = 0;
        if(Float.isInfinite(computed)) computed = 1f;

        blink = Mathf.lerpDelta(blink, 0f, 0.2f);
        value = Mathf.lerpDelta(value, computed, 0.15f);

        Drawable bar = Tex.bar;

        if(outlineRadius > 0){
            Draw.color(outlineColor);
            bar.draw(x - outlineRadius, y - outlineRadius, width + outlineRadius * 2, height + outlineRadius * 2);
        }

        Draw.colorl(0.1f);
        Draw.alpha(parentAlpha);
        bar.draw(x, y, width, height);
        Draw.color(color, blinkColor, blink);
        Draw.alpha(parentAlpha);

        Drawable top = Tex.barTop;

        top.draw(
        x + (width / 2f - Core.atlas.find("bar-top").width / 2f) * (Math.min(value, 0f) + 1f),
        y,
        Core.atlas.find("bar-top").width + (-Core.atlas.find("bar-top").width / 2f + width / 2f) * Math.abs(value),
        height
        );

        Draw.color();

        Font font = Fonts.outline;
        GlyphLayout lay = Pools.obtain(GlyphLayout.class, GlyphLayout::new);
        lay.setText(font, name);

        font.setColor(1f, 1f, 1f, 1f);
        font.getCache().clear();
        font.getCache().addText(name, x + width / 2f - lay.width / 2f, y + height / 2f + lay.height / 2f + 1);
        font.getCache().draw(parentAlpha);

        Pools.free(lay);
    }
}
