package com.constellio.data.utils.comparators;

import java.util.Comparator;

import com.constellio.data.utils.AccentApostropheCleaner;

public abstract class AbstractTextComparator<T> implements Comparator<T> {

	private int maxNormalizedTextLength = 255;
	
	public AbstractTextComparator() {
		this(Integer.MAX_VALUE);
	}
	
	public AbstractTextComparator(int maxNormalizedTextLength) {
		this.maxNormalizedTextLength = maxNormalizedTextLength;
	}
	
	@Override
	public int compare(T o1, T o2) {
		String text1 = o1 != null ? getNormalizedText(o1) : null;
		String text2 = o2 != null ? getNormalizedText(o2) : null;

		int resultat;
		if (text1 == null && text2 == null) {
			resultat = 0;
		} else if (text1 == null) {
			resultat = -1;
		} else if (text2 == null) {
			resultat = 1;
		} else {
			resultat = text1.compareTo(text2);
		}
		return resultat;
	}
	
	public String getNormalizedText(T object) {
		String text;
		if (object != null) {
			text = getText(object);
			if (text != null) {
				text = normalize(text);
				if (text.length() > maxNormalizedTextLength) {
					text = text.substring(0, maxNormalizedTextLength);
				}
			}
		} else {
			text = null;
		}
		return text;
	}
	
	protected String normalize(String text) {
		if (text != null) {
			text = text.toLowerCase();
			text = AccentApostropheCleaner.removeAccents(text);
			text = AccentApostropheCleaner.removeApostrophe(text);
		}
		return text;
	}
	
	protected abstract String getText(T object);

}
