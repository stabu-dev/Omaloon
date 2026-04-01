package omaloon.editor;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.gl.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import mindustry.editor.*;
import mindustry.graphics.*;
import mindustry.world.*;
import mindustry.world.blocks.environment.*;
import omaloon.graphics.*;

import static mindustry.Vars.*;

public class OlEditorRenderer extends EditorRenderer{

    @Override
    public void resize(int width, int height){
        byte[] savedDarkness = new byte[width * height];
        for(int x = 0; x < width; x++){
            for(int y = 0; y < height; y++){
                Tile tile = world.tile(x, y);
                if(tile != null && tile.block() instanceof StaticWall){
                    savedDarkness[x + y * width] = tile.data;
                }
            }
        }

        super.resize(width, height);

        for(int x = 0; x < width; x++){
            for(int y = 0; y < height; y++){
                Tile tile = world.tile(x, y);
                if(tile != null && tile.block() instanceof StaticWall){
                    tile.data = savedDarkness[x + y * width];
                }
            }
        }
    }

    @Override
    public void draw(float tx, float ty, float tw, float th){
        IntSet recaches = Reflect.get(EditorRenderer.class, this, "recacheChunks");
        boolean doUpdate = Reflect.<Integer>get(EditorRenderer.class, this, "width") != world.width()
        || Reflect.<Integer>get(EditorRenderer.class, this, "height") != world.height()
        || recaches.size > 0;

        if(doUpdate && OlRenderer.darknessChunk != null){
            OlRenderer.darknessChunk.updated = false;
        }

        super.draw(tx, ty, tw, th);

        if(OlEditorExtension.showDarkness && OlRenderer.darknessChunk != null){
            if(!OlRenderer.darknessChunk.updated){
                boolean scissor = Gl.isEnabled(Gl.scissorTest);
                if(scissor) Gl.disable(Gl.scissorTest);

                OlRenderer.darknessChunk.updatePaintedDarkness();
                OlRenderer.darknessChunk.updated = true;

                if(scissor) Gl.enable(Gl.scissorTest);
            }

            FrameBuffer dark = Reflect.get(BlockRenderer.class, renderer.blocks, "dark");

            Core.camera.position.set(world.width() / 2f * tilesize, world.height() / 2f * tilesize);
            Core.camera.width = 999999f;
            Core.camera.height = 999999f;
            Core.camera.mat.set(Draw.proj()).mul(Tmp.m3.setToTranslation(tx, ty).scale(tw / (world.width() * tilesize), th / (world.height() * tilesize)).translate(4f, 4f));

            Mat prevProj = Tmp.m2.set(Draw.proj());
            Draw.proj(Core.camera.mat);

            Draw.shader(Shaders.darkness);
            Draw.rect(Draw.wrap(dark.getTexture()),
            world.width() * tilesize / 2f - tilesize / 2f,
            world.height() * tilesize / 2f - tilesize / 2f,
            world.width() * tilesize,
            -world.height() * tilesize);
            Draw.shader();

            Draw.proj(prevProj);
        }
    }
}
