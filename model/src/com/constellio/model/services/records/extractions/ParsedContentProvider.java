package com.constellio.model.services.records.extractions;

import com.constellio.model.entities.records.ParsedContent;
import com.constellio.model.services.contents.ContentManager;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Majid
 */
class ParsedContentProvider {

	Map<String, ParsedContent> cache = new HashMap<>();

	ContentManager contentManager;

	private ParsedContentProvider(ContentManager contentManager) {
		this.contentManager = contentManager;
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
