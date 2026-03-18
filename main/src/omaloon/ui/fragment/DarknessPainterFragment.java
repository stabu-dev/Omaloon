package omaloon.ui.fragment;

import arc.*;
import arc.graphics.*;
import arc.math.*;
import arc.scene.*;
import arc.scene.actions.*;
import arc.scene.event.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.noise.*;
import mindustry.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.*;
import mindustry.world.*;
import omaloon.core.*;
import omaloon.graphics.*;

public class DarknessPainterFragment{
    public static boolean debug = false;

    private Table table;
    private Table modeTable = new Table();

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

                ui.add("@fragment.omaloon-darkness-painter").row();

                Image bgSlider = new Image(Tex.whiteui);
                bgSlider.color.set(Color.clear);
                Slider slider = new Slider(0f, 1f, 0.25f, false);
                slider.moved(value -> {
                    currentValue = (byte) Mathf.round(value * 255);
                    bgSlider.color.a(value);
                });
                ui.stack(bgSlider, slider).growX().row();

                initButtons();
                ui.table(modesTable -> {
                    modesTable.left();
                    modesTable.defaults().pad(5).size(40f);

                    ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle() {{
                        up = Styles.black6;
                        down = checked = ((TextureRegionDrawable) Tex.whiteui).tint(Color.grays(0.5f).a(0.6f));
                    }};

                    int i = 0;
                    for (PaintMode mode : modes) {
                        if (i % 3 == 0) modesTable.row();
                        int finalI = i;
                        var buttonCell = modesTable.button(mode.icon, style, 30f, () -> {
                            modeTable.clear();
                            if (currentMode != finalI) {
                                currentMode = finalI;
                                modeTable.table(table -> {
                                    table.add(Core.bundle.get(mode.name)).padBottom(10f).row();
                                    mode.build(table);
                                }).padTop(10f).growX();
                            } else {
                                currentMode = -1;
                            }
                        }).checked(button -> currentMode == modes.indexOf(mode));
                        if (Core.bundle.has(mode.name + ".tooltip")) buttonCell.tooltip(Core.bundle.get(mode.name + ".tooltip"));
                        i++;
                    }
                    if (i % 3 == 0) modesTable.row();
                    modesTable.button(Icon.refresh, style, 30, () -> Vars.ui.showConfirm("@fragment.omaloon-darkness-painter.clear-darkness.confirm", () -> {
                        OlRenderer.darknessChunk.clearDarknessMap();
                        OlRenderer.darknessChunk.updated = false;
                    })).tooltip("@fragment.omaloon-darkness-painter.clear-darkness").checked(button -> false);
                }).growX().padBottom(0).row();

                modeTable = ui.table().growX().padBottom(0).get();
                ui.row();

                ui.add().row();
            });

            t.update(() -> {
                if (Vars.state.isMenu() || (!Vars.state.rules.editor && !debug)) {
                    table.actions(Actions.fadeOut(0f));
                    shown = false;
                    return;
                }

                if (Core.input.keyTap(OlBinding.openDarknessPainter) && (Vars.state.rules.editor || debug)) toggle();

                if (Core.input.keyDown(OlBinding.paintDarkness) && shown) {
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
        modes.add(new PaintMode.NoiseCircleMode());
    }

    public Seq<PaintMode> getModes() {
        return modes;
    }

    public void toggle(){
        shown = !shown;

        table.clearActions();
        if (shown){
            table.actions(Actions.fadeIn(0.25f, Interp.pow2In));
        } else{
            table.actions(Actions.fadeOut(0.25f, Interp.pow2Out));
        }
    }

    public static abstract class PaintMode{
        public String name;
        public TextureRegionDrawable icon;

        public PaintMode(String name, TextureRegionDrawable icon){
            this.name = name;
            this.icon = icon;
        }

        public abstract void build(Table table);

        public abstract void draw(int x, int y, byte value);

        public abstract void use(int x, int y, byte value);

        public static class FlatMode extends PaintMode{
            public float currentRadius = 1;

            public FlatMode() {
                super("ui.omaloon-flat-mode", Icon.pencil);
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

        public static class NoiseCircleMode extends PaintMode{
            public float currentRadius = 1f;

            public static double scale = 0.002, persistence = 0.9, octaves = 5;
            public static float magnitude = 40f;

            public NoiseCircleMode() {
                super("ui.omaloon-noise-circle-mode", Icon.commandRally);
            }

            @Override
            public void build(Table table) {
                table.slider(1f, 20f, 1f, value -> currentRadius = value).growX();
            }

            @Override
            public void draw(int x, int y, byte value) {
                Drawf.dashCircle(x * 8f, y * 8f, currentRadius * 8f, Pal.accent);
            }

            @Override
            public void use(int x, int y, byte value) {
                for (int i = (int) -currentRadius; i < currentRadius; i++) {
                    for (int j = (int) -currentRadius; j < currentRadius; j++) {
                        Tile here = Vars.world.tile(x + i, y + j);
                        if (here != null && here.dst(x * 8, y * 8) < currentRadius * 8 - Simplex.noise2d(value, octaves, persistence, scale, x + i, y + j) * magnitude) {
                            OlRenderer.darknessChunk.putDarkness(here.x, here.y, value);
                        }
                    }
                }
            }
        }
    }
}
