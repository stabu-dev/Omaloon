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
import mindustry.content.*;
import mindustry.editor.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.ui.*;
import mindustry.world.*;
import omaloon.graphics.*;
import omaloon.world.patterns.*;

import java.util.*;

import static mindustry.Vars.*;

/** Extends the vanilla map editor with darkness painting and whole-shape pattern stamping. */
public class OlEditorExtension{
    private static final float[] darknessSliderValues = {0f, 1f, 2f, 3f, 4f, 5f};

    /** Darkness value to paint in vanilla darkness units. */
    public static byte darkValue = 0;
    /** Whether darkness rendering is enabled in the editor. */
    public static boolean showDarkness = true;
    /** Darkness alt-mode indices per tool, populated dynamically during init. */
    static int pencilDarkness = -1, lineDarkness = -1, eraserDarkness = -1;
    static int fillDarkness = -1, sprayDarkness = -1;
    static int lastX = -1, lastY = -1;
    static boolean drawing = false;
    static boolean wasEditorShown = false;

    static Point2 wholeShapeHover = new Point2(-1, -1);
    static Pattern wholeShapePattern = null;

    static Block lastDrawBlock = null;
    static boolean wasDarkness = false;
    static Vec2[][] prevBrushPolygons = null;

    public static boolean isDrawing(){
        return drawing;
    }

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
            boolean editorShown = state.isMenu() && dialog.isShown();
            updateBrushPreview();
            if(editorShown && !wasEditorShown && OlRenderer.darknessChunk != null){
                OlRenderer.darknessChunk.updated = false;
            }
            if(!editorShown){
                wasEditorShown = false;
                return;
            }
            wasEditorShown = true;

            injectUI(ui.editor);

            MapView view = ui.editor.getView();
            if(view.name == null || !view.name.contains("ol-dark")){
                addListener(view);
                view.name = (view.name == null ? "" : view.name) + "ol-dark";
            }

            updateShapeHover(view);

