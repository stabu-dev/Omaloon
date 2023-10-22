package omaloon.ui;

import arc.func.*;
import arc.graphics.*;
import arc.math.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import mindustry.*;
import mindustry.ctype.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.ui.*;

import static mindustry.Vars.state;

public class MultiItemSelection {
    public static void buildTable(Table table, MultiItemData data) {
        buildTable(table, Vars.content.items(), data);
    }

    public static void buildTable(Table table, Seq<Item> items, MultiItemData data) {
        buildTable(table, items, data::isToggled, data::toggle);
    }

    public static <T extends UnlockableContent> void buildTable(Table table,
                                                                Seq<T> items,
                                                                Boolf<T> holder,
                                                                Cons<T> toggle)
    {
        Table list22 = new Table().top();
        Table cont = new Table().top();
        cont.defaults().size(40);

        Runnable rebuild = () -> {
            cont.clearChildren();
            list22.clearChildren();

            Seq<T> list = items.select(u -> {
                if (!u.unlockedNow()) return false;
                if (u instanceof Item checkVisible && state.rules.hiddenBuildItems.contains(checkVisible)) return false;
                return !u.isHidden();
            });

            if (!list.isEmpty()) {
                final int[] i = new int[] {0};
                Runnable rower = () -> {
                    if (i[0]++ % 4 == 3) {
                        cont.row();
                    }
                };

                for (T item : list) {
                    ImageButton button = cont.button(Tex.whiteui, Styles.clearNoneTogglei, Mathf.clamp(item.selectionSize, 0f, 40f),
                            () -> {}).tooltip(item.localizedName).get();

                    button.changed(() ->
                            toggle.get(item)
                    );

                    button.getStyle().imageUp = new TextureRegionDrawable(item.uiIcon);
                    button.update(() ->
                            button.setChecked(holder.get(item))
                    );

                    rower.run();
                }
            }

            ScrollPane pane = new ScrollPane(cont, Styles.smallPane);
            pane.setScrollingDisabled(true, false);
            pane.setOverscroll(false, false);
            list22.add(pane).maxHeight(200);
            if (list.isEmpty()) {
                list22.add("@empty").color(Color.gray);
            }
        };

        rebuild.run();
        Table main = new Table().background(Styles.black6);
        main.row();
        main.add(list22).width(170).minHeight(30).maxHeight(200);
        table.top().add(main);
    }
}
