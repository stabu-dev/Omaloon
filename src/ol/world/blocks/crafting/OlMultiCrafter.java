package ol.world.blocks.crafting;

import arc.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.EnumSet;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.core.*;
import mindustry.ctype.*;
import mindustry.entities.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.logic.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.Block;
import mindustry.world.blocks.*;
import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.consumers.*;
import mindustry.world.draw.*;
import mindustry.world.meta.*;
import ol.type.MultiBar;
import ol.world.meta.RecipeListValue;
import ol.world.blocks.crafting.recipe.Recipe;
import ol.world.blocks.crafting.recipe.ConsumeLiquidDynamic;

import static mindustry.Vars.*;
import static mindustry.ctype.ContentType.liquid;


public class OlMultiCrafter extends Block {
    public final int timerDump;
    public final int timerReBuildBars;
    public Recipe[] recipes = {};

    /** if true, crafters with multiple liquid outputs will dump excess when there's still space for at least one liquid type */
    public boolean dumpExtraLiquid = true;
    public boolean ignoreLiquidFullness = false;

    //TODO should be seconds?
    public float craftTime = 80;
    public Effect craftEffect = Fx.none;
    public Effect updateEffect = Fx.none;
    public Effect changeCraftEffect = Fx.none;
    public float updateEffectChance = 0.04f;
    public float warmupSpeed = 0.019f;
    public DrawBlock drawer = new DrawDefault();
    public boolean changeTexture = false;
    public boolean dynamicItem = true;
    public boolean dynamicLiquid = true;

    public OlMultiCrafter(String name){
        super(name);
        timerDump = timers++;
        timerReBuildBars = timers++;
        update = true;
        solid = true;
        hasItems = true;
        ambientSound = Sounds.machine;
        sync = true;
        ambientSoundVolume = 0.03F;
        flags = EnumSet.of(BlockFlag.factory);
        configurable = true;
        destructible = true;
        config(Integer.class, (OlMultiCrafterBuild tile, Integer i) -> {
            tile.currentRecipe = i >= 0 && i < recipes.length ? i : -1;
            Color color;
            boolean spawnEffect = changeTexture;
            if(tile.currentRecipe != -1){
                color = new Color(Color.white).lerp(Color.yellow, 1.0f);
//                if(itemsTexture != null && itemsTexture[i] == region) spawnEffect = false;
            }else{
                color = new Color(Color.black).lerp(Color.white, 0.0f);
            }
            if(spawnEffect && changeCraftEffect != null){
                changeCraftEffect.at(tile.x, tile.y, tile.block.size * 1.1f, color, new Color(Color.black).lerp(Color.white, 8.0f));
            }
            tile.progress = 0.0F;
            tile.rebuildInfo();

        });
        if(dynamicItem){
            this.consumeBuilder.add(new ConsumeItemDynamic(e -> {
                return ((OlMultiCrafterBuild)e).getCurrentRecipe().consumeItems;
            }));
        }
        if(dynamicLiquid){
            this.consumeBuilder.add(new ConsumeLiquidDynamic<OlMultiCrafterBuild>(e -> {
                return e.getCurrentRecipe().consumeLiquids;
            }));
        }
    }

    public void recipes(Recipe... recipes){
        this.recipes = recipes;
    }

    @Override
    public void load(){
        super.load();
        drawer.load(this);
    }

    @Override
    public void setStats(){
        stats.add(Stat.size, "@x@", size, size);
        stats.add(Stat.health, (float)health, StatUnit.none);
        if(canBeBuilt()){
            stats.add(Stat.buildTime, buildCost / 60.0F, StatUnit.seconds);
            stats.add(Stat.buildCost, StatValues.items(false, requirements));
        }

        if(instantTransfer){
            stats.add(Stat.maxConsecutive, 2.0F, StatUnit.none);
        }


        for(var c : consumers){
            c.display(stats);
        }
        if(hasLiquids){
            stats.add(Stat.liquidCapacity, liquidCapacity, StatUnit.liquidUnits);
        }

        if(hasItems && itemCapacity > 0){
            stats.add(Stat.itemCapacity, (float)itemCapacity, StatUnit.items);
        }
        stats.add(Stat.output, new RecipeListValue(recipes));


    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list){
        drawer.drawPlan(this, plan, list);
    }

