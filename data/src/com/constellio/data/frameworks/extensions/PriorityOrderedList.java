package com.constellio.data.frameworks.extensions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Constellio on 2017-02-14.
 */
public class PriorityOrderedList<T> implements Iterable<T> {


    List<OrderedItems<T>> items = new ArrayList<>();

    public void add(T extension) {

        add(100, extension);
    }

    public void add(int priority, T extension) {
        items.add(new OrderedItems<>(extension, priority));
        Collections.sort(items);
    }

    public List<T> getItems() {
        List<T> returnedItems = new ArrayList<>();
        for (OrderedItems<T> item : items) {
            returnedItems.add(item .behavior);
        }

        return returnedItems;
    }

    @Override
    public Iterator<T> iterator() {
        return getItems().iterator();
    }

}
