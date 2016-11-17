package com.constellio.data.utils.comparators;

public class DefaultStringComparator extends AbstractTextComparator<String> {

	public DefaultStringComparator() {
	}

	public DefaultStringComparator(int maxNormalizedTextLength) {
		super(maxNormalizedTextLength);
	}

	@Override
	protected String getText(String object) {
		return object;
	}

}