    @Override
    public TextureRegion[] icons(){
        return drawer.finalIcons(this);
    }

    @Override
    public boolean outputsItems(){
        return Structs.contains(recipes, r -> r.outputItem != null && r.outputItem.amount > 0);
    }

    @Override
    public void getRegionsToOutline(Seq<TextureRegion> out){
        drawer.getRegionsToOutline(this, out);
    }

    @Override
    public void init(){
        rotate |= Structs.contains(recipes, recipe -> recipe.outputLiquid != null && recipe.outputLiquidDirection != -1);
        super.init();


        this.config(Item.class, (obj, item) -> {
            OlMultiCrafterBuild tile = (OlMultiCrafterBuild)obj;
            tile.currentRecipe = Structs.indexOf(recipes, recipe -> recipe.outputItem.item == item);
            tile.resetProgress();
        });
    }

    @Override
    public void setBars(){
        addBar("health", (entity) -> {
            return (new Bar("stat.health", Pal.health, entity::healthf)).blink(Color.white);
        });

        if(consPower != null){
            boolean buffered = consPower.buffered;
            float capacity = consPower.capacity;

            addBar("power", entity -> new Bar(
                    () -> buffered ? Core.bundle.format("bar.poweramount", Float.isNaN(entity.power.status * capacity) ? "<ERROR>" : UI.formatAmount((int)(entity.power.status * capacity))) :
                            Core.bundle.get("bar.power"),
                    () -> Pal.powerBar,
                    () -> Mathf.zero(consPower.requestedPower(entity)) && entity.power.graph.getPowerProduced() + entity.power.graph.getBatteryStored() > 0f ? 1f : entity.power.status)
            );
        }

        if(unitCapModifier != 0){
            stats.add(Stat.maxUnits, (unitCapModifier < 0 ? "-" : "+") + Math.abs(unitCapModifier));
        }
        //liquids added last
        if(hasLiquids){
            ConsumeLiquidDynamic<?> consumeLiquidDynamic = (ConsumeLiquidDynamic<?>)Structs.find(consumers, c -> c instanceof ConsumeLiquidDynamic);
            if(consumeLiquidDynamic != null){
                addBar("bar.liquid-capacity", (OlMultiCrafterBuild build) -> {
                    if(build == null) return new Bar("0", Color.black.cpy(), () -> 0f);
                    Seq<MultiBar.BarPart> barParts = new Seq<>();

                    LiquidStack[] stacks = build.getNeedLiquids();
                    for(LiquidStack stack : stacks){
                        barParts.add(new MultiBar.BarPart(stack.liquid.color, () -> {
                            if(build.liquids == null) return 0.0f;
                            float amounts = build.liquids.get(stack.liquid);
                            float need = stack.amount;
                            if(need == 0 && build.currentRecipe != -1) return 0;
                            return Math.max(amounts / need, 0);
                        }));
                    }
                    return new MultiBar(() -> {
                        String text = Core.bundle.get("bar.liquid-capacity");
                        if(build.liquids == null)
                            return text;
                        return text + " " + Mathf.round((build.countNowLiquid() / build.countRequiredLiquid() * 100f), 0.1f) + "%";
                    }, barParts);
                });
            }
        }
    }


    public class OlMultiCrafterBuild extends Building{
        public int currentRecipe = -1;
        public OlMultiCrafter block = OlMultiCrafter.this;
        public float progress;
        public float totalProgress;
        public float warmup;
        protected Runnable rebuildBars = () -> {
        };
        protected Runnable rebuildCons = () -> {
        };

        @Override
        public void draw(){
            drawer.draw(this);
        }

