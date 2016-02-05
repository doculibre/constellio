package com.constellio.data.frameworks.extensions;

public class OrderedExtension<T> implements Comparable<OrderedExtension<T>> {

	T behavior;

	int priority;

	OrderedExtension(T behavior, int priority) {
		this.behavior = behavior;
		this.priority = priority;
	}

	@Override
	public int compareTo(OrderedExtension<T> other) {
		return new Integer(priority).compareTo(other.priority);
	}

}
