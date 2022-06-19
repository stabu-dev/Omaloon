package ol.world.meta;

import arc.Core;
import arc.graphics.g2d.TextureRegion;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Table;
import arc.util.Strings;
import mindustry.ctype.UnlockableContent;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.type.ItemStack;
import mindustry.type.LiquidStack;
import mindustry.ui.ItemImage;
import mindustry.world.meta.StatUnit;
import mindustry.world.meta.StatValue;
import mindustry.world.meta.StatValues;
import ol.ui.LiquidImage;
import ol.world.blocks.crafting.recipe.Recipe;
import ol.world.meta.values.LiquidListValue;

public class RecipeListValue implements StatValue {
    private final Recipe[] recipes;

    public RecipeListValue(Recipe... recipes) {
        this.recipes = recipes;
    }

    @Override
    public void display(Table table) {
        table.row();
        for (Recipe recipe : recipes) {
            if (recipe.outputItem != null) {
                table.add(new ItemImage(new ItemStack(recipe.outputItem.item,recipe.outputItem.amount))).size(24.0F).padRight(4.0F).right().top();
                table.add(" "+recipe.outputItem.item.localizedName).padRight(10.0F).left().top();
                if (recipe.outputLiquid != null)table.row();
            }

            if (recipe.outputLiquid != null) {
                table.add(new LiquidImage(new LiquidStack(recipe.outputLiquid.liquid,recipe.outputLiquid.amount))).size(24.0F).padRight(4.0F).right().top();
                table.add(" "+recipe.outputLiquid.liquid.localizedName).padRight(10.0F).left().top();
            }

            (table.table((bt) -> {
                bt.left().defaults().padRight(3.0F).left();
                if (recipe.consumeItems.length > 0) {
                    StatValues.items(false, recipe.consumeItems).display(bt.table().get());
                }

                if (recipe.consumeLiquids.length > 0) {
                    new LiquidListValue(false, recipe.consumeLiquids).display(bt.table().get());
                }
                StatValues.number(recipe.produceTime / 60.0F, StatUnit.seconds).display(bt);
            }).padTop(-5.0f).center().get()).background(Tex.underline);
            table.row();
        }
    }

    void sep(Table table, String text) {
        table.row();
        table.add(text);
    }

    <T extends UnlockableContent> TextureRegion icon(T t) {
        return t.uiIcon;
    }
}
