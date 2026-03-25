package omaloon.world.blocks.environment;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.geom.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.*;
import mindustry.world.*;

public class OlCliff extends Block{
    public float colorMultiplier = 1.5f;
    public boolean useMapColor = true;
    public TextureRegion[] cliffs;

    public OlCliff(String name){
        super(name);
        breakable = alwaysReplace = false;
        solid = true;
        saveData = saveConfig = true;
        cacheLayer = CacheLayer.walls;
        fillsTile = hasShadow = false;
        editorConfigurable = true;
    }

    @Override
    public void init(){
        super.init();
        lastConfig = 0;
    }

    @Override
    public void load(){
        super.load();
        cliffs = new TextureRegion[12];
        for(int i = 0; i < 12; i++){
            cliffs[i] = Core.atlas.find(name + "-" + (i + 1), "omaloon-cliff-" + (i + 1));
        }
    }

    @Override
    public void buildEditorConfig(Table table){
        table.table(t -> {
            t.defaults().growX().height(42f).pad(2f);

            t.button("@block." + name + ".draw-upward", Icon.upOpen, Styles.flatTogglet, () -> lastConfig = 0)
             .update(b -> b.setChecked(lastConfig instanceof Integer i && i == 0))
             .margin(12f);

            t.button("@block." + name + ".draw-downward", Icon.downOpen, Styles.flatTogglet, () -> lastConfig = 1)
             .update(b -> b.setChecked(lastConfig instanceof Integer i && i == 1))
             .margin(12f);
        }).growX().padBottom(2f).row();

        table.button("@block." + name + ".cleanup", Icon.trash, Styles.cleart, OlCliff::cleanUp)
             .growX().height(42f).pad(2f).margin(12f).row();
    }

    @Override
    public Object getConfig(Tile tile){
        return tile.data >= 13 ? 0 : 1;
    }

    @Override
    public void editorPicked(Tile tile){
        lastConfig = tile.data >= 13 ? 0 : 1;
    }

    @Override
    public void placeEnded(Tile tile, @Nullable Unit builder, int rotation, @Nullable Object config){
        tile.data = (byte)((config instanceof Integer i && i == 0) ? 13 : 0);
    }

    @Override
    public void drawBase(Tile tile){
        int d = tile.data;
        boolean down = d >= 13;
        int face = (d == 0 || d == 13) ? face(tile, down) : (down ? d - 13 : d);

        if(face == 0){
            Draw.color();
            Draw.rect(region, tile.drawx(), tile.drawy());
        }else{
            if(useMapColor) Draw.color(Tmp.c1.set(tile.floor().mapColor).mul(colorMultiplier));
            Draw.rect(cliffs[face - 1], tile.drawx(), tile.drawy());
        }
        Draw.color();
    }

    @Override
    public int minimapColor(Tile tile){
        return Tmp.c1.set(tile.floor().mapColor).mul(1.2f).rgba();
    }

    /** @return face index (1-12) based on neighbours, or 0 if none. */
    public static int face(Tile tile, boolean downward){
        return downward ? downFace(tile) : upFace(tile);
    }

    //                            0  1  2  3  4  5   6   7  8   9  10  11  12  13  14  15
    static final int[] upFaces = {0, 1, 2, 9, 3, 3, 10, 10, 4, 12,  4, 12, 11, 12, 11, 12};
    static final int[] downFaces = {0, 0, 0, 5, 0, 0,  6,  2, 0,  8,  0,  1,  7,  4,  3,  0};

    static int upFace(Tile tile){
        int mask = 0;
        for(int i = 0; i < 4; i++) if(!cliff(tile.nearby(i))) mask |= (1 << i);
        if(mask != 0) return upFaces[mask];

        for(int i = 3; i >= 0; i--){
            if(!cliff(tile.nearby(Geometry.d8edge(i)))) return i + 5;
        }
        return 0;
    }

    static int downFace(Tile tile){
        int mask = 0;
        for(int i = 0; i < 4; i++) if(cliff(tile.nearby(i))) mask |= (1 << i);
        if(mask != 0b1111) return downFaces[mask];

        if(!cliff(tile.nearby(-1, -1))) return 9;
        if(!cliff(tile.nearby( 1, -1))) return 10;
        if(!cliff(tile.nearby( 1,  1))) return 11;
        if(!cliff(tile.nearby(-1,  1))) return 12;
        return 0;
    }

    static boolean cliff(Tile t){
        return t != null && t.block() instanceof OlCliff;
    }

    public static void cleanUp(){
        IntSeq remove = new IntSeq();
        Vars.world.tiles.eachTile(tile -> {
            if(!(tile.block() instanceof OlCliff)) return;
            int d = tile.data;
            boolean down = d >= 13;
            if(d != 0 && d != 13) return;

            Vars.editor.addTileOp(TileOp.get(tile.x, tile.y, (byte)5 /*opData*/, TileOpData.get(tile.data, tile.floorData, tile.overlayData)));

            int f = face(tile, down);
            if(f == 0){
                remove.add(tile.pos());
            }else{
                tile.data = (byte)(down ? f + 13 : f);
            }
        });

        for(int i = 0; i < remove.size; i++){
            Tile t = Vars.world.tile(remove.get(i));
            if(t != null) t.setBlock(Blocks.air);
        }

        Vars.editor.flushOp();
    }
}