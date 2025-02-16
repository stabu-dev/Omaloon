package mmc;

public class MockOS{

    /** User's home directory. */
    public static final String userHome = prop("user.home");
    public static boolean isWindows = propNoNull("os.name").contains("Windows");
    public static boolean isLinux = propNoNull("os.name").contains("Linux") || propNoNull("os.name").contains("BSD");
    public static boolean isMac = propNoNull("os.name").contains("Mac");

    public static String env(String name){
        return System.getenv(name);
    }

    public static String propNoNull(String name){
        String s = prop(name);
        return s == null ? "" : s;
    }

    public static String prop(String name){
        return System.getProperty(name);
    }

    public static String getAppDataDirectoryString(String appname){
        if(MockOS.isWindows){
            return env("AppData") + "\\" + appname;
        }else if(isLinux){
            if(System.getenv("XDG_DATA_HOME") != null){
                String dir = System.getenv("XDG_DATA_HOME");
                if(!dir.endsWith("/")) dir += "/";
                return dir + appname + "/";
            }
            return userHome + "/.local/share/" + appname + "/";
        }else if(isMac){
            return userHome + "/Library/Application Support/" + appname + "/";
        }else{ //else, probably web
            return "";
        }
    }
}
