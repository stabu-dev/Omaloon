package omaloon.graphics;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.gl.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import arc.util.noise.*;
import mindustry.*;
import mindustry.game.*;
import mindustry.game.EventType.*;
import mindustry.graphics.*;
import mindustry.io.SaveFileReader.*;
import mindustry.io.*;
import mindustry.world.*;

import java.io.*;
import java.util.function.*;

public class OlRenderer{
    public static DarknessChunk darknessChunk;

    public static boolean darkensTile(Tile tile){
        if(tile == null) return false;

        return usesEditorDarknessRules() ? withEditorDarknessRules(() -> tile.block().isDarkened(tile)) : tile.block().isDarkened(tile);
    }

    private static boolean usesEditorDarknessRules(){
        return Vars.state.isMenu();
    }

    private static boolean withEditorDarknessRules(BooleanSupplier supplier){
        boolean prevEditor = Vars.state.rules.editor;
        Vars.state.rules.editor = true;
        try{
            return supplier.getAsBoolean();
        }finally{
            Vars.state.rules.editor = prevEditor;
        }
    }

    public static void init(){
        SaveVersion.addCustomChunk("omaloon-darkness", darknessChunk = new DarknessChunk());
    }

    /**
     * Thing that handles custom darkness. Do not mess with its io unless you're willing to make a revision system for backwards compatibility.
     * @author Liz
     */
    public static class DarknessChunk implements CustomChunk{
        private static final byte[] paintedDarknessSteps = {0, 1, 2, 3, 4, 5};

        public byte[] darkness;
        public boolean updated;
        private boolean lastMenuState = Vars.state.isMenu();

        public DarknessChunk(){
            Events.on(EventType.WorldLoadBeginEvent.class, event -> {
                darkness = null;
                updated = false;
                lastMenuState = Vars.state.isMenu();
            });
            Events.run(Trigger.draw, () -> {
                boolean menu = Vars.state.isMenu();
                if(lastMenuState != menu){
                    updated = false;
                    lastMenuState = menu;
                }

                if(!updated && !Vars.state.isMenu()){
                    updatePaintedDarkness();
                    updated = true;
                }
            });
            Events.on(EventType.TileChangeEvent.class, event -> updated = false);
        }

        public void clearDarknessMap(){
            darkness = null;
            updated = false;
        }

        public void initDarknessMap(){
            darkness = new byte[Vars.world.width() * Vars.world.height()];
            updated = false;
        }

        public void putDarkness(int x, int y, byte value){
            int index = x + y * Vars.world.width();
            byte normalized = normalizePaintedDarkness(Byte.toUnsignedInt(value));

            if(darkness == null) initDarknessMap();

            if(index < 0 || index >= darkness.length) return;

            darkness[index] = normalized;
            updated = false;
        }

        public void updatePaintedDarkness(){
            Mat prevProj = new Mat().set(Draw.proj());
            FrameBuffer dark = Reflect.get(BlockRenderer.class, Vars.renderer.blocks, "dark");

            dark.getTexture().setFilter(Texture.TextureFilter.linear);
            dark.resize(Vars.world.width(), Vars.world.height());
            dark.begin(Vars.state.rules.limitMapArea ? Color.black : Color.white);
            Draw.proj().setOrtho(0, 0, dark.getWidth(), dark.getHeight());

            if(Vars.state.rules.limitMapArea){
                Draw.color(Color.white);
                Fill.crect(Vars.state.rules.limitX, Vars.state.rules.limitY, Vars.state.rules.limitWidth, Vars.state.rules.limitHeight);
            }

            for(Tile tile : Vars.world.tiles){
                if(Vars.state.rules.limitMapArea
                && !Rect.contains(Vars.state.rules.limitX, Vars.state.rules.limitY, Vars.state.rules.limitWidth - 1, Vars.state.rules.limitHeight - 1, tile.x, tile.y)){
                    continue;
                }

                float darkness = getRenderedDarkness(tile.x, tile.y);

                if(darkness > 0f){
                    float darkValue = 1f - Math.min((darkness + 0.5f) / 4f, 1f);
                    Draw.colorl(darkValue);
                    Fill.rect(tile.x + 0.5f, tile.y + 0.5f, 1, 1);
                }
            }

            Draw.flush();
            Draw.color();
            dark.end();

            Draw.proj(prevProj);
            syncMinimapDarkness();
        }

        @Override
        public void read(DataInput stream) throws IOException{
            int len = stream.readInt();

            if(len != 0){
                darkness = new byte[len];

                for(int i = 0; i < len; i++){
                    byte read = stream.readByte();

                    darkness[i] = normalizePaintedDarkness(Byte.toUnsignedInt(read));
                }
            }else darkness = null;

            updated = false;
        }

