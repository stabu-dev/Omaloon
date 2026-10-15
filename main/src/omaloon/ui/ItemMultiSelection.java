package omaloon.ui;

import arc.*;
import arc.func.*;
import arc.math.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import mindustry.ctype.*;
import mindustry.gen.*;
import mindustry.ui.*;
import mindustry.world.*;

import static mindustry.Vars.*;

/** Mirrors vanilla {@link mindustry.world.blocks.ItemSelection}, but allows toggling any number of items. */
public class ItemMultiSelection{
    private static TextField search;
    private static int rowCount;

    public static <T extends UnlockableContent> void buildTable(Block block, Table table, Seq<T> items, Boolf<T> checked, Cons<T> toggle){
        buildTable(block, table, items, checked, toggle, 5, 4);
    }

    public static <T extends UnlockableContent> void buildTable(Block block, Table table, Seq<T> items, Boolf<T> checked, Cons<T> toggle, int rows, int columns){
        Table cont = new Table().top();
        cont.defaults().size(40);

        if(search != null) search.clearText();

        Runnable rebuild = () -> {
            cont.clearChildren();

            var text = search != null ? search.getText() : "";
            int i = 0;
            rowCount = 0;

            Seq<T> list = items.select(u -> (text.isEmpty() || u.localizedName.toLowerCase().contains(text.toLowerCase())));
            for(T item : list){
                if(!item.unlockedNow() || !item.isOnPlanet(state.getPlanet()) || item.isHidden()) continue;

                ImageButton button = cont.button(Tex.whiteui, Styles.clearNoneTogglei, Mathf.clamp(item.selectionSize, 0f, 40f), () -> {}).tooltip(item.localizedName).get();
                button.changed(() -> toggle.get(item));
                button.getStyle().imageUp = new TextureRegionDrawable(item.uiIcon);
                button.update(() -> button.setChecked(checked.get(item)));

                if(i++ % columns == (columns - 1)){
                    cont.row();
                    rowCount++;
                }
            }
        };

        rebuild.run();

        Table main = new Table().background(Styles.black6);
        if(rowCount > rows * 1.5f){
            main.table(s -> {
                s.image(Icon.zoom).padLeft(4f);
                search = s.field(null, text -> rebuild.run()).padBottom(4).left().growX().get();
                search.setMessageText("@players.search");
            }).fillX().row();
        }

        ScrollPane pane = new ScrollPane(cont, Styles.smallPane);
        pane.setScrollingDisabled(true, false);
        pane.exited(() -> {
            if(pane.hasScroll()){
                Core.scene.setScrollFocus(null);
            }
        });

        if(block != null){
            pane.setScrollYForce(block.selectScroll);
            pane.update(() -> block.selectScroll = pane.getScrollY());
        }

        pane.setOverscroll(false, false);
        main.add(pane).maxHeight(40 * rows);
        table.top().add(main);
    }
}
