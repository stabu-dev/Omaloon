package ol.tools;

import arc.Graphics;
import arc.*;
import arc.backend.sdl.SdlApplication.*;
import arc.backend.sdl.*;
import arc.files.*;
import arc.graphics.Color;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.gl.*;
import arc.mock.*;
import arc.util.*;

import java.awt.*;
import java.awt.image.*;
import java.util.*;

import static arc.Core.batch;

public class IconRasterizer{
    Graphics2D out = null;
    BufferedImage outImage = null;
    float width, height;

    public static void main(String[] inputArgs){
        try{
            executeMain(inputArgs);
        }catch(SdlError error){
            Log.warn("Cannot run " + IconRasterizer.class.getSimpleName() + ", reason: @", Strings.getStackTrace(error));
        }
    }

    private static void executeMain(String[] inputArgs){
        int[] sizes = new int[Structs.count(inputArgs, Strings::canParseInt)];

        for(int i = 0, sizeI = 0; i < inputArgs.length; i++){
            if(!Strings.canParseInt(inputArgs[i])) continue;
            sizes[sizeI++] = Strings.parseInt(inputArgs[i]);
        }
        System.out.println("sizes: " + Arrays.toString(sizes));
        /*
        Process for adding an icon to the font:
        1. Have an SVG ready, possibly created with this tool.
        2. Go to Fontello and load the config.json from core/assets-raw/fontgen/config.json
        3. Drag the SVG in.
        4. Export the config and font file, replace the old config.
        5. Take the font (ttf) from the zip, open it in FontForge, and merge it into font.woff and icon.ttf. Usually, you would do view -> go to (the 0x unicode index).
        **/
        Fi rootDirectory = Fi.get("../../../assets-raw");
        if(Core.atlas == null){
            rootDirectory = Fi.get("resources/assets-raw");
        }
//        System.out.println(rootDirectory.file().getAbsoluteFile().getAbsoluteFile().getAbsolutePath());
//        if (true) throw null;
        Log.info("Converting icons...");
        Time.mark();
        Fi svgOutputFolder = rootDirectory.child("sprites/ui/svg-icons");
        svgOutputFolder.deleteDirectory();
        Fi[] list = rootDirectory.child("icons").list();

//        Seq<Fi> files = new Seq<>();
        Batch prevBatch = batch;
        Graphics prefGraphics = Core.graphics;
        Application prevApp = Core.app;
        TextureAtlas prevAtlas = Core.atlas;
        Core.graphics = new MockGraphics();
        Core.app = new MockApplication();
        Core.gl = Core.gl20 = /*gl20 =*/ new SdlGL20();
/*        if (!Core.atlas.find("white").found()) {
            Core.atlas.addRegion("write", Pixmaps.blankTextureRegion());
            TextureAtlas.AtlasRegion white = Core.atlas.find("white");
            Reflect.set(Core.atlas, "white", white);
        }*/
        long window = GlSetup.init();

        Core.atlas = TextureAtlas.blankAtlas();
        batch = new SpriteBatch();

//        Fill.rect(0, 0, 1, 1);
        Reflect.set(Batch.class, batch, "lastTexture", Core.atlas.white().texture);
        System.out.println(rootDirectory.child("list::").absolutePath());
        for(Fi img : list){
            System.out.println(img);
            if(img.extension().equals("png")){
//                Fi dst = iconsFolder.child(img.nameWithoutExtension().replace("icon-", "") + ".svg");
                String fileName = img.nameWithoutExtension()/*.replace("icon-", "")*/;
//                dst.copyTo(iconsPartFolder.child(dst.name()));
                //dst.copyTo(svgOutputFolder.child(dst.name()));
                for(int size : sizes){
                    new IconRasterizer().convert(new Pixmap(img), size, svgOutputFolder.child(fileName + "-" + size + ".png"));
                    /*Main main = new Main(new String[]{
//                    "-d",svgOutputFolder.absolutePath()+"/"+dst.nameWithoutExtension()+"-"+size+".png",
                    "-w", size + "",
                    "-h", size + "",
                    dst.file().getAbsoluteFile().getAbsolutePath(),
                    "-scriptSecurityOff"
                    });
                    main.execute();*/
                }
//                files.add(dst);
            }
        }

        Fi svgIcons = rootDirectory.sibling("assets/sprites/ui/svg-icons");
        svgIcons.deleteDirectory();
        for(Fi fi : svgOutputFolder.list()){
            fi.copyTo(svgIcons.child(fi.name()));
        }

        GlSetup.disable(window);
        batch = prevBatch;
        Core.atlas = prevAtlas;
        Core.app = prevApp;
        Core.graphics = prefGraphics;
        Log.info("Done converting icons in &lm@&lgs.", Time.elapsed() / 1000f);
//        System.exit(0);
    }

