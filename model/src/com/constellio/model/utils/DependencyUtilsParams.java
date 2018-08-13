package com.constellio.model.utils;

import java.util.Comparator;

/**
 * Created by dakota on 2/4/16.
 */
public class DependencyUtilsParams {

	private Comparator<?> tieComparator = null;

	private boolean sortTie = false;

	private boolean tolerateCyclicDependencies;

	public <V> Comparator<V> getTieComparator() {
		return (Comparator<V>) tieComparator;
	}

	public boolean isSortTie() {
		return sortTie;
	}

	public boolean isTolerateCyclicDependencies() {
		return tolerateCyclicDependencies;
	}

	public DependencyUtilsParams(DependencyUtilsParams params) {
		this.tieComparator = params.tieComparator;
		this.sortTie = params.sortTie;
		this.tolerateCyclicDependencies = params.tolerateCyclicDependencies;
	}

	public DependencyUtilsParams() {
	}

	public DependencyUtilsParams withToleratedCyclicDepencies() {
		tolerateCyclicDependencies = true;
		return this;
	}

	public DependencyUtilsParams withoutToleratedCyclicDepencies() {
		tolerateCyclicDependencies = false;
		return this;
	}

	public DependencyUtilsParams sortTieUsing(Comparator<?> comparator) {
		this.tieComparator = comparator;
		this.sortTie = true;
		return this;
	}

	public DependencyUtilsParams sortUsingDefaultComparator() {
		this.tieComparator = new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return o1.compareTo(o2);
			}
		};
		this.sortTie = true;
		return this;
	}
}
