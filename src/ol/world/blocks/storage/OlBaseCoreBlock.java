package ol.world.blocks.storage;

import arc.graphics.Color;
import arc.scene.ui.Image;
import arc.scene.ui.layout.Table;
import arc.util.Scaling;
import mindustry.type.ItemStack;
import mindustry.type.UnitType;
import mindustry.ui.Styles;
import mindustry.world.blocks.storage.*;

public class OlBaseCoreBlock extends CoreBlock {
    public ItemStack[] minerRequirements;
    public boolean canBuildMiner;
    public UnitType minerType;

    public OlBaseCoreBlock(String name) {
        super(name);
    }

    @Override
    public void init() {
        super.init();
        if(!configurable) {
            configurable = canBuildMiner;
        }
    }

    public class OlBaseCoreBuild extends CoreBuild {
        @Override
        public void buildConfiguration(Table table) {
            table.setBackground(Styles.black6);
            if(canBuildMiner && minerType != null) {
                table.table(miner -> {
                    var module = team.items();
                    if(minerRequirements == null || minerRequirements == ItemStack.empty) {
                        miner.add("FREE").size(1.5f).row();
                    } else {
                        for(var stack : minerRequirements) {
                            miner.table(s -> {
                                s.add(new Image(stack.item.uiIcon).setScaling(Scaling.fit)).size(32);
                                s.table(name -> {
                                    name.add(stack.item.localizedName).row();
                                    name.add("x" + stack.amount).color(module.get(stack.item)
                                            >= stack.amount ? Color.white : Color.red);
                                }).grow();
                            }).row();
                        }
                    }
                    miner.button("Build", () -> {
                        boolean canMake = true;
                        boolean isNotFree = minerRequirements != null && minerRequirements != ItemStack.empty;
                        if(isNotFree) {
                            for(ItemStack stack : minerRequirements) {
                                if(module.get(stack.item) < stack.amount) {
                                    canMake = false;
                                    break;
                                }
                            }
                        }

                        if(canMake) {
                            if(isNotFree) {
                                for(ItemStack stack : minerRequirements) {
                                    module.remove(stack.item, stack.amount);
                                }
                            }

                            minerType.spawn(team, this);
                            table.clearChildren();
                            buildConfiguration(table);
                        }
                    }).size(150, 40).pad(6);
                }).pad(6);
            }
        }
    }
}