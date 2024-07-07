package omaloon.ui.dialogs;

import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.Button.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.*;
import mindustry.ui.dialogs.*;
import mindustry.world.*;
import omaloon.*;
import omaloon.world.blocks.environment.*;

import static mindustry.Vars.*;

public class OlMultiBlockEditorDialog extends BaseDialog {
	Block current;
	Stack world = new Stack();
	int offsetx, offsety,
		shape,
		worldWidth = 10,
		worldHeight = 10;

	public OlMultiBlockEditorDialog(){
		super("@ui.omaloon-multi-block-editor");

		cont.clear();
		if(!mobile){
			cont.table(menu -> {
				menu.pane(Styles.smallPane, selector -> Vars.content.blocks().each(block -> block instanceof CustomShapeProp, block -> {
					Button b = selector.button(
						button -> button.add(new Image(block.uiIcon).setScaling(Scaling.fit)).size(32),
						new ButtonStyle() {{
							up = Tex.sideline;
							down = Tex.sidelineOver;
							over = Tex.sidelineOver;
							checked = Tex.buttonSideRight;
							checkedOver = Tex.buttonSideRightDown;
						}},
						() -> {
							current = block;
							updateValues();
							rebuildStack();
						}
					).minHeight(64).minWidth(64).tooltip(tooltip -> tooltip.add(block.localizedName)).get();
					b.update(() -> b.setChecked(current == block));
					selector.row();
				}));
				menu.table(((ScaledNinePatchDrawable) Tex.whitePane).tint(Pal.gray), preview -> preview.add(world)).size(384f);
			}).row();
			cont.table(meta -> {
				meta.add("x");
				TextField fieldX = meta.field(offsetx + "", s -> {
					offsetx = Strings.parseInt(s, 0);
					updateValues();
					rebuildStack();
				}).get();
				meta.row();
				fieldX.setValidator(s -> Strings.parseInt(s, 0) + worldWidth < Vars.world.width());
				fieldX.setFilter(TextField.TextFieldFilter.digitsOnly);
				meta.add("y");
				TextField fieldY = meta.field(offsetx + "", s -> {
					offsety = Strings.parseInt(s, 0);
					updateValues();
					rebuildStack();
				}).get();
				meta.row();
				fieldY.setValidator(s -> Strings.parseInt(s, 0) + worldHeight < Vars.world.height());
				fieldY.setFilter(TextField.TextFieldFilter.digitsOnly);
				meta.add("shape");
				TextField fieldS = meta.field(offsetx + "", s -> {
					shape = Strings.parseInt(s, 0);
					updateValues();
					rebuildStack();
				}).get();
				meta.row();
				fieldS.setValidator(s -> current != null && Strings.parseInt(s, 0) < ((CustomShapeProp) current).shapes.size);
				fieldS.setFilter(TextField.TextFieldFilter.digitsOnly);
				meta.left();
			});

			ui.hudGroup.fill(cont -> cont.bottom().button("@ui.omaloon-multi-block-editor", Icon.pencil, new TextButton.TextButtonStyle(){{
				font = Fonts.def;
				up = Tex.button;
				down = Tex.buttonDown;
				over = Tex.buttonOver;
			}}, this::show).width(320f).visible(() -> ui.hudfrag.shown && OmaloonMod.editorListener.isEditor()));
		}

		addCloseButton();
		addCloseListener();
	}

	public void rebuildStack() {
		world.clear();
		Table
		table1 = new Table().bottom().left(),
		table2 = new Table().bottom().left(),
		table3 = new Table().bottom().left();

		if (current != null) {
			worldWidth = (int) Math.ceil(((CustomShapeProp) current).shapes.get(shape).width);
			worldHeight = (int) Math.ceil(((CustomShapeProp) current).shapes.get(shape).height);
		} else {
			worldWidth = 10;
			worldHeight = 10;
		}

		for(int y = worldHeight - 1; y >= 0; y--) for(int x = 0; x < worldWidth; x++) {
			Tile tile = Vars.world.tiles.getn(x + offsetx, y + offsety);
			if (tile != null) table1.image(tile.floor().uiIcon).size(32);
			if (x == worldWidth - 1) table1.row();
		}
		for(int y = worldHeight - 1; y >= 0; y--) for(int x = 0; x < worldWidth; x++) {
			Tile tile = Vars.world.tiles.getn(x + offsetx, y + offsety);
			if (tile != null) if (tile.overlay().uiIcon.found()) {
				table2.image(tile.overlay().uiIcon).size(32);
			} else table2.add().size(32);
			if (x == worldWidth - 1) table2.row();
		}
		for(int y = worldHeight - 1; y >= 0; y--) for(int x = 0; x < worldWidth; x++) {
			Tile tile = Vars.world.tiles.getn(x + offsetx, y + offsety);
			if (tile != null) if (tile.block().uiIcon.found()) {
				table3.image(tile.block().uiIcon).size(32);
			} else table3.add().size(32);
			if (x == worldWidth - 1) table3.row();
		}
		world.add(table1);
		world.add(table2);
		world.add(table3);
		if (current != null) world.add(new Image(current.variantRegions[shape]));
	}

	public void updateValues() {
		if (current != null && shape >= ((CustomShapeProp) current).shapes.size) shape = ((CustomShapeProp) current).shapes.size - 1;
		if (offsetx + worldWidth >= Vars.world.width()) {
			offsetx = Vars.world.width() - worldWidth;
		}
		if (offsety + worldHeight >= Vars.world.height()) {
			offsety = Vars.world.height() - worldHeight;
		}
	}
}
