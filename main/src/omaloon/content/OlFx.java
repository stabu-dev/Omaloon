package omaloon.content;

import arc.graphics.g2d.*;
import arc.math.*;
import mindustry.entities.*;

import static arc.graphics.g2d.Draw.*;

public class OlFx{
    public static final Effect
    glacied = new Effect(80f, e -> {
        color(OlStatusEffects.glacied.color);
        alpha(Mathf.clamp(e.fin() * 2f));

        Fill.circle(e.x, e.y, e.fout());
    });
}
