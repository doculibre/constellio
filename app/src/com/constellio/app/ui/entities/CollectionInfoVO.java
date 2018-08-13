package com.constellio.app.ui.entities;

import com.constellio.model.entities.Language;

import java.util.List;
import java.util.Locale;

public class CollectionInfoVO {

	private final Language mainSystemLanguage;
	private final String code;
	private final List<Language> collectionLanguages;
	private final Locale mainSystemLocale;
	private final List<String> secondaryCollectionLanguagesCodes;
	private final List<String> collectionLangaugesCodes;
	private final List<Locale> collectionLocales;


	public CollectionInfoVO(Language mainSystemLanguage, String code, List<Language> collectionLanguages, Locale mainSystemLocale,
							List<String> secondaryCollectionLanguagesCodes, List<String> collectionLangaugesCodes, List<Locale> collectionLocales) {
		this.mainSystemLanguage = mainSystemLanguage;
		this.code = code;
		this.collectionLanguages = collectionLanguages;
		this.mainSystemLocale = mainSystemLocale;
		this.secondaryCollectionLanguagesCodes = secondaryCollectionLanguagesCodes;
		this.collectionLangaugesCodes = collectionLangaugesCodes;
		this.collectionLocales = collectionLocales;
	}

	public Language getMainSystemLanguage() {
		return mainSystemLanguage;
	}

	public String getCode() {
		return code;
	}

	public List<Language> getCollectionLanguages() {
		return collectionLanguages;
	}

	public Locale getMainSystemLocale() {
		return mainSystemLocale;
	}

	public List<String> getSecondaryCollectionLanguagesCodes() {
		return secondaryCollectionLanguagesCodes;
	}

	public List<String> getCollectionLangaugesCodes() {
		return collectionLangaugesCodes;
	}

	public List<Locale> getCollectionLocales() {
		return collectionLocales;
	}
}
