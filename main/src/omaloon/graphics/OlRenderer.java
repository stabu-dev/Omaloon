package omaloon.graphics;

import arc.util.*;
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

        @Override
        public void read(DataInput stream) throws IOException {
            int len = stream.readInt();

            if (len != 0) {
                darkness = new byte[len];

                for (int i = 0; i < len; i++) {
                    byte read = stream.readByte();

                    Log.info("read @", read);
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
