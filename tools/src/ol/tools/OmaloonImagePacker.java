package ol.tools;

import mindustry.ctype.Content;
import mindustry.ctype.MappableContent;
import mma.tools.ModImagePacker;
import ol.*;
import ol.gen.*;
//import testmod.gen.TMContentRegions;

public class OmaloonImagePacker extends ModImagePacker {

    public OmaloonImagePacker() {
    }

    @Override
    protected void start() throws Exception {
        OlVars.create();

        super.start();
    }

    @Override
    protected void preCreatingContent() {
        super.preCreatingContent();

//        TMEntityMapping.init();
    }

    @Override
    protected void runGenerators() {
        IconRasterizer.main(new String[]{"32","64"});
        new OmaloonGenerators();
    }

    @Override
    protected void checkContent(Content content) {
        super.checkContent(content);
        if (content instanceof MappableContent){
            OlContentRegions.loadRegions((MappableContent)content);
//            TMContentRegions.loadRegions((MappableContent) content);
        }
    }

    public static void main(String[] args) throws Exception {
        new OmaloonImagePacker();
    }

}
