package omaloon.editor;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.gl.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.editor.*;
import mindustry.graphics.*;
import mindustry.world.*;
import omaloon.graphics.*;

import static mindustry.Vars.*;

public class OlEditorRenderer extends EditorRenderer{

    @Override
    public void resize(int width, int height){
        super.resize(width, height);
        if(OlRenderer.darknessChunk != null){
            OlRenderer.darknessChunk.validateMapSize();
            OlRenderer.darknessChunk.updated = false;
        }
        rebuildEditorBlockDarkness();
    }

    @Override
    public void draw(float tx, float ty, float tw, float th){
        IntSet recaches = Reflect.get(EditorRenderer.class, this, "recacheChunks");
        Seq<Tile> shadowEvents = Reflect.get(BlockRenderer.class, renderer.blocks, "shadowEvents");
        IntSet darkEvents = Reflect.get(BlockRenderer.class, renderer.blocks, "darkEvents");
        var chunk = OlRenderer.darknessChunk;

        boolean doUpdate = Reflect.<Integer>get(EditorRenderer.class, this, "width") != world.width()
        || Reflect.<Integer>get(EditorRenderer.class, this, "height") != world.height()
        || recaches.size > 0
        || shadowEvents.size > 0
        || (darkEvents != null && darkEvents.size > 0)
        || (chunk != null && chunk.isInvalidSize());

        if(doUpdate){
            if(chunk != null) chunk.validateMapSize();
            if(darkEvents != null && darkEvents.size > 0) darkEvents.clear();
            rebuildEditorBlockDarkness();
            if(chunk != null) chunk.updated = false;
        }

        super.draw(tx, ty, tw, th);

        if(chunk != null){
            if(!chunk.updated){
                boolean scissor = Gl.isEnabled(Gl.scissorTest);
                if(scissor) Gl.disable(Gl.scissorTest);

                chunk.updatePaintedDarkness();
                chunk.updated = true;

                if(scissor) Gl.enable(Gl.scissorTest);
            }
        }

        if(OlEditorExtension.showDarkness && chunk != null){
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

    private void rebuildEditorBlockDarkness(){
        boolean prevEditor = state.rules.editor;
        if(state.isMenu()) state.rules.editor = true;
        try{
            byte[] dark = new byte[world.width() * world.height()];
            byte[] writeBuffer = new byte[dark.length];

            for(int i = 0; i < dark.length; i++){
                Tile tile = world.tiles.geti(i);
                if(OlRenderer.darkensTile(tile)){
                    dark[i] = (byte)darkRadius;
                }
            }

            for(int i = 0; i < darkRadius; i++){
                for(Tile tile : world.tiles){
                    int index = tile.array();
                    boolean min = false;

                    for(Point2 point : Geometry.d4){
                        int newX = tile.x + point.x, newY = tile.y + point.y;
                        int newIndex = newY * world.width() + newX;
                        if(world.tiles.in(newX, newY) && dark[newIndex] < dark[index]){
                            min = true;
                            break;
                        }
                    }

                    writeBuffer[index] = (byte)Math.max(0, dark[index] - Mathf.num(min));
                }

                System.arraycopy(writeBuffer, 0, dark, 0, writeBuffer.length);
            }

            for(Tile tile : world.tiles){
                int index = tile.array();
                boolean darkened = OlRenderer.darkensTile(tile);
                if(darkened){
                    tile.data = dark[index];

                    if(dark[index] == darkRadius){
                        boolean full = true;

                        for(Point2 point : Geometry.d4){
                            int px = point.x + tile.x, py = point.y + tile.y;
                            int newIndex = py * world.width() + px;
                            if(world.tiles.in(px, py) && !(OlRenderer.darkensTile(world.tiles.geti(newIndex)) && dark[newIndex] == darkRadius)){
                                full = false;
                                break;
                            }
                        }

                        if(full){
                            tile.data = (byte)(darkRadius + 1);
                        }
                    }
                }else{
                    tile.data = 0;
                }
            }
        }finally{
            state.rules.editor = prevEditor;
        }
    }
}