    void convert(Pixmap pixmap, int size, Fi output){
        boolean[][] grid = new boolean[pixmap.width][pixmap.height];

        for(int x = 0; x < pixmap.width; x++){
            for(int y = 0; y < pixmap.height; y++){
                grid[x][pixmap.height - 1 - y] = !pixmap.empty(x, y);
            }
        }

        float xscl = 1f, yscl = 1f;//resolution / (float)pixmap.getWidth(), yscl = resolution / (float)pixmap.getHeight();
        float scl = xscl;

        width = pixmap.width;
        height = pixmap.height;
        FrameBuffer buffer = new FrameBuffer(size, size);
        Texture.TextureFilter filter = Texture.TextureFilter.linear;
        buffer.getTexture().setFilter(filter, filter);
        buffer.begin(Color.clear);
//        outImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
//        out = (Graphics2D) outImage.getGraphics();
//        out.setColor(Color.WHITE);
//        float scaleX = outImage.getWidth() / width;
//        float scaleY = outImage.getHeight() / height;
//        Draw.scl(scaleX, scaleY);
        Draw.proj(0, 0, width, height);
        for(int x = -1; x < pixmap.width; x++){
            for(int y = -1; y < pixmap.height; y++){
                int index = index(x, y, pixmap.width, pixmap.height, grid);

                float leftx = x * xscl, boty = y * yscl, rightx = x * xscl + xscl, topy = y * xscl + yscl,
                    midx = x * xscl + xscl / 2f, midy = y * yscl + yscl / 2f;

                switch(index){
                    case 0:
                        break;
                    case 1:
                        tri(
                            leftx, midy,
                            leftx, topy,
                            midx, topy
                        );
                        break;
                    case 2:
                        tri(
                            midx, topy,
                            rightx, topy,
                            rightx, midy
                        );
                        break;
                    case 3:
                        rect(leftx, midy, scl, scl / 2f);
                        break;
                    case 4:
                        tri(
                            midx, boty,
                            rightx, boty,
                            rightx, midy
                        );
                        break;
                    case 5:
                        //ambiguous

                        //7
                        tri(
                            leftx, midy,
                            midx, midy,
                            midx, boty
                        );

                        //13
                        tri(
                            midx, topy,
                            midx, midy,
                            rightx, midy
                        );

                        rect(leftx, midy, scl / 2f, scl / 2f);
                        rect(midx, boty, scl / 2f, scl / 2f);

                        break;
                    case 6:
                        rect(midx, boty, scl / 2f, scl);
                        break;
                    case 7:
                        //invert triangle
                        tri(
                            leftx, midy,
                            midx, midy,
                            midx, boty
                        );

                        //3
                        rect(leftx, midy, scl, scl / 2f);

                        rect(midx, boty, scl / 2f, scl / 2f);
                        break;
                    case 8:
                        tri(
                            leftx, boty,
                            leftx, midy,
                            midx, boty
                        );
                        break;
                    case 9:
                        rect(leftx, boty, scl / 2f, scl);
                        break;
                    case 10:
                        //ambiguous

                        //11
                        tri(
                            midx, boty,
                            midx, midy,
                            rightx, midy
                        );

                        //14
                        tri(
                            leftx, midy,
                            midx, midy,
                            midx, topy
                        );

                        rect(midx, midy, scl / 2f, scl / 2f);
                        rect(leftx, boty, scl / 2f, scl / 2f);

                        break;
                    case 11:
                        //invert triangle

                        tri(
                            midx, boty,
                            midx, midy,
                            rightx, midy
                        );

                        //3
                        rect(leftx, midy, scl, scl / 2f);

                        rect(leftx, boty, scl / 2f, scl / 2f);
                        break;
                    case 12:
                        rect(leftx, boty, scl, scl / 2f);
                        break;
                    case 13:
                        //invert triangle

                        tri(
                            midx, topy,
                            midx, midy,
                            rightx, midy
                        );

                        //12
                        rect(leftx, boty, scl, scl / 2f);

                        rect(leftx, midy, scl / 2f, scl / 2f);
                        break;
                    case 14:
                        //invert triangle

                        tri(
                            leftx, midy,
                            midx, midy,
                            midx, topy
                        );

                        //12
                        rect(leftx, boty, scl, scl / 2f);

                        rect(midx, midy, scl / 2f, scl / 2f);
                        break;
                    case 15:
                        square(midx, midy, scl);
                        break;
                }
            }
        }
        buffer.end();
        System.out.println(output.absolutePath());
        output.writePng(toPixmap(buffer));


//        out = null;
//        outImage = null;
//        output.writeString(out.toString());
    }

