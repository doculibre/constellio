package com.constellio.model.entities.records.wrappers;

import java.util.ArrayList;
import java.util.List;

import com.constellio.data.utils.AccentApostropheCleaner;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public class Capsule extends RecordWrapper {

	public static final String SCHEMA_TYPE = "capsule";

	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";

	public static final String CODE = "code";

	public static final String HTML = "html";

	public static final String KEYWORDS = "keywords";
	
	public static final String LANGUAGE = "language";
	
	public static final String IMAGES = "images";

	public Capsule(Record record, MetadataSchemaTypes types) {
		super(record, types, SCHEMA_TYPE);
	}

	public String getCode() {
		return get(CODE);
	}

	public Capsule setCode(String code) {
		set(CODE, code);
		return this;
	}

	public String getHtml() {
		return get(HTML);
	}

	public String getHTML() {
		return get(HTML);
	}

	public Capsule setHTML(String html) {
		set(HTML, html);
		return this;
	}

	public List<String> getKeywords() {
		return getList(KEYWORDS);
	}

	public Capsule setKeywords(List<String> keywords) {
		keywords = trimKeyWords(keywords);
		set(KEYWORDS, keywords);
		return this;
	}

	private List<String> trimKeyWords(List<String> keyswords) {
		List<String> trimmedKeyWords = new ArrayList<>();
		for (String keyword : keyswords) {
			String lowerCase = keyword.toLowerCase();
			String accentRemoved = AccentApostropheCleaner.removeAccents(lowerCase);
			trimmedKeyWords.add(accentRemoved);
		}
		return trimmedKeyWords;
	}

	public String getLanguage() {
		return get(LANGUAGE);
	}

	public Capsule setLanguage(String language) {
		set(LANGUAGE, language);
		return this;
	}

	public List<Content> getImages() {
		return get(IMAGES);
	}

	public Capsule setImages(List<Content> images) {
		set(IMAGES, images);
		return this;
	}
}
