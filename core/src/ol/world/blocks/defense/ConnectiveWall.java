package ol.world.blocks.defense;

import arc.*;
import arc.func.Boolf;
import arc.graphics.g2d.*;

import arc.math.*;
import arc.util.*;
import mindustry.*;
import mindustry.entities.*;
import mindustry.gen.*;
import mindustry.world.*;

import ol.world.meta.*;

import static mindustry.Vars.*;

public class ConnectiveWall extends OlWall {
    public float damageScl;
    public int damageRad;
    public TextureRegion[] joins;

    public ConnectiveWall(String name) {
        super(name);
    }

    @Override
    public void load() {
        super.load();
        joins = getRegions(Core.atlas.find(name + "-joins"), 12, 4, 32);
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.addPercent(OlStat.damageSpread, damageScl);
    }

    @Override
    public void drawBase(Tile tile) {
        Tile[][] grid = new Tile[3][3];
        int avail = 0;

        for(int i = 0; i < 3; i++)
            for(int j = 0; j < 3; j++) {
                grid[i][j] = world.tile(i + tile.x - 1, j + tile.y - 1);
                if(grid[i][j] != null) avail++;
            }

        int index = getTilingIndex(grid, 1, 1, t -> t != null && t.block() == ConnectiveWall.this);
        Draw.rect(avail == 0 ? region : joins[index], tile.worldx(), tile.worldy());
    }

    public TextureRegion[] icons() {
        return new TextureRegion[]{Core.atlas.find(name)};
    }

    public class ConnectiveWallBuild extends OlWallBuild {
        public boolean justDamaged;
        public float damageDelay = 10f;

        @Override
        public void updateTile() {
            super.updateTile();
            if(justDamaged && (damageDelay -= Time.delta) <= 0) {
                justDamaged = false;
                damageDelay = 10f;
            }
        }

        @Override
        public void damage(float amount) {
            damage(1, amount, null);
        }

        public void damage(int length, float amount, Building source) {
            justDamaged = true;
            damageDelay = 10f;

            if (this.dead()) return;

            float dm = Vars.state.rules.blockHealth(team);
            lastDamageTime = Time.time;
            if (Mathf.zero(dm)) amount = health + 1.0F;
            else amount = Damage.applyArmor(amount, block.armor) / dm;

            if (!Vars.net.client()) {
                health -= handleDamage(amount);
                healthChanged();
                if (health <= 0.0F) Call.buildDestroyed(this);
            }

            if (length >= damageRad) return;

            for (Building b : proximity) {
                if (b instanceof ConnectiveWallBuild w && b != source && !w.justDamaged && b != this && block == w.block)
                    w.damage(length + 1, amount * damageScl, this);
            }
        }
    }

    public static TextureRegion[] getRegions(TextureRegion region, int w, int h, int tilesize) {
        int size = w * h;
        TextureRegion[] regions = new TextureRegion[size];

        float tileW = (region.u2 - region.u) / w;
        float tileH = (region.v2 - region.v) / h;

        for(int i = 0; i < size; i++) {
            float tileX = ((float)(i % w)) / w;
            float tileY = ((float)(i / w)) / h;
            TextureRegion reg = new TextureRegion(region);

            //start coordinate
            reg.u = Mathf.map(tileX, 0f, 1f, reg.u, reg.u2) + tileW * 0.01f;
            reg.v = Mathf.map(tileY, 0f, 1f, reg.v, reg.v2) + tileH * 0.01f;
            //end coordinate
            reg.u2 = reg.u + tileW * 0.98f;
            reg.v2 = reg.v + tileH * 0.98f;

            reg.width = reg.height = tilesize;

            regions[i] = reg;
        }
        return regions;
    }

    static int[][] joinschkdirs = {
            {-1, 1},{0, 1},{1, 1},
            {-1, 0},/*{X}*/{1, 0},
            {-1,-1},{0,-1},{1,-1},
    };

    static int[] joinsMap = {
            39,39,27,27,39,39,27,27,38,38,17,26,38,38,17,26,36,
            36,16,16,36,36,24,24,37,37,41,21,37,37,43,25,39,
            39,27,27,39,39,27,27,38,38,17,26,38,38,17,26,36,
            36,16,16,36,36,24,24,37,37,41,21,37,37,43,25,3,
            3,15,15,3,3,15,15,5,5,29,31,5,5,29,31,4,
            4,40,40,4,4,20,20,28,28,10,11,28,28,23,32,3,
            3,15,15,3,3,15,15,2,2,9,14,2,2,9,14,4,
            4,40,40,4,4,20,20,30,30,47,44,30,30,22,6,39,
            39,27,27,39,39,27,27,38,38,17,26,38,38,17,26,36,
            36,16,16,36,36,24,24,37,37,41,21,37,37,43,25,39,
            39,27,27,39,39,27,27,38,38,17,26,38,38,17,26,36,
            36,16,16,36,36,24,24,37,37,41,21,37,37,43,25,3,
            3,15,15,3,3,15,15,5,5,29,31,5,5,29,31,0,
            0,42,42,0,0,12,12,8,8,35,34,8,8,33,7,3,
            3,15,15,3,3,15,15,2,2,9,14,2,2,9,14,0,
            0,42,42,0,0,12,12,1,1,45,18,1,1,19,13
    };

    public static <T> int getMaskIndex(T[][] map, int x,int y, Boolf<T> canConnect){
        int index = 0, ax, ay;
        T t;

        for(int i = 0; i < joinschkdirs.length; i++) {
            ax = joinschkdirs[i][0] + x;
            ay = joinschkdirs[i][1] + y;
            t = null;

            if(ax >= 0 && ay >= 0 && ax < map.length && ay < map[0].length){
                t = map[ax][ay];
            }

            index += canConnect.get(t) ? (1<<i) : 0;
        }

        return index;
    }

    public static <T> int getTilingIndex(T[][] map, int x, int y, Boolf<T> canConnect) {
        return joinsMap[getMaskIndex(map, x, y, canConnect)];
    }
}