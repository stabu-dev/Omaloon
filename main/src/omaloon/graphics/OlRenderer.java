package omaloon.graphics;

import arc.*;
import arc.graphics.g2d.*;
import arc.graphics.gl.*;
import arc.math.*;
import arc.util.*;
import mindustry.*;
import mindustry.game.EventType.*;
import mindustry.graphics.*;
import mindustry.io.SaveFileReader.*;
import mindustry.io.*;

import java.io.*;

public class OlRenderer{
    public static DarknessChunk darknessChunk;

    public static void init() {
        SaveVersion.addCustomChunk("omaloon-darkness", darknessChunk = new DarknessChunk());
    }

    /**
     * Thing that handles custom darkness. Do not mess with its io unless you're willing to make a revision system for backwards compatibility.
     * @author Liz
     */
    public static class DarknessChunk implements CustomChunk {
        public byte[] darkness;

        public DarknessChunk() {
            // apparently for some unknown reason this is called before the class even reads so, delay time
            Events.on(WorldLoadEvent.class, event -> {
                // TODO prefferably not delay this
                Time.runTask(10f, this::updatePaintedDarkness);
            });
        }

        public void initDarknessMap() {
            darkness = new byte[Vars.world.width() * Vars.world.height()];
        }

        public void clearDarknessMap() {
            darkness = null;
        }

        private void updatePaintedDarkness() {
            FrameBuffer dark = Reflect.get(BlockRenderer.class, Vars.renderer.blocks, "dark");

            if (darkness != null) {
                dark.begin();

                int wWidth = dark.getWidth();
                int wHeight = dark.getHeight();

                Draw.proj().setOrtho(0, 0, dark.getWidth(), dark.getHeight());
                for (int i = 0; i < darkness.length; i++) {
                    float alpha = Byte.toUnsignedInt(darkness[i]) / 255f;
                    if (Mathf.zero(alpha)) continue;
                    Draw.colorl(1 - alpha);
                    Fill.rect(i % wWidth + 0.5f, Mathf.floor(i / wWidth) + 0.5f, 1, 1);
                }
                dark.end();
            }
        }

        @Override
        public void read(DataInput stream) throws IOException {
            int len = stream.readInt();

            if (len != 0) {
                darkness = new byte[len];

                for (int i = 0; i < len; i++) {
                    byte read = stream.readByte();

                    darkness[i] = read;
                }
            } else darkness = null;
        }

        @Override
        public void write(DataOutput stream) throws IOException {
            int len = darkness == null ? 0 : darkness.length;

            stream.writeInt(len);
            if (darkness != null) for(byte value : darkness) {
                stream.writeByte(value);
            }
            darkness = null;
        }
    }
}
