package omaloon.utils;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.ui.*;

import java.io.*;
import java.util.*;

public class IconLoader{
    public static void loadIcons(){
        Seq<Font> availableFonts = Seq.with(Fonts.def, Fonts.outline);
        int fontSize = (int)(Fonts.def.getData().lineHeight / Fonts.def.getData().scaleY);

        Properties iconProperties = new Properties();
        try(Reader reader = Vars.tree.get("icons/omaloon-icons.properties").reader(512)){
            iconProperties.load(reader);
        }catch(Exception e){
            return;
        }

        for(Map.Entry<Object, Object> entry : iconProperties.entrySet()){
            String codePointStr = (String)entry.getKey();
            String[] valueParts = ((String)entry.getValue()).split("\\|");
            if(valueParts.length < 2){
                continue;
            }

            try{
                int codePoint = Integer.parseInt(codePointStr);
                String textureName = valueParts[1];
                TextureRegion region = Core.atlas.find(textureName);

                Vec2 scaledSize = Scaling.fit.apply(region.width, region.height, fontSize, fontSize);
                Font.Glyph glyph = constructGlyph(codePoint, region, scaledSize, fontSize);

                for(Font font : availableFonts){
                    font.getData().setGlyph(codePoint, glyph);
                }

            }catch(Exception ignored){
            }
        }
    }

    private static Font.Glyph constructGlyph(int id, TextureRegion region, Vec2 size, int fontSize){
        Font.Glyph glyph = new Font.Glyph();
        glyph.id = id;
        glyph.srcX = 0;
        glyph.srcY = 0;
        glyph.width = (int)size.x;
        glyph.height = (int)size.y;
        glyph.u = region.u;
        glyph.v = region.v2;
        glyph.u2 = region.u2;
        glyph.v2 = region.v;
        glyph.xoffset = 0;
        glyph.yoffset = -fontSize;
        glyph.xadvance = fontSize;
        glyph.fixedWidth = true;
        glyph.page = 0;
        return glyph;
    }
}

