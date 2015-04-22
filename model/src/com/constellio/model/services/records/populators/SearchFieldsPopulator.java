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
package com.constellio.model.services.records.populators;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.data.utils.KeyListMap;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.entities.records.ParsedContent;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.parser.LanguageDetectionManager;
import com.constellio.model.services.records.FieldsPopulator;

public class SearchFieldsPopulator extends SeparatedFieldsPopulator implements FieldsPopulator {

	private static final Logger LOGGER = LoggerFactory.getLogger(SearchFieldsPopulator.class);

	//LanguageDetectionManager languageDectionServices;

	ContentManager contentManager;

	List<String> collectionLanguages;

	public SearchFieldsPopulator(MetadataSchemaTypes types, LanguageDetectionManager languageDectionServices,
			ContentManager contentManager,
			List<String> collectionLanguages) {
		super(types);
		//	this.languageDectionServices = languageDectionServices;
		this.contentManager = contentManager;
		this.collectionLanguages = collectionLanguages;
	}

	@Override
	public Map<String, Object> populateCopyfields(Metadata metadata, Object value) {

		if (metadata.isSearchable()) {

			String dataStoreCode = metadata.getDataStoreCode();
			String copiedMetadataCodePrefix;
			if (dataStoreCode.endsWith("_txt")) {
				copiedMetadataCodePrefix = dataStoreCode.replace("_txt", "_txt_");
			} else {
				copiedMetadataCodePrefix = dataStoreCode.replace("_t", "_t_").replace("_ss", "_txt_").replace("_s", "_t_");
			}

			if (metadata.isMultivalue() && metadata.getType().isStringOrText()) {
				List<String> values = asNotNullList(value);
				return populateCopyFieldsOfMultivalueSearchableTextMetadata(values, copiedMetadataCodePrefix);

			} else if (metadata.isMultivalue() && metadata.getType().equals(MetadataValueType.CONTENT)) {
				List<Content> values = asNotNullList(value);
				return populateCopyFieldsOfMultivalueSearchableContentMetadata(values, metadata.getLocalCode());

			} else if (!metadata.isMultivalue() && metadata.getType().isStringOrText()) {
				return populateCopyFieldsOfSinglevalueSearchableTextMetadata((String) value, copiedMetadataCodePrefix);

			} else if (!metadata.isMultivalue() && metadata.getType().equals(MetadataValueType.CONTENT)) {
				return populateCopyFieldsOfSinglevalueSearchableContentMetadata((Content) value, metadata.getLocalCode());

			} else {
				return Collections.emptyMap();
			}

		} else {
			return Collections.emptyMap();
		}
	}

	private void addFilenameAndParsedContent(ContentVersion currentVersion, KeyListMap<String, Object> keyListMap, String code) {
		ParsedContent parsedContent = contentManager.getParsedContent(currentVersion.getHash());

		String contentLanguage = null;
		if (collectionLanguages.size() == 1) {
			contentLanguage = collectionLanguages.get(0);
		} else if (parsedContent != null && collectionLanguages.contains(parsedContent.getLanguage())) {
			contentLanguage = parsedContent.getLanguage();
		}

		if (parsedContent == null || contentLanguage == null) {
			for (String collectionLanguage : collectionLanguages) {
				keyListMap.add(code + "_" + collectionLanguage + "_ss", currentVersion.getFilename());
			}
		} else {
			keyListMap.add(code + "_" + contentLanguage + "_ss", currentVersion.getFilename());
		}

		if (parsedContent != null && contentLanguage != null) {
			keyListMap.add(code + "_txt_" + contentLanguage, parsedContent.getParsedContent());
		}
	}

