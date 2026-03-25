package omaloon.editor;

import arc.*;
import arc.input.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.*;
import arc.scene.event.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import mindustry.editor.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.ui.*;
import omaloon.graphics.*;

import java.util.*;

import static mindustry.Vars.*;

/** Extends the vanilla map editor with darkness painting alt-modes on existing tools. */
public class OlEditorExtension{
    /** Darkness value to paint (0-255). */
    public static byte darkValue = 0;
    /** Whether darkness rendering is enabled in the editor. */
    public static boolean showDarkness = true;
    /** Darkness alt-mode indices per tool, populated dynamically during init. */
    static int pencilDarkness = -1, lineDarkness = -1, eraserDarkness = -1;
    static int fillDarkness = -1, sprayDarkness = -1;
    static int lastX = -1, lastY = -1;
    static boolean drawing = false;

    public static void init(){
        pencilDarkness = appendAltMode(EditorTool.pencil, "omaloon-drawdarkness");
        lineDarkness = appendAltMode(EditorTool.line, "omaloon-linedarkness");
        eraserDarkness = appendAltMode(EditorTool.eraser, "omaloon-erasedarkness");
        fillDarkness = appendAltMode(EditorTool.fill, "omaloon-filldarkness");
        sprayDarkness = appendAltMode(EditorTool.spray, "omaloon-spraydarkness");

        Log.info("Omaloon: darkness editor modes injected (pencil=@, line=@, eraser=@, fill=@, spray=@)",
        pencilDarkness, lineDarkness, eraserDarkness, fillDarkness, sprayDarkness);

        editor.renderer = new OlEditorRenderer();

        Events.run(EventType.Trigger.update, () -> {
            MapEditorDialog dialog = ui.editor;
            if(!state.isMenu() || !dialog.isShown()) return;

            injectUI(ui.editor);

            MapView view = ui.editor.getView();
            if(view.name == null || !view.name.contains("ol-dark")){
                addListener(view);
                view.name = (view.name == null ? "" : view.name) + "ol-dark";
            }
        });
    }

    /** @return whether a darkness alt-mode is currently selected. */
    public static boolean isDarknessActive(){
        MapView view = ui.editor.getView();
        EditorTool tool = view.getTool();
        return !isDarkMode(tool);
    }

    static boolean isDarkMode(EditorTool tool){
        if(tool == EditorTool.pencil) return tool.mode == pencilDarkness;
        if(tool == EditorTool.line) return tool.mode == lineDarkness;
        if(tool == EditorTool.eraser) return tool.mode == eraserDarkness;
        if(tool == EditorTool.fill) return tool.mode == fillDarkness;
        if(tool == EditorTool.spray) return tool.mode == sprayDarkness;
        return false;
    }

    /** Appends an alt-mode to a tool's altModes array additively. */
    static int appendAltMode(EditorTool tool, String name){
        String[] cur = tool.altModes;
        int idx = cur.length;
        String[] ext = Arrays.copyOf(cur, cur.length + 1);
        ext[idx] = name;
        Reflect.set(EditorTool.class, tool, "altModes", ext);
        return idx;
    }

    /** Injects darkness controls into the editor's left panel. */
    static void injectUI(MapEditorDialog dialog){
        MapView view = dialog.getView();
        if(view.parent == null || view.parent.parent == null) return;

        Group cont = view.parent.parent;
        if(cont.getChildren().isEmpty()) return;

        Element leftPanel = cont.getChildren().get(0);
        if(!(leftPanel instanceof Table midTable)) return;

        if(midTable.find("omaloon-darkness-show") != null) return;

        float w = mobile ? 50f : 58f;

        Table brushTable = null;
        for(Element child : midTable.getChildren()){
            if(child instanceof Table t && t.getBackground() == Tex.underline){
                brushTable = t;
                break;
            }
        }

        if(brushTable != null){
            Table darkSettings = new Table();
            darkSettings.name = "omaloon-dark-settings";

            Slider slider = new Slider(0f, 1f, 0.01f, false);
            slider.setValue(darkValue / 255f);
            slider.moved(v -> darkValue = (byte)Math.round(v * 255));

            var label = new Label("@editor.omaloon-darkness");
            label.setAlignment(Align.center);
            label.touchable = Touchable.disabled;

            darkSettings.top().stack(slider, label).width(w * 3f - 20).padTop(4f);
            darkSettings.row();

            darkSettings.button("@editor.omaloon-darkness.clear", Icon.trash, Styles.flatt, () ->
            ui.showConfirm("@editor.omaloon-darkness.clear.confirm", () -> {
                OlRenderer.darknessChunk.clearDarknessMap();
                OlRenderer.darknessChunk.updated = false;
            })
            ).growX().height(36f).margin(6f);

            Collapser col = new Collapser(darkSettings, true);
            col.setCollapsed(true, false);
            col.update(() -> col.setCollapsed(isDarknessActive(), true));

            brushTable.row();
            brushTable.add(col).growX();
        }

        CheckBox showToggle = new CheckBox("@editor.omaloon-darkness.show");
        showToggle.name = "omaloon-darkness-show";
        showToggle.setChecked(showDarkness);
        showToggle.changed(() -> showDarkness = showToggle.isChecked());

        Element last = midTable.getChildren().peek();
        boolean hasCenter = !mobile && last instanceof Button;

        if(hasCenter){
            for(int i = 0; i < midTable.getCells().size; i++){
                if(midTable.getCells().get(i).get() == last){
                    midTable.getCells().remove(i);
                    break;
                }
            }
            midTable.removeChild(last);
        }

        midTable.row();
        midTable.add(showToggle).pad(2f).growX().left().row();

        if(hasCenter){
            midTable.add(last).growX().margin(9f);
        }
    }