            boolean isDark = !isDarknessInactive();
            if(isDark && !wasDarkness){
                lastDrawBlock = editor.drawBlock;
                editor.drawBlock = Blocks.air;
            }else if(!isDark && wasDarkness){
                if(lastDrawBlock != null) editor.drawBlock = lastDrawBlock;
                lastDrawBlock = null;
            }else if(isDark){
                if(editor.drawBlock != Blocks.air && editor.drawBlock.isMultiblock()){
                    lastDrawBlock = editor.drawBlock;
                    editor.drawBlock = Blocks.air;
                }
            }
            wasDarkness = isDark;
        });
    }

    /** @return whether a darkness alt-mode is not currently selected. */
    public static boolean isDarknessInactive(){
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

        installDarknessBlockLabel(cont);

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

            Slider slider = new Slider(darknessSliderValues[0], darknessSliderValues[darknessSliderValues.length - 1], 1f, false);
            slider.setSnapToValues(darknessSliderValues, 0.1f);
            slider.setValue(Byte.toUnsignedInt(darkValue));
            slider.moved(v -> darkValue = (byte)Math.round(v));

            var label = new Label("@editor.omaloon-darkness");
            label.setAlignment(Align.center);
            label.touchable = Touchable.disabled;

            darkSettings.top().stack(slider, label).width(w * 3f - 20).padTop(4f);
            darkSettings.row();

            darkSettings.button("@editor.omaloon-darkness.clear", Icon.trash, Styles.flatt, () ->
            ui.showConfirm("@editor.omaloon-darkness.clear.confirm", () -> {
                editor.flushOp();
                DarknessOperation dop = new DarknessOperation();
                if(OlRenderer.darknessChunk.darkness != null){
                    for(int i = 0; i < OlRenderer.darknessChunk.darkness.length; i++){
                        byte old = OlRenderer.darknessChunk.darkness[i];
                        if(old != 0){
                            dop.diff.put(i, old);
                            dop.redoDiff.put(i, 0);
                        }
                    }
                }
                Reflect.set(MapEditor.class, editor, "currentOp", dop);
                editor.flushOp();

                OlRenderer.darknessChunk.clearDarknessMap();
                OlRenderer.darknessChunk.updated = false;
                ui.editor.resetSaved();
            })
            ).growX().height(36f).margin(6f).pad(2f).padTop(4f);

            Collapser col = new Collapser(darkSettings, true);
            col.setCollapsed(true, false);
            col.update(() -> col.setCollapsed(isDarknessInactive(), true));

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
        midTable.add(showToggle).pad(2f).left().row();

        if(hasCenter){
            midTable.add(last).growX().margin(9f);
        }
    }

    static void installDarknessBlockLabel(Group cont){
        if(cont.getChildren().size < 3) return;

        Element rightPanel = cont.getChildren().peek();
        if(!(rightPanel instanceof Table panel)) return;

        Label selectionLabel = panel.find("omaloon-darkness-block-label");
        if(selectionLabel != null) return;

        for(Element child : panel.getChildren()){
            if(!(child instanceof Table table) || table.getBackground() != Tex.underline) continue;

            Label label = table.find(e -> e instanceof Label);
            if(label == null) return;

            label.name = "omaloon-darkness-block-label";
            label.setText(OlEditorExtension::getBlockSelectionLabel);
            return;
        }
    }

    static String getBlockSelectionLabel(){
        if(!isDarknessInactive()){
            return Core.bundle.get("editor.omaloon-darkness") + " " + Byte.toUnsignedInt(darkValue);
        }

        return editor.drawBlock == null ? "" : editor.drawBlock.localizedName;
    }

    /** Inserts a capture InputListener on MapView to intercept darkness and pattern touches. */
    static void addListener(MapView view){
        view.addCaptureListener(new InputListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button){
                if(pointer != 0) return false;
                if(isWholeShapeActive()){
                    if(!mobile && button != KeyCode.mouseLeft) return false;

                    Reflect.set(MapView.class, view, "mousex", x);
                    Reflect.set(MapView.class, view, "mousey", y);

                    Pattern pattern = currentShapePattern();
                    if(pattern != null){
                        Point2 anchor = shapeAnchor(view, x, y, pattern);
                        paintShapeAt(anchor.x, anchor.y);
                        lastX = anchor.x;
                        lastY = anchor.y;
                        drawing = true;
                        ui.editor.resetSaved();
                    }

                    event.stop();
                    return true;
                }
                if(isDarknessInactive()) return false;
                if(!mobile && button != KeyCode.mouseLeft) return false;

                Reflect.set(MapView.class, view, "mousex", x);
                Reflect.set(MapView.class, view, "mousey", y);

                editor.flushOp();
                Reflect.set(MapEditor.class, editor, "currentOp", new DarknessOperation());

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

                if(isWholeShapeActive()){
                    Reflect.set(MapView.class, view, "mousex", x);
                    Reflect.set(MapView.class, view, "mousey", y);

                    Pattern pattern = currentShapePattern();
                    if(pattern == null) return;

                    Point2 anchor = shapeAnchor(view, x, y, pattern);
                    if(anchor.x == lastX && anchor.y == lastY) return;

                    Bresenham2.line(lastX, lastY, anchor.x, anchor.y, OlEditorExtension::paintShapeAt);
                    lastX = anchor.x;
                    lastY = anchor.y;
                    wholeShapeHover.set(anchor);
                    wholeShapePattern = pattern;
                    ui.editor.resetSaved();
                    return;
                }

                Reflect.set(MapView.class, view, "mousex", x);
                Reflect.set(MapView.class, view, "mousey", y);

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
                ui.editor.resetSaved();
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
                editor.flushOp();
                if(OlRenderer.darknessChunk != null){
                    OlRenderer.darknessChunk.updated = false;
                }
            }
        });
    }

    static void putDarknessOp(int x, int y, byte value){
        var chunk = OlRenderer.darknessChunk;
        if(chunk == null) return;
        if(chunk.darkness == null || chunk.isInvalidSize()) chunk.initDarknessMap();

        int index = x + y * world.width();
        if(index < 0 || index >= chunk.darkness.length) return;

        byte old = chunk.darkness[index];
        if(old == value) return;

        DrawOperation current = Reflect.get(MapEditor.class, editor, "currentOp");
        if(current instanceof DarknessOperation dop){
            if(!dop.diff.containsKey(index)){
                dop.diff.put(index, old);
            }
            dop.redoDiff.put(index, value);
        }

        chunk.putDarkness(x, y, value);
    }

    /** Paints darkness within the current brush circle. */
    static void applyBrush(int x, int y){
        editor.drawCircle(x, y, tile -> putDarknessOp(tile.x, tile.y, darkValue));
    }

    /** Erases darkness (sets to 0) within the current brush circle. */
    static void applyBrushErase(int x, int y){
        editor.drawCircle(x, y, tile -> putDarknessOp(tile.x, tile.y, (byte)0));
    }

    /** Sprays darkness randomly within the current brush circle. */
    static void applySpray(int x, int y){
        editor.drawCircle(x, y, tile -> {
            if(Mathf.chance(0.012)){
                putDarknessOp(tile.x, tile.y, darkValue);
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
                    putDarknessOp(x1, py, darkValue);

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
        var chunk = OlRenderer.darknessChunk;
        if(chunk == null || chunk.darkness == null || chunk.isInvalidSize()) return 0;
        int i = x + y * world.width();
        if(i < 0 || i >= chunk.darkness.length) return 0;
        return chunk.darkness[i];
    }

    public static boolean isWholeShapeActive(){
        if(!ui.editor.isShown() || !isDarknessInactive()) return false;
        if(ui.editor.getView().getTool() != EditorTool.pencil) return false;
        if(!(editor.drawBlock instanceof Patterned p) || !p.wholeShape()) return false;
        return currentShapePattern() != null;
    }

    static Pattern currentShapePattern(){
        if(!(editor.drawBlock instanceof Patterned p)) return null;
        Pattern top = p.getPattern();
        if(!(editor.drawBlock.lastConfig instanceof Integer idx) || idx < 0) return null;
        if(top instanceof MultiPattern mp){
            return idx < mp.patterns.size ? mp.get(idx) : null;
        }
        return top;
    }

    static void paintShapeAt(int x, int y){
        float old = editor.brushSize;
        editor.brushSize = 0f;
        editor.drawBlocks(x, y);
        editor.brushSize = old;
    }

    /** Centers the shape under the cursor, same idea as vanilla even-size multiblock snap. */
    static Point2 shapeAnchor(MapView view, float mx, float my, Pattern pattern){
        var s = pattern.shape;
        if(s == null) return Tmp.p1.set(-1, -1);

        float zoom = Reflect.get(MapView.class, view, "zoom");
        float ratio = 1f / ((float)editor.width() / editor.height());
        float size = Math.min(view.getWidth(), view.getHeight());
        float sclwidth = size * zoom, sclheight = size * zoom * ratio;
        float ox = (Float)Reflect.get(MapView.class, view, "offsetx") * zoom;
        float oy = (Float)Reflect.get(MapView.class, view, "offsety") * zoom;

        float cx = (mx - view.getWidth() / 2 + sclwidth / 2 - ox) / sclwidth * editor.width();
        float cy = (my - view.getHeight() / 2 + sclheight / 2 - oy) / sclheight * editor.height();
        float snapX = s.width() % 2 == 0 ? (int)(cx - 0.5f) + 0.5f : (int)cx;
        float snapY = s.height() % 2 == 0 ? (int)(cy - 0.5f) + 0.5f : (int)cy;

        return Tmp.p1.set(
        Math.round(snapX + s.anchorX - (s.width() - 1) / 2f),
        Math.round(snapY + s.anchorY - (s.height() - 1) / 2f)
        );
    }

    static void updateBrushPreview(){
        MapView view = ui.editor.getView();
        if(view == null) return;

        boolean active = isWholeShapeActive();
        if(active && prevBrushPolygons == null){
            prevBrushPolygons = Reflect.get(MapView.class, view, "brushPolygons");
            Reflect.set(MapView.class, view, "brushPolygons", new Vec2[MapEditor.brushSizes.length][0]);
        }else if(!active && prevBrushPolygons != null){
            Reflect.set(MapView.class, view, "brushPolygons", prevBrushPolygons);
            prevBrushPolygons = null;
        }
    }

    static void updateShapeHover(MapView view){
        Pattern pattern = isWholeShapeActive() ? currentShapePattern() : null;
        if(view == null || pattern == null || pattern.shape == null){
            wholeShapeHover.set(-1, -1);
            wholeShapePattern = null;
            return;
        }

        float mx = Reflect.get(MapView.class, view, "mousex");
        float my = Reflect.get(MapView.class, view, "mousey");
        wholeShapeHover.set(shapeAnchor(view, mx, my, pattern));
        wholeShapePattern = pattern;
    }

    public static class DarknessOperation extends DrawOperation{
        public IntIntMap diff = new IntIntMap();
        public IntIntMap redoDiff = new IntIntMap();

        @Override
        public void undo(){
            super.undo();
            applyDiff(diff);
        }

        @Override
        public void redo(){
            super.redo();
            applyDiff(redoDiff);
        }

        private void applyDiff(IntIntMap map){
            var chunk = OlRenderer.darknessChunk;
            if(chunk == null || chunk.darkness == null) return;
            for(var e : map.entries()){
                if(e.key >= 0 && e.key < chunk.darkness.length){
                    chunk.darkness[e.key] = (byte)e.value;
                }
            }
            chunk.updated = false;
        }

        @Override
        public boolean isEmpty(){
            return super.isEmpty() && diff.isEmpty();
        }
    }
}