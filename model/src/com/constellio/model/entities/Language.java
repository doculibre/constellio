package com.constellio.model.entities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public enum Language {

	UNKNOWN("unknown", null),
	//Arabic("ar"),
	//	Armenian,
	//	Basque,
	//	Bulgarian,
	//	Catalan,
	//	Chinese,
	//	Japanese,
	//	Korean,
	//	Czech,
	//	Danish,
	//	Dutch,
	English("en", Locale.ENGLISH),
	//	Finnish,
	French("fr", Locale.FRENCH);
	//	Galician,
	//  German("de"),
	//	Greek,
	//	Hebrew,
	//	Hindi,
	//	Hungarian,
	//	Indonesian,
	//	Italian,
	//	Norwegian,
	//	Persian,
	//	Polish,
	//  Portuguese("pt"),
	//	Romanian,
	//	Russian,
	//  Spanish("es"),
	//	Swedish,
	//	Thai,
	//	Turkish;

	final Locale locale;

	final String code;

	Language(String code, Locale locale) {
		this.code = code;
		this.locale = locale;
	}

	public static boolean isSupported(String languageCode) {

		for (Language language : Language.values()) {
			if (language.code.equals(languageCode)) {
				return true;
			}
		}
		return false;
	}

	public String getCode() {
		return code;
	}

	public Locale getLocale() {
		return locale;
	}

	public static Language withCode(String code) {
		for (Language language : values()) {
			if (code.equals(language.getCode())) {
				return language;
			}
		}
		return French;
	}

	public static List<Language> getAvailableLanguages() {
		return Arrays.asList(values());
	}
}
