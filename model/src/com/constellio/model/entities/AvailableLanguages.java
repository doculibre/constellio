package com.constellio.model.entities;

import java.util.List;
import java.util.Locale;

public class AvailableLanguages {

	Locale mainSystemLocale;

	List<Locale> getCollectionLocales;

	public AvailableLanguages(Locale mainSystemLocale, List<Locale> getCollectionLocales) {
		this.mainSystemLocale = mainSystemLocale;
		this.getCollectionLocales = getCollectionLocales;
	}

}
