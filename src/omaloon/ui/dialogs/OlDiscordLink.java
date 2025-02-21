package omaloon.ui.dialogs;

import arc.graphics.*;
import arc.scene.ui.*;
import mindustry.gen.*;

import static arc.Core.app;
import static mindustry.Vars.ui;

public class OlDiscordLink extends Dialog{
    public String discordURL = "https://discord.gg/bNMT82Hswb";

    public OlDiscordLink(){
        super("");
        float h = 200f;

        cont.margin(12f);
        Color color = Color.valueOf("7289da");

        cont.table(t -> {
            t.background(Tex.button).margin(0);

            t.table(img -> {
                img.image().height(h - 5).width(40f).color(color);
                img.row();

                img.image().height(5).width(40f).color(
                    color.cpy().mul(0.8f, 0.8f, 0.8f, 1f)
                );
            }).expandY();

            t.table(i -> i.image(Icon.discord)).size(40).left();

            t.add("@setting.omaloon-discord").growX().pad(10);
        }).size(580f, h).pad(10f).left();

        buttons.defaults().size(190f, 50);
        buttons.button("@back", Icon.left, this::hide);

        buttons.button("@copylink", Icon.copy, () -> {
            app.setClipboardText(discordURL);
            ui.showInfoFade("@copied");
        });

        buttons.button("@openlink", Icon.discord, () -> {
            if(!app.openURI(discordURL)){
                ui.showInfoFade("@linkfail");
                app.setClipboardText(discordURL);
            }
        });
    }
}