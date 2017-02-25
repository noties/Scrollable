package ru.noties.scrollable.sample.next;

import java.util.ArrayList;
import java.util.List;

public class ItemsGenerator {

    public static List<String> generate(int count) {
        final List<String> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            list.add("Item #" + i);
        }
        return list;
    }

    private ItemsGenerator() {}
}