        @Override
        public void drawLight(){
            super.drawLight();
            drawer.drawLight(this);
        }

        @Override
        public boolean shouldConsume(){
            if(getCurrentRecipe().outputItem != null){
                var output = getCurrentRecipe().outputItem;
                if(items.get(output.item) + output.amount > itemCapacity){
                    return false;
                }
            }
            if(getCurrentRecipe().outputLiquid != null && !ignoreLiquidFullness){
                boolean allFull = true;
                var output = getCurrentRecipe().outputLiquid;
                if(liquids.get(output.liquid) >= liquidCapacity - 0.001f){
                    if(!dumpExtraLiquid){
                        return false;
                    }
                }else{
                    //if there's still space left, it's not full for all liquids
                    allFull = false;
                }

                //if there is no space left for any liquid, it can't reproduce
                if(allFull){
                    return false;
                }
            }

            return enabled;
//            return getCurrentRecipe().outputItem == null || this.items.get(getCurrentRecipe().outputItem.item) < MultiCrafter.this.itemCapacity;
        }

        public void updateTile(){
            if(timer.get(timerReBuildBars, 10)){
                setBars();
            }

            if(currentRecipe < 0 || currentRecipe >= recipes.length){
                currentRecipe = -1;
                progress = 0;
            }

            if(canCraft() && currentRecipe != -1){
                progress += getProgressIncrease(recipes[currentRecipe].produceTime);
                totalProgress += delta();
            }

            if(efficiency > 0 && currentRecipe != -1){

                progress += getProgressIncrease(recipes[currentRecipe].produceTime);
                warmup = Mathf.approachDelta(warmup, warmupTarget(), warmupSpeed);

                //continuously output based on efficiency
                Recipe recipe = getCurrentRecipe();
                if(recipe.outputLiquid != null){
                    float inc = getProgressIncrease(1f);
                    var output = recipe.outputLiquid;
                    handleLiquid(this, output.liquid, Math.min(output.amount * inc, liquidCapacity - liquids.get(output.liquid)));
                }

                if(wasVisible && Mathf.chanceDelta(updateEffectChance)){
                    updateEffect.at(x + Mathf.range(size * 4f), y + Mathf.range(size * 4));
                }
            }else{
                warmup = Mathf.approachDelta(warmup, 0f, warmupSpeed);
            }

            //TODO may look bad, revert to edelta() if so
            totalProgress += warmup * Time.delta;

            if(progress >= 1f){
                craft();
            }

            dumpOutputs();
            if(getCurrentRecipe().outputItem != null && timer(timerDump, 5.0F)){
                dump(getCurrentRecipe().outputItem.item);
            }
        }


        public float warmupTarget(){
            return 1f;
        }

        @Override
        public float warmup(){
            return warmup;
        }

        @Override
        public float totalProgress(){
            return totalProgress;
        }

        private void craft(){
            consume();
            if(getCurrentRecipe().outputItem != null){
                for(int i = 0; i < getCurrentRecipe().outputItem.amount; ++i){
                    offload(getCurrentRecipe().outputItem.item);
                }
            }

            craftEffect.at(x, y);
            progress %= 1f;
        }

        public void dumpOutputs(){
            if(currentRecipe == -1) return;
            Recipe recipe = getCurrentRecipe();
            if(recipe.outputItem != null && timer(timerDump, dumpTime / timeScale)){
                var output = recipe.outputItem;
                dump(output.item);
            }

            if(recipe.outputLiquid != null){
                var output = recipe.outputLiquid;
                dumpLiquid(output.liquid, 2f, recipe.outputLiquidDirection);
            }
        }

        @Override
        public double sense(LAccess sensor){
            if(sensor == LAccess.progress) return progress();
            //attempt to prevent wild total liquid fluctuation, at least for crafters
            Recipe recipe = getCurrentRecipe();
            if(sensor == LAccess.totalLiquids && recipe != null && recipe.outputLiquid != null) return liquids.get(recipe.outputLiquid.liquid);
            return super.sense(sensor);
        }

