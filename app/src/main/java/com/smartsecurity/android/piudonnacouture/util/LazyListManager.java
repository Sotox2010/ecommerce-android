package com.smartsecurity.android.piudonnacouture.util;

import java.util.ArrayList;
import java.util.List;

public class  LazyListManager {

    public static <T> List<T> add(List<T> list, T item) {
        if (list == null) {
            list =  new ArrayList<>();
            list.add(item);
        } else if (!list.contains(item)) {
            list.add(item);
        }
        return list;
    }

    public static <T> List<T> remove(List<T> list, T item) {
        if (list != null) {
            list.remove(item);
            if (list.isEmpty()) {
                list = null;
            }
        }
        return list;
    }
}