	private Map<String, Object> populateCopyFieldsOfSinglevalueSearchableContentMetadata(Content value,
			String copiedMetadataCode) {

		KeyListMap<String, Object> keyListMap = new KeyListMap<>();
		if (value != null) {
			ContentVersion currentVersion = value.getCurrentVersion();
			addFilenameAndParsedContent(currentVersion, keyListMap, copiedMetadataCode);

		}
		addEmptyValuesToOtherFields(copiedMetadataCode, keyListMap);
		return (Map) keyListMap.getNestedMap();
	}

	private void addEmptyValuesToOtherFields(String copiedMetadataCode, KeyListMap<String, Object> keyListMap) {
		for (String collectionLanguage : collectionLanguages) {
			String fieldCode = copiedMetadataCode + "_txt_" + collectionLanguage;
			if (!keyListMap.getNestedMap().containsKey(fieldCode)) {
				keyListMap.add(fieldCode, "");
			}

			fieldCode = copiedMetadataCode + "_" + collectionLanguage + "_ss";
			if (!keyListMap.getNestedMap().containsKey(fieldCode)) {
				keyListMap.add(fieldCode, "");
			}
		}
	}

	private Map<String, Object> populateCopyFieldsOfSinglevalueSearchableTextMetadata(String value,
			String copiedMetadataCodePrefix) {

		String valueLanguage = collectionLanguages.get(0);

		Map<String, Object> copyfields = new HashMap<>();
		for (String collectionLanguage : collectionLanguages) {
			String fieldCode = copiedMetadataCodePrefix + collectionLanguage;
			if (collectionLanguage.equals(valueLanguage) && value != null) {
				copyfields.put(fieldCode, value);
			} else {
				copyfields.put(fieldCode, "");
			}
		}

		return copyfields;
	}

	private Map<String, Object> populateCopyFieldsOfMultivalueSearchableTextMetadata(List<String> values,
			String copiedMetadataCodePrefix) {

		KeyListMap<String, Object> keyListMap = new KeyListMap<>();
		for (String value : values) {
			String language = collectionLanguages.get(0);
			if (language != null && collectionLanguages.contains(language)) {
				String fieldCode = copiedMetadataCodePrefix + language;
				keyListMap.add(fieldCode, value);
			}
		}

		for (String collectionLanguage : collectionLanguages) {
			String fieldCode = copiedMetadataCodePrefix + collectionLanguage;
			if (!keyListMap.getNestedMap().containsKey(fieldCode)) {
				keyListMap.add(fieldCode, "");
			}
		}
		return (Map) keyListMap.getNestedMap();
	}

	private Map<String, Object> populateCopyFieldsOfMultivalueSearchableContentMetadata(List<Content> values,
			String copiedMetadataCode) {

		KeyListMap<String, Object> keyListMap = new KeyListMap<>();
		for (Content value : values) {
			ContentVersion currentVersion = value.getCurrentVersion();
			ParsedContent parsedContent = contentManager.getParsedContent(currentVersion.getHash());

			String contentLanguage = null;
			if (collectionLanguages.size() == 1) {
				contentLanguage = collectionLanguages.get(0);
			} else if (parsedContent != null && collectionLanguages.contains(parsedContent.getLanguage())) {
				contentLanguage = parsedContent.getLanguage();
			}

			if (parsedContent == null || contentLanguage == null) {
				for (String collectionLanguage : collectionLanguages) {
					keyListMap.add(copiedMetadataCode + "_" + collectionLanguage + "_ss", currentVersion.getFilename());
				}
			} else {
				keyListMap.add(copiedMetadataCode + "_" + contentLanguage + "_ss", currentVersion.getFilename());
			}
			if (parsedContent != null && contentLanguage != null) {
				keyListMap.add(copiedMetadataCode + "_txt_" + contentLanguage, parsedContent.getParsedContent());
			}
		}

		addEmptyValuesToOtherFields(copiedMetadataCode, keyListMap);
		return (Map) keyListMap.getNestedMap();
	}

	private <T> List<T> asNotNullList(Object value) {
		if (value == null) {
			return Collections.emptyList();
		} else {
			return (List) value;
		}
	}
}
