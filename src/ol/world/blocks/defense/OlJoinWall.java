package ol.world.blocks.defense;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.*;
import mindustry.entities.*;
import mindustry.gen.*;
import mindustry.world.*;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import mindustry.world.meta.StatValues;
import ol.graphics.*;
import ol.world.meta.*;

import static mindustry.Vars.*;

public class OlJoinWall extends OlWall {
    /**percent of damage will the nearby walls receive. 0-0%, 1-100%*/
    public float damageScl = 0.5f;
    /**(int) radius in which the walls receive damage*/
    public int damageRad = 3;

    TextureRegion[] joins;

    public OlJoinWall(String name) {
        super(name);
    }

    @Override
    public void load(){
        super.load();
        joins = OlGraphics.getRegions(Core.atlas.find(name+"-joins"), 12, 4,32);
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.addPercent(OlStat.damageSpread, 1 * damageScl);
    }

    @Override
    public void drawBase(Tile tile){
        Tile[][] grid = new Tile[3][3];

        int avail = 0;
        for(int i = 0; i<3; i++) {
            for(int j = 0; j<3; j++) {
                grid[i][j] = world.tile(i + tile.x - 1, j + tile.y - 1);

                if(grid[i][j] != null) {
                    avail++;
                }
            }
        }

        int index = OlGraphics.getTilingIndex(grid, 1, 1, t -> t != null && t.block() == OlJoinWall.this);
        Draw.rect(avail == 0 ? region : joins[index], tile.worldx(), tile.worldy());
    }

    public TextureRegion[] icons(){
        return new TextureRegion[]{Core.atlas.find(name, name)};
    }

    public class OlJoinWallBuild extends OlWallBuild {
        boolean justDamaged = false;
        private float s = 10;

        @Override
        public void updateTile() {
            super.updateTile();

            if(justDamaged) {
                s--;

                if(s < 0) {
                    justDamaged = false;
                    s = 10;
                }
            }
        }

        public void damage(int length, float amount, Building source) {
            justDamaged = true;
            s = 10;

            if(!this.dead()) {
                float dm = Vars.state.rules.blockHealth(this.team);
                this.lastDamageTime = Time.time;
                if (Mathf.zero(dm)) {
                    amount = this.health + 1.0F;
                } else {
                    amount = Damage.applyArmor(amount, this.block.armor) / dm;
                }

                if (!Vars.net.client()) {
                    this.health -= this.handleDamage(amount);
                }

                this.healthChanged();
                if (this.health <= 0.0F) {
                    Call.buildDestroyed(this);
                }
            }

            Log.info(length);
            if(length >= damageRad) {
                return;
            }

            for(Building b : proximity) {
                if(b instanceof OlJoinWallBuild w && b != source && !w.justDamaged && b != this) {
                    w.damage(length + 1, amount * damageScl, this);
                }
            }
        }

        @Override
        public void damage(float damage) {
            damage(1, damage, null);
        }
    }
}