package omaloon.type;

import arc.*;
import arc.func.*;
import arc.graphics.g2d.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.bullet.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.meta.*;

/**
 * a weapon that shoots different things depending on things in a unit
 */
public class FilterWeapon extends Weapon{
    /**
     * separate from bulletFilter for stats
     */
    public BulletType[] bullets = new BulletType[]{Bullets.placeholder};
    public Func<Unit, BulletType> bulletFilter = unit -> bullets[0];
    // TODO i don't know how to make those icons work, and i have no clue as to why it is casting a string to BulletType
    public TextureRegion[] iconRegions;
    public String[] icons = new String[]{""};

    @Override
    public void addStats(UnitType u, Table t){
        if(inaccuracy > 0){
            t.row();
            t.add("[lightgray]" + Stat.inaccuracy.localized() + ": [white]" + (int)inaccuracy + " " + StatUnit.degrees.localized());
        }
        if(!alwaysContinuous && reload > 0){
            t.row();
            t.add("[lightgray]" + Stat.reload.localized() + ": " + (mirror ? "2x " : "") + "[white]" + Strings.autoFixed(60f / reload * shoot.shots, 2) + " " + StatUnit.perSecond.localized());
        }

        t.row();
        t.table(Styles.grayPanel, weapon -> {
            for(int i = 0; i < bullets.length; i++){
                int finalI = i;
                weapon.table(Tex.underline, b -> {
                    b.left();
                    if(iconRegions[finalI].found()) b.image(iconRegions[finalI]).padRight(10).center();
                    StatValues.ammo(ObjectMap.of(u, bullets[finalI])).display(b.add(new Table()).get());
                }).growX().row();
            }
        }).margin(10f);
    }

    @Override
    public void load(){
        super.load();
        iconRegions = new TextureRegion[bullets.length];
        for(int i = 0; i < iconRegions.length; i++){
            if(i < icons.length){
                iconRegions[i] = Core.atlas.find(icons[i]);
            }else{
                iconRegions[i] = Core.atlas.find("error");
            }
        }
    }

    @Override
    protected void shoot(Unit unit, WeaponMount mount, float shootX, float shootY, float rotation){
        bullet = bulletFilter.get(unit);
        super.shoot(unit, mount, shootX, shootY, rotation);
    }
}
