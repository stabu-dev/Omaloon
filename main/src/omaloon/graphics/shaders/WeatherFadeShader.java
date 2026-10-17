package omaloon.graphics.shaders;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.gl.*;
import arc.math.*;
import arc.struct.*;
import mindustry.gen.*;
import omaloon.graphics.*;
import omaloon.world.blocks.defense.*;

import java.util.*;

import static omaloon.graphics.OlShaders.file;

/**
 * Fades weather drawing inside shelter fields.
 * @author stabu_
 */
public class WeatherFadeShader extends Shader{
    /** Half the span of a side lobe, in degrees. */
    public static final float lobeHalfSpan = 45f;
    /** Degrees each part reaches past its seams, so the arc and its lobes merge instead of leaving a gap. */
    public static final float fieldOverlap = 4f;
    /** {@code GL_MAX_FRAGMENT_UNIFORM_COMPONENTS}; Arc only names the vector count that GLES reports. */
    private static final int maxFragmentUniformComponents = 0x8B49;
    /** How many fields fit the driver's uniform budget, three vec4s each. */
    public static final int maxFields;
    /** Cosine and sine of the overlap-expanded lobe half-span, injected as {@code LOBE_COS}/{@code LOBE_SIN}. */
    private static final float lobeHalfCos, lobeHalfSin;

    static{
        int vectors = Core.app.isMobile() ? Gl.getInt(Gl.maxFragmentUniformVectors) : Gl.getInt(maxFragmentUniformComponents) / 4;
        maxFields = Mathf.clamp((vectors - 2) / 3, 5, 16);
        float lobeHalf = (lobeHalfSpan + fieldOverlap * 0.5f) * Mathf.degRad;
        lobeHalfCos = Mathf.cos(lobeHalf);
        lobeHalfSin = Mathf.sin(lobeHalf);
    }

    /** Faintest alpha weather may reach inside a field. */
    public float minAlpha = 0.15f;

    private final float[] fields = new float[maxFields * 4];
    private final float[] arcs = new float[maxFields * 4];
    private final float[] lobes = new float[maxFields * 4];
    private int count;

    private static final Seq<Shelter.ShelterBuild> visible = new Seq<>();

    public WeatherFadeShader(){
        super(file("weather-fade.vert").readString(),
            "#define MAX_FIELDS " + maxFields + "\n"
            + "#define LOBE_COS " + lobeHalfCos + "\n"
            + "#define LOBE_SIN " + lobeHalfSin + "\n"
            + file("weather-fade.frag").readString());
    }

    public void clearFields(){
        count = 0;
        Arrays.fill(fields, 0f);
        Arrays.fill(arcs, 0f);
        Arrays.fill(lobes, 0f);
    }

    /** Registers a field; a part with a zero radius is treated as absent. */
    public void addField(float x, float y, float coreRadius, float arcRadius, float arcStart, float arcSpan, float leftRadius, float rightRadius){
        if(count >= maxFields) return;

        int f = count * 4;
        float half = arcSpan * 0.5f * Mathf.degRad, mid = (arcStart + arcSpan * 0.5f) * Mathf.degRad;
        float arcHalf = half + fieldOverlap * 0.5f * Mathf.degRad;

        fields[f] = x;
        fields[f + 1] = y;
        fields[f + 2] = coreRadius;
        fields[f + 3] = arcRadius;

        arcs[f] = Mathf.cos(mid);
        arcs[f + 1] = Mathf.sin(mid);
        arcs[f + 2] = Mathf.cos(arcHalf);
        arcs[f + 3] = Mathf.sin(arcHalf);

        lobes[f] = Mathf.cos(half + lobeHalfSpan * Mathf.degRad);
        lobes[f + 1] = Mathf.sin(half + lobeHalfSpan * Mathf.degRad);
        lobes[f + 2] = leftRadius;
        lobes[f + 3] = rightRadius;

        count++;
    }

    /**
     * Binds the fade around one weather draw pass; the nearest fields win beyond {@link #maxFields}.
     * Weather-agnostic. Any weather can wrap its drawing in this and {@link #end(boolean)}.
     * @return whether {@link #end(boolean)} has to unbind.
     */
    public static boolean begin(){
        WeatherFadeShader shader = OlShaders.weatherFade;
        if(shader == null) return false;

        visible.clear();
        float camX = Core.camera.position.x, camY = Core.camera.position.y;
        float halfW = Core.camera.width / 2f, halfH = Core.camera.height / 2f;

        for(Building b : Groups.build){
            if(!(b instanceof Shelter.ShelterBuild s) || s.visualScale <= 0.001f) continue;

            float radius = s.fieldMaxRadius();
            if(radius <= 0f || Math.abs(s.x - camX) > halfW + radius || Math.abs(s.y - camY) > halfH + radius) continue;

            if(visible.size < maxFields){
                visible.add(s);
                continue;
            }

            int farthest = 0;
            float distance = visible.first().dst(camX, camY);
            for(int i = 1; i < visible.size; i++){
                float d = visible.get(i).dst(camX, camY);
                if(d > distance){
                    distance = d;
                    farthest = i;
                }
            }
            if(s.dst(camX, camY) < distance) visible.set(farthest, s);
        }

        if(visible.isEmpty()) return false;

        shader.clearFields();
        for(Shelter.ShelterBuild s : visible){
            shader.addField(s.x, s.y, s.fieldInnerRadius(), s.fieldOuterRadius(), s.currentRotation, s.currentArcLength, s.fieldLobeRadius(s.currentLRadius), s.fieldLobeRadius(s.currentRRadius));
        }

        Draw.shader(shader);
        return true;
    }

    /** Unbinds the fade when {@link #begin()} bound it. */
    public static void end(boolean faded){
        if(faded) Draw.shader();
    }

    @Override
    public void apply(){
        int slots = Math.min(count + 1, maxFields);

        for(int i = 0; i < slots; i++){
            int f = i * 4;
            setUniformf("u_fields[" + i + "]", fields[f], fields[f + 1], fields[f + 2], fields[f + 3]);
            setUniformf("u_arcs[" + i + "]", arcs[f], arcs[f + 1], arcs[f + 2], arcs[f + 3]);
            setUniformf("u_lobes[" + i + "]", lobes[f], lobes[f + 1], lobes[f + 2], lobes[f + 3]);
        }
        setUniformf("u_minAlpha", minAlpha);
    }
}
