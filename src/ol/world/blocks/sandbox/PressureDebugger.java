package ol.world.blocks.sandbox;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.scene.ui.layout.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.*;
import mindustry.world.blocks.defense.*;
import mindustry.world.meta.*;
import ol.core.*;
import ol.utils.pressure.*;

import static mindustry.Vars.world;

//THIS BLOCK ENABLED ONLY IN EXPERIMENTAL MODE
//THIS BLOCK CREATED TO CHECK BUGS IN PRESSURE SYSTEM
public class PressureDebugger extends Wall{
    public PressureDebugger(String name){
        super(name);

        this.flashColor = Color.white;
        this.configurable = true;
        this.health = Integer.MAX_VALUE;
        this.buildVisibility = new BuildVisibility(()-> {
            return BuildVisibility.sandboxOnly.visible() && SettingsManager.debug.get();
        });

        config(Boolean.class, (PressureDebuggerBuild build, Boolean bool) -> {
            build.showingMinimap = bool;
        });
    }

    public class PressureDebuggerBuild extends WallBuild{
        public boolean showingMinimap = false;

        @Override
        public boolean collide(Bullet other){
            return false;
        }

        @Override
        public boolean collision(Bullet other){
            return false;
        }

        public String toColor(int r, int g, int b){
            return formatText(
                "#{}{}{}",

                hexAntiTrim(r, 2),
                hexAntiTrim(g, 2),
                hexAntiTrim(b, 2)
            );
        }

        public String repeat(String str, int count){
            StringBuilder result = new StringBuilder();
            for(int i = 0; i < count; i++){
                result.append(str);
            }

            return result.toString();
        }

        public String antiTrim(String str, String filler, int length){
            //null check
            if(str == null || filler == null){
                return null;
            }

            //returns str if filler is empty or str is bigger or equals when length
            if(filler.trim().length() == 0 || str.length() >= length){
                return str;
            }

            //getting how many filler repeats
            int repeatCount = length / filler.length();

            //if string is empty when return filler
            if(str.trim().length() == 0){
                return repeat(filler, repeatCount);
            }

            //returning result
            return repeat(filler, repeatCount).substring(str.length()) + str;
        }

        public String antiTrimTimer(String str, int length){
            return antiTrim(str, "0", length);
        }

        public String hexAntiTrim(int number, int length){
            return antiTrimTimer(Integer.toHexString(number), length);
        }

        @Override
        public void kill(){
            //HAHA
        }

        @Override
        public void draw(){
            super.draw();

            if(showingMinimap){
                drawMinimap(x, y + size * 8F + 40F, 80F);
            }
        }

        public void drawMinimap(float dx, float dy, float scale){
            if(world == null || world.tiles == null){
                return;
            }

            if(PressureUpdater.nets.isEmpty()){
                return;
            }

            Draw.z(Layer.blockBuilding + 10F);
            Draw.color(Color.cyan);
            Draw.alpha(0.10F);
            Fill.rect(dx, dy, scale + 8F, scale + 8F);

            float wx = dx - scale / 2F;
            float wy = dy - scale / 2F;
            int width = world.width();
            int height = world.height();

            float tw, th;

            tw = 1F / width * scale;
            th = 1F / height * scale;

            for(PressureNet net : PressureUpdater.nets){


                Draw.color(net.color);
                for(int i = 0; i < net.buildingAmount(); i++){
                    Building build = world.build(net.buildingPosition(i));
                    if(build == null){
                        return;
                    }

                    float tx = build.tileX() / (float)width * scale;
                    float ty = build.tileY() / (float)height * scale;

                    Fill.rect(tx + wx, ty + wy, tw, th);
                }
            }
        }

        public String formatText(String text, Object... args){
            //check text
            if(text == null || text.isEmpty()){
                return text;
            }

            if(args == null || args.length == 0){
                return text; //formatted xd
            }

            //integers
            int posOpener;
            int posCloser;
            int argId = 0;

            //this string need to return
            StringBuilder result = new StringBuilder();

            for(; true; ){
                //getting indexes of the args
                posOpener = text.indexOf('{');
                posCloser = text.indexOf('}');

                //when reached end of the string
                if(posCloser == -1 || posOpener == -1){
                    result.append(text);
                    break;
                }

                //update result and get arg content
                result.append(text, 0, posOpener);
                String arg = text.substring(posOpener + 1, posCloser);
                text = text.substring(posCloser + 1);

                //id
                int argId2;

                try{
                    //get id at value content, {0} - 0, {55} - 55, {} - err
                    argId2 = Integer.parseInt(arg);
                }catch(Exception ignored){
                    //if content not support when get arg and next
                    argId2 = argId++;
                }

                //appending arg value
                Object argObj = args[argId2];
                result.append(argObj == null ? "null" : argObj.toString());
            }

            return result.toString();
        }

        @Override
        public void buildConfiguration(Table table){
            table.table(buttons -> {
                buttons.setBackground(Styles.black5);

                buttons.button(Icon.refresh, PressureUpdater::reload).pad(6F).size(50F);
                buttons.button(Icon.fill, PressureUpdater::uncolor).pad(6F).size(50F);
                buttons.button(Icon.editor, PressureUpdater::mergeNets).pad(6F).size(50F);
                buttons.button(Icon.cancel, PressureUpdater::clearNets).pad(6F).size(50F);
                //...
            }).growX().height(62F).row();

            String general_info = "nets: {}, renderer rate: {}";

            table.table(general -> {
                general.setBackground(Styles.black5);

                general.button("show minimap", () -> configure(!showingMinimap)).update(button -> {
                    button.setText(showingMinimap ? "hide minimap" : "show minimap");
                }).width(300F).pad(12f).row();

                general.add(general_info).update(label -> {
                    int scale = PressureUpdater.nets.size;

                    label.setText(formatText(
                        general_info,

                        switch(scale){
                            case 0 -> "[red]no nets[]";
                            case 1 -> "1 net";
                            default -> size + " nets";
                        },

                        Pressure.getPressureRendererProgress()
                    ));
                }).row();

                general.image()
                    .color(Color.gray)
                    .growX()
                    .height(4F)
                    .padTop(3F)
                    .padBottom(3F)
                    .row()
                ;
            }).row();

            table.table(netsInfo -> netsInfo.setBackground(Styles.black5)).update(netsInfo -> {
                netsInfo.clearChildren();

                if(PressureUpdater.nets.isEmpty()){
                    return;
                }

                int total = 0;
                int netCounter = 1;
                for(PressureNet net : PressureUpdater.nets){
                    int len = net.buildingAmount();

                    netsInfo.add(formatText(
                        "[{}]net {}: {} blocks",

                        net.color.toString(),
                        netCounter++,
                        len
                    )).row();

                    total += len;
                }

                netsInfo.image()
                    .color(Color.gray)
                    .growX()
                    .height(4F)
                    .padTop(3F)
                    .padBottom(3F)
                    .row()
                ;

                netsInfo.add("total: " + total + " blocks").pad(6F);
            }).growX();
        }
    }
}