        @Override
        public void write(DataOutput stream) throws IOException{
            int len = darkness == null ? 0 : darkness.length;

            stream.writeInt(len);
            if(darkness != null) for(byte value : darkness){
                stream.writeByte(value);
            }
        }

        private static byte normalizePaintedDarkness(int raw){
            float value = raw <= 5 ? raw : Mathf.clamp(raw / 255f * 5f, 0f, 5f);
            byte nearest = paintedDarknessSteps[0];
            float best = Float.MAX_VALUE;

            for(byte step : paintedDarknessSteps){
                float diff = Math.abs(value - step);
                if(diff < best){
                    best = diff;
                    nearest = step;
                }
            }

            return nearest;
        }

        private float getPaintedDarkness(int x, int y){
            if(darkness == null) return 0f;

            int index = x + y * Vars.world.width();
            if(index < 0 || index >= darkness.length) return 0f;

            return Byte.toUnsignedInt(darkness[index]);
        }

        private float getRenderedDarkness(int x, int y){
            return Math.max(getBaseDarkness(x, y), getPaintedDarkness(x, y));
        }

        private float getBaseDarkness(int x, int y){
            float dark = 0f;

            if(Vars.state.rules.borderDarkness){
                int edgeBlend = 2;
                int edgeDst;

                if(!Vars.state.rules.limitMapArea){
                    edgeDst = Math.min(x, Math.min(y, Math.min(-(x - (Vars.world.tiles.width - 1)), -(y - (Vars.world.tiles.height - 1)))));
                }else{
                    edgeDst =
                        Math.min(x - Vars.state.rules.limitX,
                        Math.min(y - Vars.state.rules.limitY,
                        Math.min(-(x - (Vars.state.rules.limitX + Vars.state.rules.limitWidth - 1)), -(y - (Vars.state.rules.limitY + Vars.state.rules.limitHeight - 1)))));
                }

                if(edgeDst <= edgeBlend){
                    dark = Math.max((edgeBlend - edgeDst) * (4f / edgeBlend), dark);
                }
            }

            if(Vars.state.hasSector() && Vars.state.getSector().preset == null){
                int circleBlend = 5;
                float offset = Vars.state.getSector().rect.rotation + 90f;
                float angle = Angles.angle(x, y, Vars.world.tiles.width / 2f, Vars.world.tiles.height / 2f) + offset;
                int sides = Vars.state.getSector().tile.corners.length;
                float step = 360f / sides;
                float prev = Mathf.round(angle, step);
                float next = prev + step;
                float length = Vars.state.getSector().getSize() / 2f;
                float rawDst = Intersector.distanceLinePoint(
                    Tmp.v1.trns(prev, length),
                    Tmp.v2.trns(next, length),
                    Tmp.v3.set(x - Vars.world.tiles.width / 2f, y - Vars.world.tiles.height / 2f).rotate(offset)
                ) / Mathf.sqrt3 - 1f;

                rawDst += Noise.noise(x, y, 11f, 7f) + Noise.noise(x, y, 22f, 15f);

                int circleDst = (int)(rawDst - (length - circleBlend));
                if(circleDst > 0){
                    dark = Math.max(circleDst, dark);
                }
            }

            Tile tile = Vars.world.tile(x, y);
            if(darkensTile(tile)){
                dark = Math.max(dark, tile.data);
            }

            return dark;
        }

        private void syncMinimapDarkness(){
            if(Vars.renderer == null) return;

            boolean needsFullRefresh = Vars.state.isMenu() || darkness != null;
            if(!needsFullRefresh) return;

            var minimap = Vars.renderer.minimap;
            Pixmap pixmap = minimap.getPixmap();
            Texture texture = minimap.getTexture();
            if(pixmap == null || texture == null || pixmap.isDisposed() || texture.isDisposed()) return;

            minimap.updateAll();

            for(Tile tile : Vars.world.tiles){
                float vanillaDarkness = Vars.world.getDarkness(tile.x, tile.y);
                float renderedDarkness = getRenderedDarkness(tile.x, tile.y);
                if(renderedDarkness <= vanillaDarkness) continue;

                float vanillaScale = 1f - Mathf.clamp(vanillaDarkness / 4f);
                if(vanillaScale <= 0f) continue;

                float renderedScale = 1f - Mathf.clamp(renderedDarkness / 4f);
                int py = pixmap.height - 1 - tile.y;
                pixmap.set(tile.x, py, Tmp.c1.rgba8888(pixmap.get(tile.x, py)).mul(renderedScale / vanillaScale).rgba());
            }

            texture.draw(pixmap);
        }
    }
}
