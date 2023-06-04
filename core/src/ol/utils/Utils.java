package ol.utils;

import arc.struct.IntSet;

public class Utils {
    public static int getByIndex(IntSet intSet, int index) {
        if (index < 0 || index >= intSet.size) {
            throw new IndexOutOfBoundsException();
        }

        final int[] value = {0};
        final int[] counter = {0};
        intSet.each((item) -> {
            if (counter[0] == index) {
                value[0] = item;
            }
            counter[0]++;
        });

        if (counter[0] > index) {
            return value[0];
        } else {
            throw new IllegalArgumentException();
        }
    }
}