        @Override
        public float progress(){
            return Mathf.clamp(progress);
        }

        @Override
        public int getMaximumAccepted(Item item){
            return itemCapacity;
        }

        @Override
        public boolean shouldAmbientSound(){
            return efficiency > 0;
        }

        //custom part

        @Override
        public void drawSelect(){
            super.drawSelect();
            Recipe recipe = getCurrentRecipe();
            if(recipe != null && recipe.outputLiquid != null){
                int dir = recipe.outputLiquidDirection;

                if(dir != -1){
                    Draw.rect(
                            recipe.outputLiquid.liquid.fullIcon,
                            x + Geometry.d4x(dir + rotation) * (size * tilesize / 2f + 4),
                            y + Geometry.d4y(dir + rotation) * (size * tilesize / 2f + 4),
                            8f, 8f
                    );
                }
            }
        }

        @Override
        public void displayConsumption(Table table){
            rebuildCons = () -> {
                table.clearChildren();
                table.clear();

                table.left();
                for(Consume cons : block.consumers){
                    if(cons.optional && cons.booster) continue;
                    cons.build(self(), table);
                }
            };
            rebuildCons.run();

        }

        @Override
        public void displayBars(Table table){

            rebuildBars = () -> {
                table.clearChildren();
                for(Func<Building, Bar> bar : block.listBars()){
                    var result = bar.get(self());
                    if(result == null) continue;
                    table.add(result).growX();
                    table.row();
                }
            };
            rebuildBars.run();
        }

        @Override
        public void drawStatus(){

            if(this.currentRecipe == -1) return;
            if(!OlMultiCrafter.this.changeTexture || (OlMultiCrafter.this.size >= 2)){
                super.drawStatus();
            }else{
                float brcx = tile.drawx() + (float)(block.size * 8) / 2.0F - 2.0F;
                float brcy = tile.drawy() - (float)(block.size * 8) / 2.0F + 2.0F;
                Draw.z(71.0F);
                Draw.color(Pal.gray);
                Fill.square(brcx, brcy, 1.25f, 45.0F);
                Draw.color(status().color);
                Fill.square(brcx, brcy, 0.75f, 45.0F);
                Draw.color();
            }

        }

        public <T> void buildTable(Table table, Seq<T> items, Func<T, UnlockableContent> itemToContent, Prov<T> holder, Cons<T> consumer){

            ButtonGroup<ImageButton> group = new ButtonGroup<>();
            group.setMinCheckCount(0);
            Table cont = new Table();
            cont.defaults().size(40);

            int i = 0;

            for(T item : items){
//                if(!content.unlockedNow() || () || content.isHidden()) continue;
                UnlockableContent content = itemToContent.get(item);
                ImageButton button = cont.button(Tex.whiteui, Styles.clearTogglei, 24, () -> control.input.config.hideConfig()).group(group).tooltip(content.localizedName).get();
                button.changed(() -> consumer.get(button.isChecked() ? item : null));
                button.getStyle().imageUp = new TextureRegionDrawable(content.uiIcon);
                button.update(() -> button.setChecked(holder.get() == item));

                if(i++ % 4 == 3){
                    cont.row();
                }
            }

            //add extra blank spaces so it looks nice
            if(i % 4 != 0){
                int remaining = 4 - (i % 4);
                for(int j = 0; j < remaining; j++){
                    cont.image(Styles.black6);
                }
            }

            ScrollPane pane = new ScrollPane(cont, Styles.smallPane);
            pane.setScrollingDisabled(true, false);

            pane.setScrollYForce(block.selectScroll);
            pane.update(() -> {
                block.selectScroll = pane.getScrollY();
            });

            pane.setOverscroll(false, false);
            table.add(pane).maxHeight(Scl.scl(40 * 5));
        }

