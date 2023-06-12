package ol.multiitem;

import arc.Core;
import arc.func.Boolf;
import arc.func.Cons;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.ImageButton;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.TextField;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import mindustry.Vars;
import mindustry.ctype.UnlockableContent;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.type.Item;
import mindustry.ui.Styles;

public class MultiItemSelection {
    private static TextField search;

    public static void buildTable(Table table, MultiItemData data) {
        buildTable(table, Vars.content.items(), data);
    }

    public static void buildTable(Table table, Seq<Item> items, MultiItemData data) {
        buildTable(table, items, data::isToggled, data::enable, data::disable, data::toggle);
    }

    public static <T extends UnlockableContent> void buildTable(Table table,
                                                                Seq<T> items,
                                                                Boolf<T> holder,
                                                                Cons<T> enabled,
                                                                Cons<T> disabler,
                                                                Cons<T> toggler)
    {
        Table list22 = new Table().top();
        Table cont = new Table().top();
        cont.defaults().size(40);

        if(search != null) {
            search.clearText();
        }

        Runnable rebuild = () -> {
            cont.clearChildren();
            list22.clearChildren();
            String text = search != null ? search.getText() : "";

            Seq<T> list = items.select(u -> {
                return text.isEmpty() || u.localizedName.toLowerCase().contains(text.toLowerCase());
            });

            if(!list.isEmpty()) {
                final int[] i = new int[] {0};
                Runnable rower = () -> {
                    if(i[0]++ % 4 == 3) {
                        cont.row();
                    }
                };

                ImageButton button4 = cont.button(Tex.whiteui, Styles.clearNoneTogglei, 40f, () -> {
                    list.forEach(toggler::get);
                }).tooltip(Core.bundle.get("ol.reverse")).get();
                button4.getStyle().imageUp = Icon.refresh;
                rower.run();

                ImageButton button2 = cont.button(Tex.whiteui, Styles.clearNoneTogglei, 40f, () -> {
                    list.forEach(disabler::get);
                }).tooltip(Core.bundle.get("ol.disableAll")).get();
                button2.getStyle().imageUp = Icon.cancel;
                rower.run();

                ImageButton button3 = cont.button(Tex.whiteui, Styles.clearNoneTogglei, 40f, () -> {
                    list.forEach(enabled::get);
                }).tooltip(Core.bundle.get("ol.enableAll")).get();
                button3.getStyle().imageUp = Icon.add;
                rower.run();

                for(var btn : new ImageButton[] {button4, button2, button3}) {
                    btn.changed(() -> {
                        btn.setChecked(false);
                    });
                }

                for(T item : list) {
                    ImageButton button = cont.button(Tex.whiteui, Styles.clearNoneTogglei, Mathf.clamp(item.selectionSize, 0f, 40f),
                            () -> {}).tooltip(item.localizedName).get();

                    button.changed(() -> {
                        toggler.get(item);
                    });

                    button.getStyle().imageUp = new TextureRegionDrawable(item.uiIcon);
                    button.update(() -> {
                        button.setChecked(holder.get(item));
                    });

                    rower.run();
                }
            }

            ScrollPane pane = new ScrollPane(cont, Styles.smallPane);
            pane.setScrollingDisabled(true, false);
            pane.setOverscroll(false, false);
            list22.add(pane).maxHeight(200);
            if(list.isEmpty()) {
                list22.add("@empty").color(Color.gray);
            }
        };

        rebuild.run();
        Table main = new Table().background(Styles.black6);
        search = main.field(null, text -> {
            rebuild.run();
        }).width(170).padBottom(4).left().growX().get();
        search.setMessageText("@players.search");
        main.row();
        main.add(list22).width(170).minHeight(30).maxHeight(200);
        table.top().add(main);
    }
}
