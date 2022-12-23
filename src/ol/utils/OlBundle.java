package ol.utils;

import ol.Omaloon;

import static arc.Core.*;

public class OlBundle {
    public static String _ol_id_220358(String id) {
        return Omaloon.MOD_PREFIX + "." + StringUtils.notNull(id);
    }

    public static String _mod_ol_id_220358(String id) {
        return "mod." + OlBundle._ol_id_220358(id);
    }

    public static String get(String id, Object... args) {
        return OlBundle.get("", id, args);
    }

    public static String getSetting(String id, Object... args) {
        return OlBundle.get("setting", id, args);
    }

    public static String get(String prefix, String id, Object... args) {
        //100% throws error because anuken
        if(id == null || id.trim().length() == 0) {
            return id;
        }

        //null check
        if(prefix == null) {
            prefix = "";
        }

        //if bundles is null not throws error
        if(bundle == null) {
            return "";
        }

        //prefix need end with .
        if(!prefix.endsWith(".") && prefix.trim().length() > 0) {
            prefix += ".";
        }

        //get ol.{id} bundle
        String olArg = prefix + OlBundle._ol_id_220358(id);

        //if bundle found when return this bundle
        if(bundle.has(olArg)) {
            return StringUtils.argStr(bundle.get(olArg), args);
        } else {
            //but if and this bundle not found when return mod.ol.{id}
            String modOlArg = prefix + OlBundle._mod_ol_id_220358(id);

            if(bundle.has(modOlArg)) {
                return StringUtils.argStr(bundle.get(modOlArg), args);
            } else {
                //getting arg
                String arg = prefix + id;

                if(bundle.has(arg)) {
                    return StringUtils.argStr(bundle.get(arg), args);
                }

                //if this bundle just don`t exists when return ???{id}???
                return StringUtils.argStr("???{}???", arg);
            }
        }
    }
}