        @Override
        public void buildConfiguration(Table table){
            Seq<Recipe> recipes = Seq.with(OlMultiCrafter.this.recipes).filter(Recipe::unlockedNow);
            if(recipes.any()){
                this.buildTable(table, recipes, (Recipe it) -> it.mainContent(), () -> {
                    return currentRecipe == -1 ? null : OlMultiCrafter.this.recipes[currentRecipe];
                }, (item) -> {
                    this.configure(Structs.indexOf(OlMultiCrafter.this.recipes, item));
                });
            }else{
                table.table(Styles.black3, (t) -> {
                    t.add("@none").color(Color.lightGray);
                });
            }
        }

        public float countNowLiquid(){
            float amounts = 0;
            for(LiquidStack stack : getNeedLiquids()){
                amounts += Math.min(this.liquids.get(stack.liquid), stack.amount);
            }
            return amounts;
        }

        public float countRequiredLiquid(){
            float need = 0;
            for(LiquidStack stack : getNeedLiquids()){
                need += stack.amount;
            }
            return need;
        }

        public LiquidStack[] getNeedLiquids(){
            return getCurrentRecipe().consumeLiquids;
        }

        @Override
        public void handleLiquid(Building source, Liquid liquid, float amount){
            Recipe recipe = getCurrentRecipe();
            if (recipe==null)return;
            if(recipe.outputLiquid != null && liquid == recipe.outputLiquid.liquid){
                float need = Math.max(0, liquidCapacity - liquids.get(liquid));
                this.liquids.add(liquid, Math.min(amount, need));
                return;
            }
            LiquidStack[] needLiquids = getNeedLiquids();
            LiquidStack found = Structs.find(needLiquids, (l) -> l.liquid == liquid);
            if(found == null){
                return;
            }
            float need = Math.max(0, found.amount - liquids.get(liquid));
            this.liquids.add(liquid, Math.min(amount, need));
        }

        @Override
        public String toString(){
            return "MultiCrafterBuild#" + id;
        }

        @Override
        public boolean acceptLiquid(Building source, Liquid liquid){
            LiquidStack found = Structs.find(getNeedLiquids(), (l) -> l.liquid.name.equals(liquid.name));
            return found != null && this.liquids.get(liquid) <= liquidCapacity;
        }

        @Override
        public boolean acceptItem(Building source, Item item){
            /*int[] count = {-1};
            for(ItemStack stack : getCurrentRecipe().consumeItems){
                if(stack.item == item) count[0] += stack.amount;
            }
            return count[0] > 0 && this.items.get(item) < count[0];*/
            int maximumAccepted = getMaximumAccepted(item);
            return maximumAccepted > 0 && items.get(item) < maximumAccepted;
        }

        public Recipe getCurrentRecipe(){
            if(currentRecipe == -1) return Recipe.empty;
            return recipes[currentRecipe];
        }

        public boolean canCraft(){
            ItemStack[] requirements = getCurrentRecipe().consumeItems;
            int req = 0;
            for(ItemStack i : requirements){
                req += (i.amount + i.item.id);
            }
            int[] counter = {0};
            items.each((item, c) -> {
                counter[0] += (item.id + c);
            });
            int now = counter[0];

            return this.consumeTriggerValid() && req <= now;
        }

        public Object config(){
            return currentRecipe;
        }

        public void write(Writes write){
            super.write(write);
            write.i(currentRecipe);
        }

        public void playerPlaced(Object config){
            if(lastConfig == null) lastConfig = -1;
            if(config == null){
                if(!lastConfig.equals(-1)) configure(lastConfig);
            }else{
                configure(config);
            }
        }

        public void read(Reads read, byte revision){
            super.read(read, revision);
            currentRecipe = read.i();
            if(currentRecipe < 0 || currentRecipe >= recipes.length){
                currentRecipe = -1;
                progress = 0;
            }
        }

        public void resetProgress(){
            progress = 0f;
            totalProgress = 0f;
        }

        public void rebuildInfo(){
            rebuildBars.run();
            rebuildCons.run();
        }
    }
}