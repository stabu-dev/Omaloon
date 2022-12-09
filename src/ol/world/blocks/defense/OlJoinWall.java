package ol.world.blocks.defense;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.util.Log;
import arc.util.Time;
import mindustry.Vars;
import mindustry.entities.Damage;
import mindustry.gen.Building;
import mindustry.gen.Call;
import mindustry.world.Tile;
import mindustry.world.meta.Stat;
import ol.graphics.OlGraphics;

import static mindustry.Vars.world;

public class OlJoinWall extends OlWall {
    //when all block have the same hp
    public boolean healthLink = false;

    //when 2~3 blocks damaged
    public boolean damageLink = false;
    public float damageScl = 1f; //from 0 to 1
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
        
        //when damage link and damage falls
        if(damageLink && damageScl != 1) {
            stats.add(Stat.damageMultiplier, 100 * damageScl + "%");
        }
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

    public class OlJoinWallBuild extends OlWallBuild{
        boolean justDamaged = false;
        private float s = 10;

        @Override
        public void updateTile() {
            super.updateTile();

            if(damageLink && justDamaged) {
                s--;

                if(s < 0) {
                    justDamaged = false;
                    s = 10;
                }
            }

            if(healthLink) {
                int i = 1;
                float thp = health;

                for(Building b : proximity) {
                    if(b instanceof OlJoinWallBuild w) {
                        thp += w.health;
                        i++;
                    }
                }

                float hp = thp/i;
                for(Building b : proximity) {
                    if(b instanceof OlJoinWallBuild w) {
                        w.health = hp;
                    }
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