package omaloon.tools;

import arc.files.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.struct.*;

import static omaloon.tools.Tools.*;

/**
 * A specialized thread-safe texture atlas for generated texture regions. No {@link Texture}s are instantiated; use
 * {@link GenRegion#pixmap()} instead.
 * @author GlennFolker
 */
public class GenAtlas extends TextureAtlas{
    private final ObjectMap<String, GenRegion> regions = new ObjectMap<>();
    public GenRegion clear;

    /** The name should be prefixed with {@code modName-} */
    public GenRegion addRegion(Fi file){
        String name = file.nameWithoutExtension();
        if(!file.path().contains("sprites/vanilla/")) name = System.getProperty("currentModName") + "-" + name;

        GenRegion reg = new GenRegion(name, new Pixmap(file));

        String path = file.path();
        path = path.substring(path.indexOf("sprites/") + "sprites/".length(), path.lastIndexOf("/"));

        reg.relativePath = path;

        synchronized(regions){
            regions.put(name, reg);
            return reg;
        }
    }

    /** @inheritDocs. The name should be prefixed with {@code modName-} */
    @Override
    public GenRegion addRegion(String name, TextureRegion textureRegion){
        synchronized(regions){
            GenRegion reg = (GenRegion)textureRegion;

            regions.put(name, reg);
            return reg;
        }
    }

    /** @inheritDocs. The name should be prefixed with {@code modName-} */
    @Override
    public GenRegion addRegion(String name, Texture texture, int x, int y, int width, int height){
        Pixmap pixmap = texture.getTextureData().getPixmap();
        GenRegion reg = new GenRegion(name, Pixmaps.crop(pixmap, x, y, width, height));

        synchronized(regions){
            regions.put(name, reg);
            return reg;
        }
    }

    @Override
    public GenRegion find(String name){
        synchronized(regions){
            if(!regions.containsKey(name)){
                return new GenRegion(name, null);
            }

            return regions.get(name);
        }
    }

    @Override
    public GenRegion find(String name, String def){
        synchronized(regions){
            return regions.get(name, find(def));
        }
    }

    @Override
    public GenRegion find(String name, TextureRegion def){
        synchronized(regions){
            return regions.get(name, (GenRegion)def);
        }
    }

    @Override
    public PixmapRegion getPixmap(AtlasRegion region){
        if(region instanceof GenRegion gen){
            return new PixmapRegion(gen.pixmap());
        }
        return super.getPixmap(region);
    }

    @Override
    public boolean has(String s){
        synchronized(regions){
            return regions.containsKey(s);
        }
    }

    @Override
    public void dispose(){
        synchronized(regions){
            for(var pix : regions.values()){
                if(pix.pixmap != null) pix.pixmap.dispose();
            }

            regions.clear();
        }
    }

    public static class GenRegion extends AtlasRegion{
        private final Pixmap pixmap;
        public String relativePath = "";

        public GenRegion(String name, Pixmap pixmap){
            this.name = name;
            this.pixmap = pixmap;

            if(pixmap != null){
                width = pixmap.width;
                height = pixmap.height;
            }

            u = v = 0f;
            u2 = v2 = 1f;
        }

        @Override
        public boolean found(){
            return pixmap != null;
        }

        @Override
        public TextureRegion[][] split(int tileWidth, int tileHeight){
            if(tileWidth <= 0 || tileHeight <= 0) return new GenRegion[0][0];
            int rows = height / tileHeight;
            int cols = width / tileWidth;

            GenRegion[][] tiles = new GenRegion[rows][cols];
            for(int row = 0; row < rows; row++){
                for(int col = 0; col < cols; col++){
                    int rx = col * tileWidth;
                    int ry = row * tileHeight;
                    Pixmap croppedPixmap = pixmap != null ? Pixmaps.crop(pixmap, rx, ry, tileWidth, tileHeight) : null;
                    tiles[row][col] = new GenRegion(name + "-split-" + row + "-" + col, croppedPixmap);
                }
            }
            return tiles;
        }

        /**
         * @return The pixmap associated with this region. Must not be directly modified
         * @see Pixmap#copy()
         * @see Pixmap#dispose()
         */
        public Pixmap pixmap(){
            if(!found()) throw new IllegalArgumentException("Region does not exist: " + name);
            return pixmap;
        }

        public void save(){
            save(true);
        }

        public void save(boolean add){
            if(!found()) throw new IllegalArgumentException("Cannot save an invalid region: " + name);

            Fi dir = spritesDir.child(relativePath);
            dir.mkdirs();

            Fi file = dir.child(name.replaceFirst(System.getProperty("currentModName") + "-", "") + ".png");
            file.writePng(pixmap);

            if(add) atlas.addRegion(name, this);
        }
    }
}
