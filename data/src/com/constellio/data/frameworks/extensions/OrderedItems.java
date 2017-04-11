package com.constellio.data.frameworks.extensions;

public class OrderedItems<T> implements Comparable<OrderedItems<T>> {

	T behavior;

	int priority;

	OrderedItems(T behavior, int priority) {
		this.behavior = behavior;
		this.priority = priority;
	}

	@Override
	public int compareTo(OrderedItems<T> other) {
		return new Integer(priority).compareTo(other.priority);
	}

}
