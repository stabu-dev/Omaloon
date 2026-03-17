package omaloon.ui.fragment;

import arc.*;
import arc.graphics.*;
import arc.input.*;
import arc.math.*;
import arc.scene.*;
import arc.scene.actions.*;
import arc.scene.event.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import mindustry.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.*;
import mindustry.world.*;
import omaloon.graphics.*;

public class DarknessPainterFragment{
    public static boolean debug = false;

    private Table table;
    private Table modeTable;

    private final Seq<PaintMode> modes = new Seq<>();

    public boolean shown;
    public byte currentValue;
    public int currentMode = -1;

    public void build(Group parent) {
        parent.fill(t -> {
            table = t;

            t.touchable(() -> shown ? Touchable.childrenOnly : Touchable.disabled);

            t.left();
            t.table(Styles.black6, ui -> {
                ui.defaults().pad(10).padTop(0f);

                ui.add().row();

                Image bgSlider = new Image(Tex.whiteui);
                bgSlider.color.set(Color.clear);
                Slider slider = new Slider(0f, 1f, 0.25f, false);
                slider.moved(value -> {
                    currentValue = (byte) Mathf.round(value * 255);
                    bgSlider.color.a(value);
                });
                ui.stack(bgSlider, slider).row();

                initButtons();
                ui.table(modesTable -> {
                    modesTable.left();
                    modesTable.defaults().pad(5).size(40f);
                    int i = 0;
                    for (PaintMode mode : modes) {
                        if (i % 3 == 0) modesTable.row();
                        int finalI = i;
                        modesTable.button(mode.icon, () -> {
                            currentMode = finalI;
                            modeTable.clear();
                            mode.build(modeTable);
                        }).with(button -> {
                            button.update(() -> button.setChecked(currentMode == modes.indexOf(mode)));
                        });
                        i++;
                    }
                }).growX().row();

                modeTable = ui.table().growX().get();
            }).margin(10f);

            t.update(() -> {
                if (Vars.state.isMenu()) {
                    table.actions(Actions.fadeOut(0f));
                    shown = false;
                    return;
                }

                // TODO keybind
                if (Core.input.keyTap(KeyCode.p) && (Vars.state.rules.editor || debug)) toggle();

                if (Core.input.keyDown(KeyCode.mouseLeft) && shown) {
                    if (currentMode != -1) {
                        modes.get(currentMode).use((int) (Core.input.mouseWorldX() / 8), (int) (Core.input.mouseWorldY() / 8), currentValue);
                    }
                }
            });
            t.color.a(0);
        });

        Events.run(EventType.Trigger.draw, () -> {
            if (currentMode != -1 && shown) {
                modes.get(currentMode).draw((int) (Core.input.mouseWorldX() / 8), (int) (Core.input.mouseWorldY() / 8), currentValue);
            }
        });
    }

    private void initButtons() {
        modes.add(new PaintMode.FlatMode());
    }

    public Seq<PaintMode> getModes() {
        return modes;
    }

    public void toggle() {
        shown = !shown;

        table.clearActions();
        if (shown) {
            table.actions(Actions.fadeIn(0.5f, Interp.pow2In));
        } else {
            table.actions(Actions.fadeOut(0.5f, Interp.pow2Out));
        }
    }

    public static abstract class PaintMode {
        TextureRegionDrawable icon;
        PaintMode(TextureRegionDrawable icon) {
            this.icon = icon;
        }

        public abstract void build(Table table);

        public abstract void draw(int x, int y, byte value);

        public abstract void use(int x, int y, byte value);

        public static class FlatMode extends PaintMode {
            public float currentRadius;

            public FlatMode() {
                super(Icon.pencil);
            }

            @Override
            public void build(Table table) {
                table.slider(1f, 20f, 1f, value -> currentRadius = value).growX();
            }

            @Override
            public void draw(int x, int y, byte value) {
                Drawf.dashSquare(Pal.accent, x * 8f, y * 8f, currentRadius * 8f);
            }

            @Override
            public void use(int x, int y, byte value) {
                for (int i = (int) -currentRadius/2; i < currentRadius/2; i++) {
                    for (int j = (int) -currentRadius/2; j < currentRadius/2; j++) {
                        Tile here = Vars.world.tile(x + i, y + j);
                        if (here != null) {
                            OlRenderer.darknessChunk.putDarkness(here.x, here.y, value);
                        }
                    }
                }
            }
        }
    }
}