    public Pixmap toPixmap(FrameBuffer buffer){
        buffer.begin();
        int h = buffer.getHeight();
        int w = buffer.getWidth();
        byte[] lines = ScreenUtils.getFrameBufferPixels(0, 0, w, h, true);
        buffer.end();

        for(int i = 0; i < lines.length; i += 4){
//            lines[i + 3] = (byte) 255;
        }
        Pixmap fullPixmap = new Pixmap(w, h);
        Buffers.copy(lines, 0, fullPixmap.pixels, lines.length);
        return fullPixmap;
    }

    void square(float x, float y, float size){
        rect(x - size / 2f, y - size / 2f, size, size);
    }

    void tri(float x1, float y1, float x2, float y2, float x3, float y3){
//        float scaleX = outImage.getWidth() / width;
//        float scaleY = outImage.getHeight() / height;
        Fill.tri(
            x1 + 0.5f, flip(y1 + 0.5f),
            x2 + 0.5f, flip(y2 + 0.5f),
            x3 + 0.5f, flip(y3 + 0.5f)
        );
       /* Tri tri = new Tri(
                new Vec2(x1 + 0.5f, flip(y1 + 0.5f)).scl(scaleX, scaleY),
                new Vec2(x2 + 0.5f, flip(y2 + 0.5f)).scl(scaleX, scaleY),
                new Vec2(x3 + 0.5f, flip(y3 + 0.5f)).scl(scaleX, scaleY)
        );
*/
        /*out.draw(tri);*/
    }

    void rect(float x1, float y1, float width, float height){
//        Fill.quad(x1 + 0.5f, flip(y1 + 0.5f), width, height);
        float x = x1 + 0.5f;
        float y = flip(y1 + 0.5f);
        Fill.quad(
            x, y,
            x + width, y,
            x + width, y + height,
            x, y + height
        );
        /*float scaleX = outImage.getWidth() / width;
        float scaleY = outImage.getHeight() / height;
        Square square = new Square(
                new Vec2(Tmp.v1.set(x1 + 0.5f, flip(y1 + 0.5f))).scl(scaleX, scaleY),
                new Vec2(Tmp.v1.x + width, Tmp.v1.y).scl(scaleX, scaleY),
                new Vec2(Tmp.v1.x + width, Tmp.v1.y + height).scl(scaleX, scaleY),
                new Vec2(Tmp.v1.x, Tmp.v1.y + height).scl(scaleX, scaleY)
        );

        out.draw(square);*/
    }

    float flip(float y){
        return y;
    }

    int index(int x, int y, int w, int h, boolean[][] grid){
        int botleft = sample(grid, x, y);
        int botright = sample(grid, x + 1, y);
        int topright = sample(grid, x + 1, y + 1);
        int topleft = sample(grid, x, y + 1);
        return (botleft << 3) | (botright << 2) | (topright << 1) | topleft;
    }

    int sample(boolean[][] grid, int x, int y){
        return (x < 0 || y < 0 || x >= grid.length || y >= grid[0].length) ? 0 : grid[x][y] ? 1 : 0;
    }
}
