package com.constellio.model.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class CollectionInfo implements Serializable {

	String code;

	Locale mainSystemLocale;

	List<Locale> collectionLocales;

	Language mainSystemLanguage;

	List<Language> collectionLanguages;

	List<String> collectionLanguesCodes;

	List<String> secondaryCollectionLanguageCodes = new ArrayList<>();

	byte collectionId;

	public CollectionInfo(byte collectionId, String code, String mainSystemLanguageCode,
						  List<String> collectionLanguesCodes) {
		this.collectionId = collectionId;
		this.code = code;
		this.mainSystemLocale = Language.withCode(mainSystemLanguageCode).locale;
		this.mainSystemLanguage = Language.withLocale(mainSystemLocale);
		this.collectionLanguesCodes = Collections.unmodifiableList(collectionLanguesCodes);

		List<Locale> collectionLocales = new ArrayList<>();
		for (String collectionLanguesCode : collectionLanguesCodes) {
			collectionLocales.add(Language.withCode(collectionLanguesCode).locale);
		}
		this.collectionLocales = Collections.unmodifiableList(collectionLocales);

		List<Language> languages = new ArrayList<>();
		for (Locale locale : collectionLocales) {
			languages.add(Language.withLocale(locale));
		}
		this.collectionLanguages = Collections.unmodifiableList(languages);

		List<String> secondaryCollectionLanguageCodes = new ArrayList<>();
		for (Locale locale : collectionLocales) {
			if (!locale.getLanguage().equals(mainSystemLanguageCode)) {
				secondaryCollectionLanguageCodes.add(locale.getLanguage());
			}
		}
		this.secondaryCollectionLanguageCodes = Collections.unmodifiableList(secondaryCollectionLanguageCodes);
	}

	public CollectionInfo(byte collectionId, String code, Locale mainSystemLocale, List<Locale> collectionLocales) {
		this.collectionId = collectionId;
		this.code = code;
		this.mainSystemLocale = mainSystemLocale;
		this.collectionLocales = Collections.unmodifiableList(collectionLocales);
		this.mainSystemLanguage = Language.withLocale(mainSystemLocale);

		List<Language> languages = new ArrayList<>();
		for (Locale locale : collectionLocales) {
			languages.add(Language.withLocale(locale));
		}

		this.collectionLanguages = Collections.unmodifiableList(languages);

	}

	public Locale getMainSystemLocale() {
		return mainSystemLocale;
	}

	public List<Locale> getCollectionLocales() {
		return collectionLocales;
	}

	public Language getMainSystemLanguage() {
		return mainSystemLanguage;
	}

	public List<Language> getCollectionLanguages() {
		return collectionLanguages;
	}

	public String getCode() {
		return code;
	}

	public List<String> getCollectionLanguesCodes() {
		return collectionLanguesCodes;
	}

	public List<String> getSecondaryCollectionLanguesCodes() {
		return secondaryCollectionLanguageCodes;
	}

	public boolean isMonoLingual() {
		return collectionLocales.size() == 1;
	}

	public byte getCollectionId() {
		return collectionId;
	}
}
