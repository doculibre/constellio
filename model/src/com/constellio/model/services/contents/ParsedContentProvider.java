package com.constellio.model.services.contents;

import java.util.HashMap;
import java.util.Map;

import com.constellio.model.entities.records.ParsedContent;

public class ParsedContentProvider {

	Map<String, ParsedContent> cache = new HashMap<>();

	ContentManager contentManager;

	public ParsedContentProvider(ContentManager contentManager) {
		this.contentManager = contentManager;
	}

	public ParsedContentProvider(ContentManager contentManager, Map<String, ParsedContent> cache) {
		this.contentManager = contentManager;
		this.cache = cache;
	}

	public ParsedContent getParsedContentParsingIfNotYetDone(String hash) {
		ParsedContent parsedContent = cache.get(hash);
		if (parsedContent == null) {
			parsedContent = contentManager.getParsedContentParsingIfNotYetDone(hash);
			cache.put(hash, parsedContent);
		}

		return parsedContent;
	}
}
