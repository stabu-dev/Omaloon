package omaloon.world.blocks.storage;

import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.blocks.storage.*;

public class GlassmoreCoreBlock extends CoreBlock{
    public float spawnCooldown = 2f * 60f;

    public GlassmoreCoreBlock(String name){
        super(name);
    }

    public class DelayedSpawnCoreBuild extends CoreBuild{
        public float timer = 0f;
        public boolean requested = false;
        public float heat, progress, time;
        public Player spawnPlayer;
        public boolean animating = false;
        public boolean justSpawned = false;

        @Override
        public void draw(){
            super.draw();

            if((heat > 0.001f || animating) && !justSpawned){
                drawRespawn();
            }
        }

        @Override
        public void updateTile(){
            super.updateTile();
            if(timer > 0) timer -= Time.delta;

            if(spawnPlayer != null || animating){
                heat = Mathf.lerpDelta(heat, 1f, 0.1f);
                time += Time.delta;
                progress += 1f / spawnCooldown * Time.delta;

                if(progress >= 1f){
                    if(spawnPlayer != null && spawnPlayer.dead()){
                        playerSpawn(tile, spawnPlayer);
                        justSpawned = true;
                    }
                    animating = false;
                    spawnPlayer = null;
                    requested = false;
                }
            }else{
                heat = Mathf.lerpDelta(heat, 0f, 0.1f);
                if(justSpawned && heat <= 0.001f){
                    justSpawned = false;
                }
            }
        }

        @Override
        public void requestSpawn(Player player){
            if(Vars.state.isEditor()){
                spawnPlayer = player;
                playerSpawn(tile, spawnPlayer);
            }else if(!requested && player.dead() && !justSpawned){
                timer = spawnCooldown;
                requested = true;
                spawnPlayer = player;
                progress = 0f;
                time = 0f;
                heat = 0f;
                animating = true;
            }
        }

        void drawRespawn(){
            Draw.color(Pal.darkMetal);
            Lines.stroke(2f * heat);
            Fill.poly(x, y, 4, 10f * heat);

            Draw.reset();
            if(spawnPlayer != null){
                TextureRegion region = spawnPlayer.icon();

                Draw.color(0f, 0f, 0f, 0.4f * progress);
                Draw.rect("circle-shadow", x, y, region.width / 3f, region.height / 3f);
                Draw.color();

                Draw.draw(Draw.z(), () -> Drawf.construct(this, region, 0, progress, progress, timer));

                Draw.color(Pal.accentBack);
                float pos = Mathf.sin(time, 6f, 8f);
                Lines.lineAngleCenter(x + pos, y, 90, 16f - Math.abs(pos) * 2f);
                Draw.reset();
            }

            Lines.stroke(2f * heat);
            Draw.color(Pal.accentBack);
            Lines.poly(x, y, 4, 8f * heat);

            float oy = -7f, len = 6f * heat;
            Lines.stroke(5f);
            Draw.color(Pal.darkMetal);
            Lines.line(x - len, y + oy, x + len, y + oy);

            for(int i : Mathf.signs){
                Fill.tri(x + len * i, y + oy - Lines.getStroke() / 2f, x + len * i, y + oy + Lines.getStroke() / 2f, x + (len + Lines.getStroke() * heat) * i, y + oy);
            }

            Lines.stroke(3f);
            Draw.color(Pal.accent);
            Lines.line(x - len, y + oy, x - len + len * 2 * progress, y + oy);

            Draw.reset();
        }
    }
}