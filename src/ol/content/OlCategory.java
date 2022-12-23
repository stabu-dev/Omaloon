package ol.content;

import arc.Core;
import arc.graphics.Texture;
import arc.graphics.g2d.TextureRegion;

import static mindustry.Vars.*;

import arc.scene.ui.layout.Table;
import mindustry.content.Blocks;

import mindustry.ctype.UnlockableContent;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.world.Block;
import mindustry.world.meta.BuildVisibility;
import ol.Omaloon;
import ol.content.blocks.OlDistribution;
import ol.utils.OlBundle;

import java.util.ArrayList;

public class OlCategory {
    public static ArrayList<OlCategory> all = new ArrayList<>();

    public TextureRegion icon = Core.atlas.find("ol-omaloon");
    public String name;
    public int id;

    public OlCategory(String name) {
        this.id = all.size();
        all.add(this);

        this.name = name;
    }

    public OlCategory(String name, TextureRegion icon) {
        this(name);

        this.icon = icon;
    }

    public OlCategory(String name, Texture texture) {
        this(name, new TextureRegion(texture));
    }

    public static OlCategory getCategoryByName(String name) {
        for(OlCategory category : all) {
            if(category.name.equals(name)) {
                return category;
            }
        }

        return null;
    }

    public static OlCategory current;
    public static OlCategory vanilla, omaloon;

    public static void loadCategories() {
        vanilla = new OlCategory("vanilla", Blocks.coalCentrifuge.uiIcon);
        ArrayList<String> strings = new ArrayList<>();

        mods.eachEnabled(mod -> {
            if(mod == null || mod.meta.hidden) {
                return;
            }

            new OlCategory(mod.name, mod.iconTexture);
            strings.add(mod.name);
        });

        content.each(c -> {
            if(c instanceof Block content) {
                BuildVisibility old = content.buildVisibility;
                String[] splited = content.name.split("-");

                for(String name : strings) {
                    if(splited[0].equals(name)) {
                        content.buildVisibility = new BuildVisibility(() -> {
                            return old.visible() && (current == null || current == getCategoryByName(name));
                        });

                        return;
                    }
                }

                content.buildVisibility = new BuildVisibility(() -> {
                    return old.visible() && (current == null || current == vanilla);
                });
            }
        });

        omaloon = getCategoryByName(Omaloon.MOD_PREFIX);
    }

    public static void loadUI() {
        Table table = new Table().bottom().left();
        table.name = "categories";

        for(OlCategory category : all) {
            table.button(btn -> {
                btn.name = category.name;
                btn.image(category.icon);

            }, () -> current = category)
                    .pad(3f).size(50f).row();
        }

        table.button(btn -> {
            btn.name = "all categories";
            btn.image(Icon.cancel);

        }, () -> current = null)
                .pad(3f).size(50f);

        table.setSize(62f, 300f);

        table.visible(() -> {
            return Core.settings.getBool(OlBundle._mod_ol_id_220358("content-sort"));
        });

        ui.hudGroup.addChild(table);
    }
}