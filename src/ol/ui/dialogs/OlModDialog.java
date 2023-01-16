package ol.ui.dialogs;

import arc.*;
import arc.graphics.*;
import arc.scene.style.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import mindustry.ctype.*;
import mindustry.gen.*;
import mindustry.ui.*;
import mindustry.ui.dialogs.*;

import java.lang.reflect.*;

import static mindustry.Vars.*;
import static mma.ModVars.*;
import static ol.ui.dialogs.OlUpdateCheckDialog.mod;

public class OlModDialog extends BaseDialog{
    private static final Runnable reinstaller;

    static{
        try{
            Method mod = ModsDialog.class.getDeclaredMethod("githubImportMod", String.class, boolean.class, String.class);
            mod.setAccessible(true);
            reinstaller = () -> {
                try{
                    mod.invoke(ui.mods, modInfo.getRepo(), modInfo.isJava(), null);
                }catch(IllegalAccessException | InvocationTargetException e){
                    throw new RuntimeException(e);
                }
            };
        }catch(NoSuchMethodException e){
            throw new RuntimeException(e);
        }
    }

    public OlModDialog(){
        super("");
        cont.image(() -> Core.atlas.find(fullName("img"))).row();
        cont.row();
        cont.pane(desc -> {
            desc.center();
            desc.defaults().padTop(10).left();

            desc.add("@editor.author").padRight(10).color(Color.gray);
            desc.row();
            desc.add(mod.meta.author).growX().wrap().padTop(0);
            desc.row();

            desc.add("@editor.description").padRight(10).color(Color.gray).top();
            desc.row();
            desc.add(mod.meta.description).growX().wrap().padTop(0);
            desc.row();

        }).width(400f);

        Seq<UnlockableContent> all = Seq.with(content.getContentMap()).<Content>flatten().select(c -> c.minfo.mod == modInfo && c instanceof UnlockableContent).as();
        buttons.defaults().size(210f, 64f).row();
        buttons.button("@ol.youtube", Icon.play, () -> Core.app.openURI("https://www.youtube.com/@omaloon"));
        if(!all.isEmpty()){
            buttons.button("@mods.viewcontent", Icon.book, () -> showContentDialog(all));
        }
        buttons.button("@ol.discord-join", Icon.discord, new OlDiscordLink()::show);
        buttons.row();
        buttons.button("@back", Icon.left, this::hide);
        if(modInfo.getRepo() != null) {
            buttons.button("@mods.github.open", Icon.link, () -> Core.app.openURI("https://github.com/" + modInfo.getRepo()));
        }
        if(modInfo.getRepo() != null) {
            boolean showImport = !modInfo.hasSteamID();
            if(mobile && showImport) buttons.row();
            if(showImport) buttons.button("@mods.browser.reinstall", Icon.download, reinstaller);
        }
        if(!mobile){
            buttons.button("@mods.openfolder", Icon.folder, () -> Core.app.openFolder(modDirectory.absolutePath()));
        }
    }

    private void showContentDialog(Seq<UnlockableContent> all){
        BaseDialog d = new BaseDialog(modInfo.meta.displayName());
        d.cont.pane(cs -> {
            int i = 0;
            for(UnlockableContent c : all){
                cs.button(new TextureRegionDrawable(c.uiIcon), Styles.cleari, iconMed, () -> {
                    ui.content.show(c);
                }).size(50f).with(im -> {
                    var click = im.getClickListener();
                    im.update(() -> im.getImage().color.lerp(!click.isOver() ? Color.lightGray : Color.white, 0.4f * Time.delta));

                }).tooltip(c.localizedName);

                if(++i % (int)Math.min(Core.graphics.getWidth() / Scl.scl(110), 14) == 0) cs.row();
            }
        }).grow();
        d.addCloseButton();
        d.show();
        this.resized(d::hide);
    }
}