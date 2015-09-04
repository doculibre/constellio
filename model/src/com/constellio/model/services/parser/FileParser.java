/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.model.services.parser;

import static org.apache.commons.lang.StringUtils.join;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.tika.exception.TikaException;
import org.apache.tika.fork.ForkParser;
import org.apache.tika.metadata.Message;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.Property;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

import com.constellio.model.entities.records.ParsedContent;
import com.constellio.model.services.parser.FileParserException.FileParserException_CannotParse;

public class FileParser {

	private final ForkParsers parsers;
	private boolean forkParserEnabled;
	private LanguageDetectionManager languageDetectionManager;

	public FileParser(ForkParsers parsers, LanguageDetectionManager languageDetectionManager, boolean forkParserEnabled) {
		super();
		this.parsers = parsers;
		this.forkParserEnabled = forkParserEnabled;
		this.languageDetectionManager = languageDetectionManager;
	}

	public ParsedContent parse(InputStream inputStream, long length)
			throws FileParserException_CannotParse {

		BodyContentHandler handler = new BodyContentHandler(200 * 1024 * 1024);
		Metadata metadata = new Metadata();

		try {
			if (forkParserEnabled) {
				ForkParser forkParser = parsers.getForkParser();
				forkParser.parse(inputStream, handler, metadata, new ParseContext());
			} else {
				AutoDetectParser parser = newAutoDetectParser();
				parser.parse(inputStream, handler, metadata);
			}
		} catch (IOException | SAXException | TikaException e) {
			String detectedMimetype = metadata.get(Metadata.CONTENT_TYPE);
			throw new FileParserException_CannotParse(e, detectedMimetype);

		} finally {
			IOUtils.closeQuietly(inputStream);
		}

		String type = metadata.get(Metadata.CONTENT_TYPE);
		String parsedContent = handler.toString().trim();
		String language = languageDetectionManager.tryDetectLanguage(parsedContent);
		Map<String, Object> properties = getPropertiesHashMap(metadata, type);
		return new ParsedContent(parsedContent, language, type, length, properties);

	}

	Map<String, Object> getPropertiesHashMap(Metadata metadata, String mimeType) {
		HashMap<String, Object> properties = new HashMap<String, Object>();

		addKeywordsTo(properties, metadata, "Keywords", TikaCoreProperties.KEYWORDS);
		addPropertyTo(properties, metadata, "Title", TikaCoreProperties.TITLE);
		addPropertyTo(properties, metadata, "Comments", TikaCoreProperties.COMMENTS);
		addPropertyTo(properties, metadata, "Author", TikaCoreProperties.CREATOR);
		addPropertyTo(properties, metadata, "Subject", "subject");
		addPropertyTo(properties, metadata, "Category", "Category");
		addPropertyTo(properties, metadata, "Manager", "Manager");
		addPropertyTo(properties, metadata, "BCC", Message.MESSAGE_BCC);
		addPropertyTo(properties, metadata, "CC", Message.MESSAGE_CC);
		addPropertyTo(properties, metadata, "From", Message.MESSAGE_FROM);
		addPropertyTo(properties, metadata, "To", Message.MESSAGE_TO);

		if (mimeType.contains("xml")) {
			addCommentsTo(properties, metadata, "Comments", TikaCoreProperties.DESCRIPTION, "_x000d_");
			addPropertyTo(properties, metadata, "Company", TikaCoreProperties.PUBLISHER);
		} else {
			addCommentsTo(properties, metadata, "Comments", TikaCoreProperties.COMMENTS, "[\r]");
			addPropertyTo(properties, metadata, "Company", "Company");
		}

		return properties;
	}

	//For Property
	private void addPropertyTo(HashMap<String, Object> properties, Metadata metadata, String key, Property property) {
		if (metadata.get(property) != null && metadata.get(property).isEmpty() == false) {
			properties.put(key, metadata.get(property));
		}
	}

	//For String
	private void addPropertyTo(HashMap<String, Object> properties, Metadata metadata, String key, String value) {
		if (metadata.get(value) != null && metadata.get(value).isEmpty() == false) {
			properties.put(key, metadata.get(value));
		}
	}

	private void addKeywordsTo(HashMap<String, Object> properties, Metadata metadata, String key, Property property) {
		if (metadata.get(property) != null) {
			List<String> finalKeywordsList = new ArrayList<String>();
			String[] keywordsAfterFirstSplit = metadata.get(property).split(";");
			for (String aKeyword : keywordsAfterFirstSplit) {
				String[] keywordsAfterSecondSplit = aKeyword.split(",");
				for (String zeKeyword : keywordsAfterSecondSplit) {
					finalKeywordsList.add(zeKeyword.trim());
				}
			}
			properties.put("List:" + key, finalKeywordsList);
		}
	}

	private void addCommentsTo(HashMap<String, Object> properties, Metadata metadata, String key, Property property,
			String regex) {
		if (metadata.get(property) != null) {
			String[] commentsListAfterSplit = metadata.get(property).split(regex);
			properties.put(key, join(commentsListAfterSplit, " "));
		}
	}

	AutoDetectParser newAutoDetectParser() {
		return new AutoDetectParser();
	}

}