    /** Inserts an InputListener at position 0 on MapView to intercept darkness touches. */
    static void addListener(MapView view){
        view.getListeners().insert(0, new InputListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button){
                if(pointer != 0) return false;
                if(isDarknessActive()) return false;
                if(!mobile && button != KeyCode.mouseLeft) return false;

                Point2 p = view.project(x, y);
                EditorTool tool = view.getTool();

                if(tool == EditorTool.pencil){
                    applyBrush(p.x, p.y);
                    lastX = p.x;
                    lastY = p.y;
                    drawing = true;
                }else if(tool == EditorTool.eraser){
                    applyBrushErase(p.x, p.y);
                    lastX = p.x;
                    lastY = p.y;
                    drawing = true;
                }else if(tool == EditorTool.spray){
                    applySpray(p.x, p.y);
                    lastX = p.x;
                    lastY = p.y;
                    drawing = true;
                }else if(tool == EditorTool.line){
                    lastX = p.x;
                    lastY = p.y;
                    drawing = true;
                }else if(tool == EditorTool.fill){
                    floodFill(p.x, p.y);
                }

                ui.editor.resetSaved();
                event.stop();
                return true;
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer){
                if(!drawing) return;

                Point2 p = view.project(x, y);
                EditorTool tool = view.getTool();

                if(tool == EditorTool.line){
                    // line tool only draws on release, nothing to do here
                    return;
                }

                if(p.x == lastX && p.y == lastY) return;

                if(tool == EditorTool.pencil){
                    Bresenham2.line(lastX, lastY, p.x, p.y, OlEditorExtension::applyBrush);
                }else if(tool == EditorTool.eraser){
                    Bresenham2.line(lastX, lastY, p.x, p.y, OlEditorExtension::applyBrushErase);
                }else if(tool == EditorTool.spray){
                    Bresenham2.line(lastX, lastY, p.x, p.y, OlEditorExtension::applySpray);
                }

                lastX = p.x;
                lastY = p.y;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, KeyCode button){
                if(!drawing) return;

                EditorTool tool = view.getTool();
                if(tool == EditorTool.line){
                    Point2 p = view.project(x, y);
                    int x2 = p.x, y2 = p.y;

                    if(EditorTool.line.mode == 1){
                        if(Math.abs(x2 - lastX) > Math.abs(y2 - lastY)){
                            y2 = lastY;
                        }else{
                            x2 = lastX;
                        }
                    }

                    int fx = x2, fy = y2;
                    Bresenham2.line(lastX, lastY, fx, fy, OlEditorExtension::applyBrush);
                    ui.editor.resetSaved();
                }

                drawing = false;
                lastX = -1;
                lastY = -1;
            }
        });
    }

    /** Paints darkness within the current brush circle. */
    static void applyBrush(int x, int y){
        editor.drawCircle(x, y, tile ->
        OlRenderer.darknessChunk.putDarkness(tile.x, tile.y, darkValue)
        );
    }

    /** Erases darkness (sets to 0) within the current brush circle. */
    static void applyBrushErase(int x, int y){
        editor.drawCircle(x, y, tile ->
        OlRenderer.darknessChunk.putDarkness(tile.x, tile.y, (byte)0)
        );
    }

    /** Sprays darkness randomly within the current brush circle. */
    static void applySpray(int x, int y){
        editor.drawCircle(x, y, tile -> {
            if(Mathf.chance(0.012)){
                OlRenderer.darknessChunk.putDarkness(tile.x, tile.y, darkValue);
            }
        });
    }

    /** Flood-fills tiles that share the same darkness value with the current value. */
    static void floodFill(int x, int y){
        int w = editor.width(), h = editor.height();
        if(!Structs.inBounds(x, y, w, h)) return;

        byte target = getDark(x, y);
        if(target == darkValue) return;

        IntSeq stack = new IntSeq();
        stack.add(Point2.pack(x, y));

        try{
            while(stack.size > 0 && stack.size < w * h){
                int pos = stack.pop();
                int py = Point2.y(pos);

                int x1 = Point2.x(pos);
                while(x1 >= 0 && getDark(x1, py) == target) x1--;
                x1++;

                boolean above = false, below = false;
                while(x1 < w && getDark(x1, py) == target){
                    OlRenderer.darknessChunk.putDarkness((short)x1, (short)py, darkValue);

                    if(!above && py > 0 && getDark(x1, py - 1) == target){
                        stack.add(Point2.pack(x1, py - 1));
                        above = true;
                    }else if(above && getDark(x1, py - 1) != target){
                        above = false;
                    }

                    if(!below && py < h - 1 && getDark(x1, py + 1) == target){
                        stack.add(Point2.pack(x1, py + 1));
                        below = true;
                    }else if(below && py < h - 1 && getDark(x1, py + 1) != target){
                        below = false;
                    }

                    x1++;
                }
            }
        }catch(OutOfMemoryError e){
            System.gc();
            Log.err("Darkness fill ran out of memory", e);
        }
    }

    static byte getDark(int x, int y){
        if(OlRenderer.darknessChunk.darkness == null) return 0;
        int i = x + y * world.width();
        if(i < 0 || i >= OlRenderer.darknessChunk.darkness.length) return 0;
        return OlRenderer.darknessChunk.darkness[i];
    }
}
