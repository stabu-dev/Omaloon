package omaloon.ui.fragments;

import arc.*;
import arc.graphics.*;
import arc.input.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.*;
import arc.scene.actions.*;
import arc.scene.event.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.Vars;
import mindustry.core.World;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.*;
import mindustry.world.*;
import omaloon.OmaloonMod;
import omaloon.world.blocks.environment.*;

import static arc.Core.*;
import static mindustry.Vars.*;

public class TreePlacerFragment {
    private static Table indicator;
    private static boolean selecting = false;
    private static final Color col2 = Color.valueOf("75edff");
    private static Block currentTree;
    private static int currentShape = 0;

    public static void build(Group parent) {
        Table table = getTable();

        indicator.actions(Actions.alpha(0));

        parent.addChildAt(0, table);

        Events.on(WorldLoadEvent.class, e -> {
            selecting = false;
            hideUI();
        });

        if (!mobile) {
            scene.addListener(new InputListener() {
                @Override
                public boolean keyDown(InputEvent event, KeyCode keycode) {
                    if (keycode == KeyCode.o && isEditorActive()) {
                        toggle();
                        return true;
                    }
                    return false;
                }
            });
        }
    }

    private static Table getTable() {
        Table table = new Table(t -> {
            t.setFillParent(true);
            t.visible(TreePlacerFragment::isEditorActive);
            t.touchable(() -> selecting && isEditorActive() ? Touchable.childrenOnly : Touchable.disabled);

            t.bottom();
            t.table(Styles.black5, t1 -> {
                indicator = t1;
                t1.margin(10f);
                t1.table(t2 -> {
                    t2.image(Icon.treeSmall).size(15f).center().padRight(15f).color(col2);
                    t2.label(() -> "Tree Placer").grow().center().get().setAlignment(Align.center);
                    t2.image(Icon.treeSmall).size(15f).center().padLeft(15f).color(col2);
                }).growX();
                t1.row();

                // Tree selection
                t1.pane(Styles.smallPane, selector -> content.blocks().each(block -> block instanceof CustomShapeProp, block -> {
                    Button b = selector.button(
                            button -> button.add(new Image(block.uiIcon).setScaling(Scaling.fit)).size(32),
                            new Button.ButtonStyle() {{
                                up = Tex.windowEmpty;
                                down = Tex.windowEmpty;
                                checked = Tex.buttonSelect;
                            }},
                            () -> {
                                currentTree = block;
                                currentShape = 0;
                            }
                    ).size(40f).get();
                    b.update(() -> b.setChecked(currentTree == block));
                })).size(300f, 50f).padTop(5f);
                t1.row();

                // Tree name and shape control
                t1.table(t3 -> {
                    t3.label(() -> currentTree == null ? "Select a tree" : currentTree.localizedName).pad(5f);
                    t3.row();
                    t3.table(t4 -> {
                        t4.button("<", () -> {
                            if (currentTree instanceof CustomShapeProp) {
                                currentShape = (currentShape - 1 + ((CustomShapeProp) currentTree).shapes.size) % ((CustomShapeProp) currentTree).shapes.size;
                            }
                        }).size(30f);
                        t4.label(() -> "Shape: " + (currentShape + 1)).pad(5f);
                        t4.button(">", () -> {
                            if (currentTree instanceof CustomShapeProp) {
                                currentShape = (currentShape + 1) % ((CustomShapeProp) currentTree).shapes.size;
                            }
                        }).size(30f);
                    });
                }).padTop(5f);
                t1.row();

                // Add place tree button
                t1.button("Place Tree", () -> {
                    if (selecting && isEditorActive()) {
                        placeTree();
                    }
                }).size(120f, 40f).pad(5f);
                t1.row();

                t1.label(() -> "< Exit >").color(Pal.lightishGray).padTop(5f);

                t1.setTransform(true);
            }).fill().bottom();
        });
        table.setFillParent(true);
        table.pack();
        return table;
    }

    private static void placeTree() {
        if (!(currentTree instanceof CustomShapeProp tree)) return;

        Vec2 world = Core.input.mouseWorld();
        int tileX = World.toTile(world.x);
        int tileY = World.toTile(world.y);

        for (int i = 0; i < tree.shapes.get(currentShape).blocks.initialWordsAmount; i++) {
            if ((tree.shapes.get(currentShape).blocks.get(i) & 2) == 2) {
                int dx = tree.shapes.get(currentShape).unpackX(i);
                int dy = tree.shapes.get(currentShape).unpackY(i);
                Tile tile = Vars.world.tile(tileX + dx, tileY + dy);
                if (tile != null) {
                    Call.setTile(tile, currentTree, tile.team(), 0);
                }
            }
        }

        CustomShapePropProcess.instance.init();
    }

    public static void toggle() {
        if (!isEditorActive()) return;
        selecting = !selecting;
        if (selecting) {
            showUI();
        } else {
            hideUI();
        }
    }

    private static void showUI() {
        indicator.actions(
                Actions.moveBy(0, -80f),
                Actions.alpha(1),
                Actions.moveBy(0, 80f, 0.3f, Interp.pow3Out)
        );
    }

    private static void hideUI() {
        indicator.actions(
                Actions.moveBy(0, -80f, 0.3f, Interp.pow3In),
                Actions.alpha(0),
                Actions.moveBy(0, 80f)
        );
    }

    private static boolean isEditorActive() {
        return ui.hudfrag.shown && OmaloonMod.editorListener.isEditor();
    }
}