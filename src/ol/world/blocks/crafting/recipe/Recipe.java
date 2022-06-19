package ol.world.blocks.crafting.recipe;

import arc.util.Nullable;
import mindustry.ctype.UnlockableContent;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.type.Liquid;
import mindustry.type.LiquidStack;
import mindustry.world.consumers.ConsumePower;

import static mindustry.Vars.state;

public class Recipe {
    public static Recipe empty = with(null, -1);
    public ItemStack outputItem;
    public LiquidStack outputLiquid;
    public int outputLiquidDirection = -1;
    public ItemStack[] consumeItems;
    public LiquidStack[] consumeLiquids;

    public float produceTime;

    public Recipe(){
        consumeItems = ItemStack.empty;
        consumeLiquids = LiquidStack.empty;

    }

    public static Recipe with(ItemStack outputItem, ItemStack[] consumeItems, LiquidStack[] consumeLiquids, float produceTime){
        Recipe recipe = new Recipe();
        recipe.output(outputItem, null);
        recipe.consume(consumeItems, consumeLiquids);
        recipe.produceTime = produceTime;
        recipe.check();
        return recipe;
    }

    public static Recipe with(ItemStack outputItem, LiquidStack[] consumeLiquids, float produceTime){
        return with(outputItem, ItemStack.empty, consumeLiquids, produceTime);
    }

    public static Recipe with(ItemStack outputItem, float produceTime){
        return with(outputItem, LiquidStack.empty, produceTime);
    }

    public static Recipe with(ItemStack outputItem, ItemStack[] consumeItems, float produceTime){
        return with(outputItem, consumeItems, LiquidStack.empty, produceTime);
    }

    public static Recipe with(){
        return new Recipe();
    }

    public UnlockableContent mainContent(){
        return outputItem == null ? outputLiquid == null ? null : outputLiquid.liquid : outputItem.item;
    }

    public Recipe produceTime(float produceTime){
        this.produceTime = produceTime;
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public Recipe consume(@Nullable ItemStack[] items, @Nullable LiquidStack[] liquids){
        if(items != null) consumeItems = items;
        if(liquids != null) consumeLiquids = liquids;
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public Recipe output(@Nullable ItemStack item, @Nullable LiquidStack liquid){
        if(item != null) outputItem = item;
        if(liquid != null) outputLiquid = liquid;
        return this;
    }

    public Recipe outputLiquidDirection(int outputLiquidDirection){
        this.outputLiquidDirection = outputLiquidDirection;
        return this;
    }

    private void check(){
//            checkItems();
//            checkLiquids();
    }

    public boolean unlockedNow(){
        for(ItemStack stack : consumeItems){
            Item item = stack.item;
            if(state.rules.hiddenBuildItems.contains(item) || item.isHidden() || !item.unlockedNow()){
                return false;
            }
        }
        for(LiquidStack stack : consumeLiquids){
            Liquid liquid = stack.liquid;
            if(liquid.isHidden() || !liquid.unlockedNow()){
                return false;
            }
        }
        return true;
    }
}