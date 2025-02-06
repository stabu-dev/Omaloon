package omaloon.world.draw;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import mindustry.graphics.*;

public class Outliner{

    /** @author MEEP of Faith#7277 */
    public static void outlineRegion(MultiPacker packer, TextureRegion textureRegion, String outputName){
        outlineRegion(packer, textureRegion, Pal.darkerMetal, outputName, 3);
    }

    /** @author MEEP of Faith#7277 */
    public static void outlineRegion(MultiPacker packer, TextureRegion textureRegion, Color outlineColor, String outputName){
        outlineRegion(packer, textureRegion, outlineColor, outputName, 3);
    }

    /** @author MEEP of Faith#7277 */
    public static void outlineRegion(MultiPacker packer, TextureRegion[] textures, Color outlineColor, String outputName){
        outlineRegion(packer, textures, outlineColor, outputName, 4);
    }

    /**
     * Outlines a list of regions. Run in createIcons.
     * @author MEEP of Faith#7277
     */
    public static void outlineRegion(MultiPacker packer, TextureRegion[] textures, Color outlineColor, String outputName, int radius){
        for(int i = 0; i < textures.length; i++){
            outlineRegion(packer, textures[i], outlineColor, outputName + "-" + i, radius);
        }
    }

    /**
     * Outlines a given textureRegion. Run in createIcons.
     * @param textureRegion The texture you want to generate outline with
     * @param outlineColor The color1 of the outline, default is Pal.darkerMetal
     * @param outputName The outline name
     * @param outlineRadius The thiccness of the outline, default is 3 or 4
     * @author MEEP of Faith#7277
     */
    public static void outlineRegion(MultiPacker packer, TextureRegion textureRegion, Color outlineColor, String outputName, int outlineRadius){
        if(textureRegion == null) return;
        PixmapRegion region = Core.atlas.getPixmap(textureRegion);
        Pixmap out = Pixmaps.outline(region, outlineColor, outlineRadius);
        if(Core.settings.getBool("linear", true)){
            Pixmaps.bleed(out);
        }
        packer.add(MultiPacker.PageType.main, outputName, out);
    }
}
