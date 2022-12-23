package ol.utils;

public class StringUtils {
    public static String argStr(String text, Object... args) {
        return argStr(text, '{', '}', args);
    }

    public static String notNull(String str) {
        if(str == null) {
            return "null";
        }

        return str;
    }

    public static String argStr(String text, char opener, char closer, Object... args) {
        //check text
        if(text == null || text.isEmpty()) {
            return text;
        }

        if(args == null || args.length == 0) {
            return text; //formatted xd
        }

        //integers
        int posOpener;
        int posCloser;
        int argId = 0;

        //this string need to return
        StringBuilder result = new StringBuilder();

        for(;true;) {
            //getting indexes of the args
            posOpener = text.indexOf(opener);
            posCloser = text.indexOf(closer);

            //when reached end of the string
            if(posCloser == -1 || posOpener == -1) {
                result.append(text);
                break;
            }

            //update result and get arg content
            result.append(text, 0, posOpener);
            String arg = text.substring(posOpener + 1, posCloser);
            text = text.substring(posCloser + 1);

            //id
            int argId2;

            try {
                //get id at value content, {0} - 0, {55} - 55, {} - err
                argId2 = Integer.parseInt(arg);
            } catch(Exception ignored) {
                //if content not support when get arg and next
                argId2 = argId++;
            }

            //appending arg value
            Object argObj = args[argId2];
            result.append(argObj == null ? "null" : argObj.toString());
        }

        return result.toString();
    }
}