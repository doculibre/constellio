package com.constellio.data.frameworks.extensions;

import com.constellio.data.utils.LazyIterator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Constellio on 2017-02-14.
 */
public class PriorityOrderedList<T> implements Iterable<T> {

	PriorityOrderedList parentPriorityList;

	public PriorityOrderedList(PriorityOrderedList parentPriorityList) {
		this.parentPriorityList = parentPriorityList;
	}

	public PriorityOrderedList() {
	}

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
			returnedItems.add(item.behavior);
		}

		return returnedItems;
	}

	@Override
	public Iterator<T> iterator() {

		final Iterator<OrderedItems<T>> itemsIterator = items.iterator();
		final Iterator<OrderedItems<T>> parentItemsIterator =
				parentPriorityList == null ? Collections.<OrderedItems<T>>emptyIterator() : parentPriorityList.items.iterator();

		return new LazyIterator<T>() {

			OrderedItems<T> currentItem;
			OrderedItems<T> currentParentItem;

			@Override
			protected T getNextOrNull() {


				if (currentItem == null && itemsIterator.hasNext()) {
					currentItem = itemsIterator.next();
				}

				if (currentParentItem == null && parentItemsIterator.hasNext()) {
					currentParentItem = parentItemsIterator.next();
				}

				T returnedValue = null;

				if (currentItem != null && currentParentItem != null) {
					if (currentParentItem.priority <= currentItem.priority) {
						returnedValue = currentParentItem.behavior;
						currentParentItem = null;
					} else {
						returnedValue = currentItem.behavior;
						currentItem = null;
					}

				} else if (currentItem != null && currentParentItem == null) {
					returnedValue = currentItem.behavior;
					currentItem = null;

				} else if (currentItem == null && currentParentItem != null) {
					returnedValue = currentParentItem.behavior;
					currentParentItem = null;
				}

				return returnedValue;
			}
		};

	}

}
