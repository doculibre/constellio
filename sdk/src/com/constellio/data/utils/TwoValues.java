package com.constellio.data.utils;

public class TwoValues<F, S> {

	F first;

	S second;

	public TwoValues(F first, S second) {
		super();
		this.first = first;
		this.second = second;
	}

	public F getFirst() {
		return first;
	}

	public void setFirst(F first) {
		this.first = first;
	}

	public S getSecond() {
		return second;
	}

	public void setSecond(S second) {
		this.second = second;
	}

}
