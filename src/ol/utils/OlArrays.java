package ol.utils;

import java.util.ArrayList;

public class OlArrays {
    public static int lengthOf(ArrayList<?> arrayList) {
        return OlArrays.lengthOf(arrayList.toArray());
    }

    public static int lengthOf(Object[] iterable) {
        int index = 0;
        for(Object ignored : iterable) {
            index++;
        }

        return index;
    }
}