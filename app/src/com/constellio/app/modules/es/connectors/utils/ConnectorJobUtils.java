package com.constellio.app.modules.es.connectors.utils;

import java.io.InputStream;

import com.constellio.model.entities.records.ParsedContent;
import com.constellio.model.services.parser.FileParser;
import com.constellio.model.services.parser.FileParserException;

public class ConnectorJobUtils {
	private ConnectorJobUtils() {
	}

	public static String getParsedContent(FileParser fileParser, InputStream inputStream, String contentId){
		try {
			ParsedContent parsedContent = fileParser.parse(inputStream, false);
			if (parsedContent.getParsedContent().isEmpty()) {
				throw new ConnectorJobUtils_DocumentHasNoParsedContent(contentId);
			} else {
				return parsedContent.getParsedContent();
			}
		} catch (FileParserException e) {
			throw new ConnectorJobUtils_CannotParseDocument(contentId, e);

		}
	}

	private static class ConnectorJobUtils_DocumentHasNoParsedContent extends RuntimeException {
		public ConnectorJobUtils_DocumentHasNoParsedContent(
				String contentId) {
			super("Document '" + contentId + "' has no parsed content");
		}
	}

	private static class ConnectorJobUtils_CannotParseDocument extends RuntimeException {
		public ConnectorJobUtils_CannotParseDocument(
				String contentId, FileParserException e) {
			super("Cannot parse document '" + contentId + "'", e);
		}
	}
}
